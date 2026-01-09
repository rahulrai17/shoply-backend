# how to implement spring security in you own way.

If you want to **override the default behavior** of Spring Security and define your own security rules, you need to create a **custom `SecurityFilterChain` bean** in your application.

Let me walk you through the **step-by-step process** to override the default behavior of Spring Security:

---

## ✅ **Step 1: Add Spring Security to Your Project**
Make sure you have added the Spring Security dependency in your `pom.xml` (for Maven) or `build.gradle` (for Gradle).

### 👉 **For Maven:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 👉 **For Gradle:**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

---

## ✅ **Step 2: Create a Custom Security Configuration Class**
Create a new Java class named `SecurityConfig` (or any name you prefer) in your project.

### 📁 **Project Structure Example:**
```
src
└── main
    └── java
        └── com
            └── example
                └── security
                    └── SecurityConfig.java
```

### 👉 **Example Code:**
```java
package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Define which endpoints are public and which require authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll() // Allow public access to "/public"
                .requestMatchers("/admin/**").hasRole("ADMIN") // Restrict to ADMIN role
                .requestMatchers("/user/**").hasRole("USER") // Restrict to USER role
                .anyRequest().authenticated() // All other requests need authentication
            )
            // 2. Use form-based login
            .formLogin(form -> form
                .loginPage("/login") // Custom login page
                .permitAll()
            )
            // 3. Enable HTTP Basic authentication (for API testing)
            .httpBasic(Customizer.withDefaults())
            // 4. CSRF protection (enabled by default, but you can disable if needed)
            .csrf(csrf -> csrf.disable()); // Optional: disable CSRF for APIs

        return http.build(); // Build the security filter chain
    }
}
```

---

## ✅ **Step 3: Create a Custom Login Page (Optional)**
If you want to use a custom login page, create an HTML file like this:

### 📁 **Example Location:**
```
src/main/resources/templates/login.html
```

### 👉 **Example Code:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Login Page</title>
</head>
<body>
    <h2>Login</h2>
    <form th:action="@{/login}" method="post">
        <div>
            <label>Username: <input type="text" name="username"/></label>
        </div>
        <div>
            <label>Password: <input type="password" name="password"/></label>
        </div>
        <div>
            <button type="submit">Login</button>
        </div>
        <div th:if="${param.error}">
            Invalid username or password.
        </div>
    </form>
</body>
</html>
```

---

## ✅ **Step 4: Create Custom Users in Memory (Optional)**
If you want to define custom users for testing, create a `UserDetailsService` bean like this:

### 👉 **Example Code:**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();

        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin123")
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}
```

---

## ✅ **Step 5: Create Sample Controller (Optional)**
Create a controller to test your security settings.

### 👉 **Example Code:**
```java
package com.example.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public/hello")
    public String publicEndpoint() {
        return "Hello from public endpoint!";
    }

    @GetMapping("/user/hello")
    public String userEndpoint() {
        return "Hello from user endpoint!";
    }

    @GetMapping("/admin/hello")
    public String adminEndpoint() {
        return "Hello from admin endpoint!";
    }
}
```

---

## ✅ **Step 6: Application Properties (Optional)**
You can configure some Spring Security settings directly in `application.properties`:

### 👉 **Example Code:**
```properties
# Custom login page
spring.security.user.name=user
spring.security.user.password=password

# H2 console access (if using H2 database)
spring.h2.console.enabled=true
spring.security.oauth2.client.registration.google.client-id=<your-client-id>
```

---

## ✅ **Step 7: Test the Application**
### 🚀 **Start the Spring Boot Application:**
```bash
mvn spring-boot:run
```

### 🏆 **Test the Endpoints:**
| URL | Description | Expected Result |
|------|-------------|----------------|
| `http://localhost:8080/public/hello` | Public endpoint | Accessible without login |
| `http://localhost:8080/user/hello` | User-only endpoint | Requires authentication as USER |
| `http://localhost:8080/admin/hello` | Admin-only endpoint | Requires authentication as ADMIN |
| `http://localhost:8080/login` | Custom login page | Should appear when authentication is needed |

---

## 🚀 **How It Works**
1. Spring Boot auto-configuration will detect the custom `SecurityFilterChain` and **disable the default behavior**.
2. Your custom configuration will handle:
    - Authentication rules
    - Authorization rules
    - Custom login form
    - HTTP Basic authentication
    - CSRF configuration

---

## ✅ **How to Customize Further**
👉 To define a custom user store (like a database) → Replace `UserDetailsService` with a custom implementation.  
👉 To add JWT-based authentication → Replace form-based login with a `BearerTokenAuthenticationFilter`.  
👉 To add OAuth2 authentication → Configure `OAuth2LoginConfigurer` in the `HttpSecurity` object.

---

## 🎯 **Summary**
✅ Create a `SecurityFilterChain` bean → Overrides default behavior  
✅ Define custom login + logout handling  
✅ Set up in-memory users (or use a database)  
✅ Secure endpoints using `authorizeHttpRequests()`

---

### 😎 **Would you like to explore more about JWT or OAuth2 setup?**