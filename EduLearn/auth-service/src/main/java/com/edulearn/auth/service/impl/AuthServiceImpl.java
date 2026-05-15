package com.edulearn.auth.service.impl;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@org.springframework.transaction.annotation.Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @jakarta.annotation.PostConstruct
    public void initDefaultAdmin() {
        String adminEmail = "admin@edulearn.com";
        Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
        User admin;
        if (adminOpt.isEmpty()) {
            admin = new User();
            admin.setEmail(adminEmail);
            admin.setFullName("Platform Admin");
            admin.setUsername("admin");
            admin.setRole("ADMIN");
        } else {
            admin = adminOpt.get();
        }
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        userRepository.save(admin);
        System.out.println("Default admin updated/created: admin@edulearn.com / Admin@123");
    }

    @Override
    public User registerUser(User user) {
        // Security: Always default to GUEST on first registration.
        user.setRole("GUEST");

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        String username = user.getUsername();
        if (username == null || username.isEmpty()) {
            username = email.split("@")[0];
        }

        // Ensure username is unique
        String baseUsername = username;
        int attempt = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + attempt++;
        }
        
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public String authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Promote GUEST to PENDING on login
        if ("GUEST".equals(user.getRole())) {
            user.setRole("PENDING");
            userRepository.save(user);
        }
        
        return generateToken(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public java.util.Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (userDetails.getUsername() != null) user.setUsername(userDetails.getUsername());
        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
        if (userDetails.getFullName() != null) user.setFullName(userDetails.getFullName());
        if (userDetails.getRole() != null) user.setRole(userDetails.getRole());
        return userRepository.save(user);
    }

    @Override
    public String processOAuthPostLogin(String email, String name) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isEmpty()) {
            user = new User();
            user.setEmail(email);
            String baseName = name != null ? name.replaceAll("\\s+", "_").toLowerCase() : email.split("@")[0];
            user.setUsername(baseName);
            // Ensure unique username for OAuth as well
            int attempt = 1;
            while (userRepository.findByUsername(user.getUsername()).isPresent()) {
                user.setUsername(baseName + attempt++);
            }
            user.setFullName(name != null ? name : email.split("@")[0]);
            user.setRole("PENDING");
            user.setPassword(passwordEncoder.encode("OAUTH_USER_" + Math.random()));
            user = userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        // Promote GUEST to PENDING on login
        if ("GUEST".equals(user.getRole())) {
            user.setRole("PENDING");
            user = userRepository.save(user);
        }

        return generateToken(user);
    }

    @Override
    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        sendResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public User getUserByResetToken(String token) {
        return userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
    }

    @Override
    public String refreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateToken(user);
    }

    @Override
    public void updateEmail(Long userId, String newEmail) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Override
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
    }

    private void sendResetEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("EduLearn - Password Reset Request");
            
            String resetLink = "http://localhost:4200/reset-password?token=" + token;
            String content = "<h3>Hello,</h3>"
                    + "<p>You requested to reset your password for your EduLearn account.</p>"
                    + "<p>Click the link below to set a new password:</p>"
                    + "<a href=\"" + resetLink + "\">Reset Password</a>"
                    + "<p>This link will expire in 1 hour.</p>"
                    + "<p>If you didn't request this, please ignore this email.</p>";
            
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email: " + e.getMessage());
        }
    }

    private String generateToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}
