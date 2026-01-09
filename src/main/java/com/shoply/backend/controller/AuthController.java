package com.shoply.backend.controller;

import com.shoply.backend.model.AppRole;
import com.shoply.backend.model.Role;
import com.shoply.backend.model.User;
import com.shoply.backend.repositories.RoleRepository;
import com.shoply.backend.repositories.UserRepository;
import com.shoply.backend.security.jwt.JwtUtils;
import com.shoply.backend.security.request.LoginRequest;
import com.shoply.backend.security.request.SignupRequest;
import com.shoply.backend.security.response.MessageResponse;
import com.shoply.backend.security.response.UserInfoResponse;
import com.shoply.backend.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication;
        try {
            // Create an authentication token using the username and password
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), // Username from request
                            loginRequest.getPassword()  // Password from request
                    ));
        } catch (AuthenticationException exception) {
            // Handle authentication failure (e.g., wrong username or password)

            // Create a map to store error response details
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials"); // Custom error message
            map.put("status", false); // Status flag indicating failure

            // Return HTTP 404 status with error message (can be changed to 401 if needed)
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        // If authentication is successful, store authentication details in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extract user details from the authenticated principal
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate a JWT Cookie using the authenticated user's details
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // Extract user roles (authorities) and convert them into a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority()) // Get authority name (e.g., ROLE_USER)
                .collect(Collectors.toList());


        // changed as implemented cookies based method
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),       // id
                userDetails.getUsername(), // Username
                roles,                     // Roles list
                jwtCookie.toString()
        );

        // Return a successful response with HTTP 200 status and the response object
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                jwtCookie.toString())
                .body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "seller":
                        Role modRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // Authentication object is passed automatically
    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails (Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract user roles (authorities) and convert them into a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority()) // Get authority name (e.g., ROLE_USER)
                .collect(Collectors.toList());


        // changed as implemented cookies based method
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),       // id
                userDetails.getUsername(), // Username
                roles                      // Roles list
        );

        return ResponseEntity.ok().body(response);
    }


    // This is method that we are using to invalidate the cookies since there is no other way to do that, tha only way is when the time is expired.
    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(new MessageResponse("You've been signed out"));
    }

}
