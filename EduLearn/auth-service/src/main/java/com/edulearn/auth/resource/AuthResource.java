package com.edulearn.auth.resource;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            String jwt = authService.authenticateUser(email, password);
            
            // Fetch user to get ID and other details
            User user = authService.getUserByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("id", user.getId());
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User user) {
        System.out.println("Updating profile for user ID: " + user.getId() + " with role: " + user.getRole());
        try {
            User updatedUser = authService.updateUser(user.getId(), user);
            System.out.println("Profile updated successfully for user: " + updatedUser.getEmail());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/oauth2/callback")
    public void oauth2Callback(org.springframework.security.core.Authentication authentication, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) email = oauth2User.getAttribute("unique_name");
        if (email == null) email = oauth2User.getAttribute("sub") + "@oauth.edulearn.com"; // Fallback for ID-only providers
        
        String name = oauth2User.getAttribute("name");
        if (name == null) name = oauth2User.getAttribute("given_name");
        if (name == null) name = email.split("@")[0];
        
        String jwt = authService.processOAuthPostLogin(email, name);
        User user = authService.getUserByEmail(email);
        
        // Redirect to a dedicated callback page to ensure stable session capture
        StringBuilder redirectUrl = new StringBuilder("http://localhost:4200/auth/callback");
        redirectUrl.append("?token=").append(jwt);
        redirectUrl.append("&name=").append(java.net.URLEncoder.encode(name, "UTF-8"));
        redirectUrl.append("&email=").append(email);
        redirectUrl.append("&userId=").append(user.getId());
        redirectUrl.append("&role=").append(user.getRole() != null ? user.getRole() : "PENDING");
        
        response.sendRedirect(redirectUrl.toString());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            authService.generateResetToken(request.get("email"));
            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            authService.resetPassword(request.get("token"), request.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newToken = authService.refreshToken(email);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @DeleteMapping("/account/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long userId) {
        try {
            authService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/account/{userId}/email")
    public ResponseEntity<?> updateEmail(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            authService.updateEmail(userId, request.get("newEmail"));
            return ResponseEntity.ok(Map.of("message", "Email updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Admin Endpoints ---

    @GetMapping("/admin/users")
    public ResponseEntity<java.util.List<Map<String, Object>>> getAllUsers() {
        java.util.List<User> users = authService.getAllUsers();
        java.util.List<Map<String, Object>> response = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("userId", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            map.put("fullName", user.getFullName());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newRole = request.get("newRole");
        try {
            authService.updateUserRole(userId, newRole);
            return ResponseEntity.ok(Map.of("message", "Role updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        java.util.List<User> users = authService.getAllUsers();
        long instructors = users.stream().filter(u -> "INSTRUCTOR".equals(u.getRole())).count();
        long students = users.stream().filter(u -> "STUDENT".equals(u.getRole())).count();
        
        return ResponseEntity.ok(Map.of(
            "totalUsers", users.size(),
            "instructors", instructors,
            "students", students
        ));
    }
}
