# Let's explore the security config file in our project.


```java
//WebSecurityConfig
package com.shoply.backend.security;

import com.shoply.backend.security.jwt.AuthEntryPointJwt;
import com.shoply.backend.security.jwt.AuthTokenFilter;
import com.shoply.backend.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/api/admin/**").permitAll()
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
    }
}

```

This `WebSecurityConfig` class is responsible for configuring security settings in a Spring Boot application using Spring Security. It defines authentication and authorization mechanisms, JWT handling, and other security-related configurations. Let's go through it step by step.


## Breakdown of the above Code application in Details.


## **1. Class-Level Annotations**
```java
@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
```
- **`@Configuration`**: Marks this class as a configuration class, meaning Spring will recognize it and process its beans.
- **`@EnableWebSecurity`**: Enables Spring Security and allows customization of security settings.
- **`@EnableMethodSecurity`** (commented out): If enabled, this allows method-level security (like `@PreAuthorize` or `@Secured` on methods).

---

## **2. Dependencies Injection**
```java
@Autowired
UserDetailsServiceImpl userDetailsService;

@Autowired
private AuthEntryPointJwt unauthorizedHandler;
```
- **`UserDetailsServiceImpl`**: Custom service that loads user-specific data during authentication.
- **`AuthEntryPointJwt`**: Handles unauthorized access errors, typically responding with `401 Unauthorized`.

---

## **3. JWT Authentication Filter Bean**
```java
@Bean
public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
}
```
- Creates a bean of `AuthTokenFilter`, which is a custom filter for handling JWT authentication.

---

## **4. Authentication Provider Configuration**
```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
}
```
- **`DaoAuthenticationProvider`**: This provider handles user authentication using a custom `UserDetailsServiceImpl`.
- **`setUserDetailsService(userDetailsService)`**: Assigns the custom user details service for authentication.
- **`setPasswordEncoder(passwordEncoder())`**: Uses BCrypt password encoding to securely store and verify passwords.

---

## **5. Authentication Manager Configuration**
```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
}
```
- Retrieves and provides the `AuthenticationManager`, which manages authentication processes in Spring Security.

---

## **6. Password Encoder Bean**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- **BCryptPasswordEncoder**: Ensures secure password hashing before storing passwords in the database.

---

## **7. Security Filter Chain Configuration**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())  // Disables CSRF protection (useful for APIs)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session for JWT
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/auth/**").permitAll() // Public authentication endpoints
                            .requestMatchers("/v3/api-docs/**").permitAll() // API documentation
                            .requestMatchers("/api/admin/**").permitAll() // Admin endpoints (should be secured in production)
                            .requestMatchers("/api/public/**").permitAll() // Public API endpoints
                            .requestMatchers("/swagger-ui/**").permitAll() // Swagger UI
                            .requestMatchers("/api/test/**").permitAll() // Test endpoints
                            .requestMatchers("/images/**").permitAll() // Public images
                            .anyRequest().authenticated() // All other requests require authentication
            );

    http.authenticationProvider(authenticationProvider()); // Uses custom authentication provider

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class); // Adds JWT filter before username-password authentication

    return http.build();
}
```
### **Key points:**
- **CSRF Disabled**: Since it's an API, CSRF protection is disabled.
- **Exception Handling**: Uses `AuthEntryPointJwt` to return unauthorized error responses.
- **Session Policy**: Uses **stateless** authentication (since JWT tokens manage authentication).
- **Authorization Rules**:
    - Allows unauthenticated access to public endpoints (`/api/auth/**`, `/swagger-ui/**`, etc.).
    - Requires authentication for all other requests.
- **JWT Authentication Filter**: Adds `AuthTokenFilter` before the standard username-password authentication filter.

---

## **8. Web Security Customizer (Ignoring Some Paths)**
```java
@Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    return (web -> web.ignoring().requestMatchers("/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**"));
}
```
- Configures Spring Security to ignore specific paths, mostly related to Swagger API documentation.

---

## **Summary**
This `WebSecurityConfig` class:
1. Configures JWT-based authentication and exception handling.
2. Defines a password encoder using **BCrypt**.
3. Uses **DaoAuthenticationProvider** with a custom user service.
4. Specifies which endpoints are public and which require authentication.
5. Disables CSRF (useful for stateless APIs).
6. Registers a **JWT authentication filter** before Spring Security's default authentication filter.

Would you like a deeper dive into any specific part? 