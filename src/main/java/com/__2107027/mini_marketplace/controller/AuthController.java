package com.__2107027.mini_marketplace.controller;

import com.__2107027.mini_marketplace.dto.LoginRequest;
import com.__2107027.mini_marketplace.dto.RegistrationRequest;
import com.__2107027.mini_marketplace.model.User;
import com.__2107027.mini_marketplace.repository.UserRepository;
import com.__2107027.mini_marketplace.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * User Registration Endpoint
     * All new users are registered as USER role by default
     * Password is encrypted with BCrypt before saving
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        Map<String, String> response = new HashMap<>();

        // Check if username already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            response.put("error", "Username is already taken");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            response.put("error", "Email is already registered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Create new user with USER role by default
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword())); // BCrypt encryption
        user.setRole("user"); // Default role for all new users

        userRepository.save(user);

        response.put("message", "User registered successfully");
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User Login Endpoint
     * Authenticates user and returns JWT token
     * Token contains username and role information
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, String> response = new HashMap<>();

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Get user details from authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Get user from database
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Return token and user info
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("tokenType", "Bearer");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get Current User Info
     * Requires valid JWT token in Authorization header
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }
}
