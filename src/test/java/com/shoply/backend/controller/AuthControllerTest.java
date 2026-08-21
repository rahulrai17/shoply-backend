package com.shoply.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoply.backend.security.request.LoginRequest;
import com.shoply.backend.security.request.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ====================================================================================
 * INTEGRATION TEST: AuthController Authentication & Authorization Flow
 * ====================================================================================
 * Uses MockMvc and Spring Boot context with in-memory H2 database to verify:
 * 1. User registration endpoint (/api/auth/signup).
 * 2. User authentication endpoint (/api/auth/signin).
 * 3. Validation failures on bad credentials.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * TEST 1: Happy Path - Register New User Account
     */
    @Test
    @DisplayName("Should successfully register a new user account with default ROLE_USER")
    void registerUser_Success() throws Exception {
        // [ARRANGE] Construct signup payload
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser_new");
        signupRequest.setEmail("testuser_new@shoply.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setRole(Collections.singleton("user"));

        // [ACT & ASSERT] Perform POST and verify HTTP 200 OK
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    /**
     * TEST 2: Negative Test - Reject Duplicate Username Registration
     */
    @Test
    @DisplayName("Should return 400 Bad Request when registering duplicate username")
    void registerUser_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // [ARRANGE] Duplicate username payload (user1 is pre-seeded)
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("user1");
        signupRequest.setEmail("unique_email@shoply.com");
        signupRequest.setPassword("Password123!");

        // [ACT & ASSERT] Expect HTTP 400 BAD_REQUEST
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    /**
     * TEST 3: Happy Path - Authenticate User & Issue JWT Cookie
     */
    @Test
    @DisplayName("Should authenticate user credentials and return HTTP-Only JWT Cookie")
    void authenticateUser_Success() throws Exception {
        // [ARRANGE] Valid credentials for seeded user1
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user1");
        loginRequest.setPassword("password1");

        // [ACT & ASSERT] Expect HTTP 200 OK and Set-Cookie header
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    /**
     * TEST 4: Negative Test - Reject Invalid Password Credentials
     */
    @Test
    @DisplayName("Should return 401 Unauthorized when password is invalid")
    void authenticateUser_BadCredentials_ReturnsUnauthorized() throws Exception {
        // [ARRANGE] Incorrect password for user1
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user1");
        loginRequest.setPassword("wrongpassword");

        // [ACT & ASSERT] Expect HTTP 401 UNAUTHORIZED and Bad credentials message
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }
}
