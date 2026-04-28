package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;

public interface AuthService {
    User registerUser(User user);
    String authenticateUser(String email, String password);
}
