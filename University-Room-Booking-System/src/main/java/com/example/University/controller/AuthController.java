//package com.example.University.controller;
//
//import com.example.University.dto.RegisterRequest;
//import com.example.University.dto.JwtResponse;
//import com.example.University.dto.MessageResponse;
//import com.example.University.dto.LoginRequest;
//import com.example.University.service.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//@Slf4j
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/login")
//    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        log.info("Authentication attempt for username: {}", loginRequest.getUsername());
//
//        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
//
//        log.info("User {} authenticated successfully", loginRequest.getUsername());
//        return ResponseEntity.ok(jwtResponse);
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        log.info("Registration attempt for username: {}", registerRequest.getUsername());
//
//        authService.registerUser(registerRequest);
//
//        log.info("User {} registered successfully", registerRequest.getUsername());
//        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<MessageResponse> logoutUser() {
//        // Since we're using JWT, logout is handled client-side by removing the token
//        // Optionally, implement token blacklisting here
//        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
//    }
//}