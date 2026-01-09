# Implementation for Spring Security and JWT Authentication

## Step 1 : Lets JWT we have Three main file that we create :
- First We need to create a security.jwt package.
- Next we Make few files There :
    - `JwtUtils` : This file will contain utility methods for generating, parsing and validating JWTs.
        - Generating includes generating a token from a username, validating a JWT, and extracting the username from a token.
    - `AuthTokenFilter` : Filters incoming requests to check for a valid JWT in the header, setting the authentication context if the token is valid.
        - Extracts JWT from request, header, validates it, and configures the spring security context with user details if the token is valid.
    - `AuthEntryPointJwt` : Provides custom handling for unauthorized requests, typically when authentication is requires but not supplied or valid.
        - When an unauthorized request is detected,it logs the error and returns a JSON response with an error message, status code, and the path attempted.
   
  - These three are the key files for JWT Authentication.
  - You can check the files in the project packages.


## **1. JwtUtils (JWT Utility Class)**
This class is responsible for **creating, parsing, and validating JWTs**.

### **Key Functions:**
| Method | Description |
|--------|-------------|
| `getJwtFromHeader(HttpServletRequest request)` | Extracts JWT from the `Authorization` header. |
| `generateTokenFromUsername(UserDetails userDetails)` | Creates a JWT for a user. |
| `getUserNameFromJwtToken(String token)` | Extracts username from a JWT. |
| `validateJwtToken(String authToken)` | Validates the JWT's format, expiration, and signature. |
| `key()` | Generates a `SecretKey` from the `jwtSecret`. |

### **Flow of JWT Generation & Validation:**
1. **User logs in** → Backend generates a JWT using `generateTokenFromUsername()`.
2. **Client stores JWT** (usually in localStorage or cookies).
3. **Client makes requests** with JWT in the `Authorization` header.
4. **Backend validates the token** using `validateJwtToken()`.

### **Example of JWT Generation**
```java
String token = jwtUtils.generateTokenFromUsername(userDetails);
System.out.println("Generated JWT: " + token);
```

---

## **2. AuthTokenFilter (JWT Authentication Filter)**
This class is a **filter that runs for every request**. It extracts the JWT, validates it, and sets authentication in **Spring Security’s context**.

### **How It Works:**
1. Extracts the JWT from the request (`parseJwt()`).
2. Validates the token (`jwtUtils.validateJwtToken(jwt)`).
3. Retrieves the username from JWT (`jwtUtils.getUserNameFromJwtToken(jwt)`).
4. Loads user details (`userDetailsService.loadUserByUsername(username)`).
5. Creates an `Authentication` object and sets it in `SecurityContextHolder`.

### **Key Code Blocks**
#### **Extracting JWT from the Request Header**
```java
private String parseJwt(HttpServletRequest request) {
    String jwt = jwtUtils.getJwtFromHeader(request);
    logger.debug("Extracted JWT: {}", jwt);
    return jwt;
}
```

#### **Setting Authentication in Security Context**
```java
UserDetails userDetails = userDetailsService.loadUserByUsername(username);
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

SecurityContextHolder.getContext().setAuthentication(authentication);
```

### **What Happens?**
- If the token is **valid**, Spring Security knows which user is making the request.
- If the token is **invalid**, the request is **not authenticated**.

---

## **3. AuthEntryPointJwt (Handling Unauthorized Requests)**
This class **handles authentication failures**, ensuring proper JSON responses when access is denied.

### **How It Works:**
1. If authentication fails (e.g., missing/invalid token), Spring calls `commence()`.
2. This method sends a **401 Unauthorized** response in JSON format.

### **Key Code Blocks**
#### **Handling Unauthorized Access**
```java
@Override
public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException, ServletException {
    logger.error("Unauthorized error: {}", authException.getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    body.put("error", "Unauthorized");
    body.put("message", authException.getMessage());
    body.put("path", request.getServletPath());

    new ObjectMapper().writeValue(response.getOutputStream(), body);
}
```

### **What Happens?**
- If a request has an **invalid/missing JWT**, Spring calls this method.
- A structured JSON response is sent back with **error details**.

---

## **How These Components Work Together**
1. **User logs in** → A JWT is generated (`JwtUtils.generateTokenFromUsername()`).
2. **Client sends a request** with JWT in `Authorization` header.
3. **`AuthTokenFilter` runs** for every request:
    - Extracts JWT
    - Validates it using `JwtUtils.validateJwtToken()`
    - Loads user details and sets authentication in `SecurityContextHolder`
4. **If the JWT is invalid/missing**, `AuthEntryPointJwt` returns a **401 Unauthorized** error.

---

## **Summary**
| Component | Purpose |
|-----------|---------|
| **JwtUtils** | Handles JWT generation, validation, and parsing. |
| **AuthTokenFilter** | Intercepts requests, extracts JWTs, validates them, and sets authentication. |
| **AuthEntryPointJwt** | Handles authentication failures by sending a structured JSON response. |


## Let's see the code of the three files and understand the use and also check if we can modify them based on our requirement.

- JwtUtils - This is when you are working with Bearer mode. You can also modify this to implement cookie based approach.
```java
// JwtUtils
package com.shoply.backend.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Extracts the username from the JWT.
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    // Generates a JWT for a given user.
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    // Extracts the username from the JWT.
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Creates a Key object from the base64-encoded jwtSecret.
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Validates the JWT to ensure it's well-formed, not expired, and properly signed.
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
```
- For cookie we will update this file :

```java
// JwtUtils.java
package com.shoply.backend.security.jwt;

import com.shoply.backend.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.file.attribute.UserPrincipal;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;


    // Extract JWT from cookies
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();
        return cookie;
    }

    // Generates a JWT for a given user.
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    // Extracts the username from the JWT.
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Creates a Key object from the base64-encoded jwtSecret.
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Validates the JWT to ensure it's well-formed, not expired, and properly signed.
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
```

- We have made few changes here 
  - changes the method `getJwtFromHeader` to `getJwtFromCookies`.
  - Also added Helping method : `generateJwtCookie`.
  - Also changed the parameter for `getUserNameFromJwtToken`.
  - Also, as `getJwtFromHeader` is called form `AuthTokenFilter` we will change it ti `getJwtFromCookie`.

- `AuthTokenFilter` :
```java
// AuthTokenFilter.java
package com.shoply.backend.security.jwt;


import com.shoply.backend.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    // here we changed getJwtFromHeader TO getJwtFromCookie
    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        // String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}
```

- Also, we need to make changes to the AuthController as there we are `generateTokenFromUsername` for bearer mehtod.
- when we are using cookies, it has this `generateJwtCookie` method which already calls the `generateTokenFromUsername`.
- Therefore, we will directly call the `generateJwtCookie` in the auth controller.

```java
// AuthController.java
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        // Generate a JWT token using the authenticated user's details
        // String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        ResponseCookie jwtToken = jwtUtils.generateJwtCookie(userDetails);

        // Extract user roles (authorities) and convert them into a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority()) // Get authority name (e.g., ROLE_USER)
                .collect(Collectors.toList());

        // Create a response object containing user details and token <- for bearer way
//        UserInfoResponse response = new UserInfoResponse(
//                userDetails.getId(),       // id
//                userDetails.getUsername(), // Username
//                roles,                     // Roles list
//                jwtToken                   // JWT token
//        );

        // changed as implemented cookies based method 
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),       // id
                userDetails.getUsername(), // Username
                roles,                     // Roles list
                jwtCookie.toSting()        // Jwt Token
        );

        // Return a successful response with HTTP 200 status and the response object
        // return ResponseEntity.ok(response); <- for Bearer way
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


}

```

- Here we have also change the userInfoResponse Object and removed the JwtToken option for this to work we hava created a construction in the `UserResponseInfo` class.
- Also we have change the `jwtToken` name to `jwtCookie` and also updated the response statement. As we need to put it in the header.
- So to use either one you are needed to do these changes. Also for bearer mode code is available in `10_SpringSecurityExample.md` file
- We also made some more changes like 
  - `jwtCookie.toSting()` - we added this to the userInfoResponse to return the jwt Token back in the response.
  - You can remove it or add it its upto you.

## Working on new api ("/user) :
- This api will be used for showing the User detials. we will just add it in the `AuthContoller.java`

```java
// AuthContorller.java

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
```
- Here we can see that we need an authentication object passed as a parameter.
- That will be passed automatically by itself.
- This method will return user details based on the session. Like which user is logged in.


## Now let's create a ("/singout") :
- Now signout is important because we need to need a way to remove the user session.
- currently we only have time expiry for token
- By creating this api we will allow the cookie to be empty which will remove the token.

```java
// AuthController.java
@PostMapping("/signout")
public ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
        cookie.toString())
        .body(new MessageResponse("You've been signed out"));
        }
```

- Also we will need to create the helper method : `getCleanJwtCookie` 

```java
// JwtUtils.java
public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
        .path("/api").
        build();
        return cookie;
        }
```
- Here we can see we have set the `jwtCookie` null therefore, the access will end without `jwt`.