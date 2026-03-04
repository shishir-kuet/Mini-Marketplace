package com.__2107027.mini_marketplace.controller;

import com.__2107027.mini_marketplace.dto.LoginRequest;
import com.__2107027.mini_marketplace.dto.RegistrationRequest;
import com.__2107027.mini_marketplace.model.Role;
import com.__2107027.mini_marketplace.model.User;
import com.__2107027.mini_marketplace.repository.UserRepository;
import com.__2107027.mini_marketplace.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    private RegistrationRequest validRegistrationRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup valid registration request
        validRegistrationRequest = new RegistrationRequest();
        validRegistrationRequest.setUsername("testuser");
        validRegistrationRequest.setEmail("test@example.com");
        validRegistrationRequest.setPassword("password123");

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
    }

    // ===========================
    // REGISTRATION TESTS
    // ===========================

    @Test
    @DisplayName("Registration: Should successfully register a new user")
    void testRegisterUser_Success() throws Exception {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail when username already exists")
    void testRegisterUser_UsernameExists() throws Exception {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username is already taken"));

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail when email already exists")
    void testRegisterUser_EmailExists() throws Exception {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is already registered"));

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with invalid username (too short)")
    void testRegisterUser_InvalidUsernameShort() throws Exception {
        // Arrange
        validRegistrationRequest.setUsername("ab"); // Too short

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with invalid username (too long)")
    void testRegisterUser_InvalidUsernameLong() throws Exception {
        // Arrange
        validRegistrationRequest.setUsername("a".repeat(51)); // Too long

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with invalid email format")
    void testRegisterUser_InvalidEmail() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with short password")
    void testRegisterUser_ShortPassword() throws Exception {
        // Arrange
        validRegistrationRequest.setPassword("12345"); // Too short

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with blank username")
    void testRegisterUser_BlankUsername() throws Exception {
        // Arrange
        validRegistrationRequest.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with blank email")
    void testRegisterUser_BlankEmail() throws Exception {
        // Arrange
        validRegistrationRequest.setEmail("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Registration: Should fail with blank password")
    void testRegisterUser_BlankPassword() throws Exception {
        // Arrange
        validRegistrationRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    // ===========================
    // LOGIN TESTS
    // ===========================

    @Test
    @DisplayName("Login: Should successfully login with valid credentials")
    void testLoginUser_Success() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("test-jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        // Verify
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    @DisplayName("Login: Should fail with invalid credentials")
    void testLoginUser_InvalidCredentials() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));

        // Verify
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Login: Should fail with blank username")
    void testLoginUser_BlankUsername() throws Exception {
        // Arrange
        validLoginRequest.setUsername("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login: Should fail with blank password")
    void testLoginUser_BlankPassword() throws Exception {
        // Arrange
        validLoginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login: Should fail with null username")
    void testLoginUser_NullUsername() throws Exception {
        // Arrange
        validLoginRequest.setUsername(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login: Should fail with null password")
    void testLoginUser_NullPassword() throws Exception {
        // Arrange
        validLoginRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login: Should fail when user not found after authentication")
    void testLoginUser_UserNotFoundAfterAuth() throws Exception {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
}
