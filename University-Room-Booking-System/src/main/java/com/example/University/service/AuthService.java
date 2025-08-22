package com.example.University.service;

import com.example.University.dto.LoginRequest;
import com.example.University.dto.RegisterRequest;
import com.example.University.dto.JwtResponse;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    void registerUser(RegisterRequest registerRequest);
}