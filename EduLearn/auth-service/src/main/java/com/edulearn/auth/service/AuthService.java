package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;

public interface AuthService {
    User registerUser(User user);
    String authenticateUser(String email, String password);
    User getUserByEmail(String email);
    java.util.Optional<User> getUserById(Long id);
    void deleteUser(Long id);
    User updateUser(Long id, User userDetails);
    String processOAuthPostLogin(String email, String name);
    void generateResetToken(String email);
    void resetPassword(String token, String newPassword);
    User getUserByResetToken(String token);
    String refreshToken(String email);
    void updateEmail(Long userId, String newEmail);
    java.util.List<User> getAllUsers();
    void updateUserRole(Long userId, String newRole);
}
