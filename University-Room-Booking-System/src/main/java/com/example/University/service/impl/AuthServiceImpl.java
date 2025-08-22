package com.example.University.service.impl;

import com.example.University.entity.Department;
import com.example.University.exception.ResourceAlreadyExistsException;
import com.example.University.exception.ResourceNotFoundException;

import com.example.University.dto.LoginRequest;
import com.example.University.dto.RegisterRequest;
import com.example.University.dto.JwtResponse;
import com.example.University.service.AuthService;
import com.example.University.entity.Role;
import com.example.University.entity.User;
import com.example.University.repository.DepartmentRepository;
import com.example.University.repository.RoleRepository;
import com.example.University.repository.UserRepository;
import com.example.University.security.JwtUtil;
import com.example.University.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;

    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("Authenticating user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsernameWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userDetails.getUsername()));

        // Create extra claims for JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("email", user.getEmail());
        extraClaims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        String jwt = jwtUtil.generateTokenWithClaims(userDetails, extraClaims);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        log.info("User {} authenticated successfully with roles: {}", user.getUsername(), roles);

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .expiresIn(jwtExpiration)
                .build();
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        log.debug("Registering new user: {}", registerRequest.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken: " + registerRequest.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already in use: " + registerRequest.getEmail());
        }

        // Get department if provided
        Department department = null;
        if (registerRequest.getDepartmentId() != null) {
            department = departmentRepository.findById(registerRequest.getDepartmentId())
                    .orElseThrow();
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            // Default role is STUDENT
            Role studentRole = roleRepository.findByName(Role.RoleName.STUDENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role STUDENT not found"));
            roles.add(studentRole);
        } else {
            for (String roleName : registerRequest.getRoles()) {
                try {
                    Role.RoleName roleEnum = Role.RoleName.valueOf(roleName.toUpperCase());
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new ResourceNotFoundException("Invalid role: " + roleName);
                }
            }
        }

        // Create user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .studentId(registerRequest.getStudentId())
                .employeeId(registerRequest.getEmployeeId())
                .department(department)
                .roles(roles)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("User {} registered successfully with roles: {}",
                user.getUsername(),
                roles.stream().map(r -> r.getName().name()).collect(Collectors.toList()));
    }
}