## InMemoryUserDetailsManager

### **What is `InMemoryUserDetailsManager` in Spring Security?**
`InMemoryUserDetailsManager` is a concrete implementation of the `UserDetailsService` interface provided by Spring Security. It allows you to store user credentials (such as usernames, passwords, and roles) **in-memory** rather than in a database or external system.

### **Purpose of `InMemoryUserDetailsManager`**
- It is primarily used for **testing** or for **simple applications** where setting up a database or external user store is unnecessary.
- It allows defining users, passwords, and roles directly in the application’s configuration.
- It helps simplify the initial setup of authentication and authorization in a Spring Security application.

---

## **How `InMemoryUserDetailsManager` Works**
### 1. **Implements `UserDetailsService`**
`InMemoryUserDetailsManager` implements the `UserDetailsService` interface, which defines a single method:
```java
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
```
- This method is called by Spring Security during the authentication process to fetch user details.
- `InMemoryUserDetailsManager` retrieves the user details from its internal map of users.

---

### 2. **How to Create an `InMemoryUserDetailsManager`**
You define users directly in the configuration using `User.withDefaultPasswordEncoder()` or `PasswordEncoderFactories.createDelegatingPasswordEncoder()`.

---

### ✅ **Example 1: Basic Example Using `InMemoryUserDetailsManager`**
This example shows how to create users and define roles directly in the `SecurityFilterChain`:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user1 = User
                .withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user1, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Secure encoding for passwords
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/user/**").hasRole("USER")
                    .anyRequest().authenticated()
            )
            .formLogin(withDefaults()) // Enable form-based authentication
            .httpBasic(); // Enable HTTP Basic Authentication

        return http.build();
    }
}
```

---

### ✅ **Example 2: Adding Users Dynamically**
You can also add or modify users dynamically at runtime:

```java
@Autowired
private InMemoryUserDetailsManager userDetailsManager;

public void addNewUser(String username, String password) {
    UserDetails newUser = User.withUsername(username)
                              .password(passwordEncoder().encode(password))
                              .roles("USER")
                              .build();
    userDetailsManager.createUser(newUser);
}

public void updateUserPassword(String username, String newPassword) {
    UserDetails updatedUser = User.withUsername(username)
                                  .password(passwordEncoder().encode(newPassword))
                                  .roles("USER")
                                  .build();
    userDetailsManager.updateUser(updatedUser);
}
```

---

### ✅ **Example 3: Deleting and Managing Users**
You can delete or modify users using `InMemoryUserDetailsManager` methods:

```java
public void deleteUser(String username) {
    userDetailsManager.deleteUser(username);
}

public boolean userExists(String username) {
    return userDetailsManager.userExists(username);
}
```

---

## **How Spring Security Uses `InMemoryUserDetailsManager`**
### 🔎 **Authentication Flow**
1. A user sends login credentials.
2. Spring Security calls `userDetailsService.loadUserByUsername(username)` to fetch user details.
3. `InMemoryUserDetailsManager` checks the internal map for the user.
4. If the user exists:
    - Spring Security verifies the password using the `PasswordEncoder`.
    - If successful, it creates an `Authentication` object.
5. If the user does not exist or the password is wrong, authentication fails.

---

## **Best Practices and Considerations**
✅ **Use Cases:**
- Best for **testing** or **small internal tools**.
- Useful when you don’t need to persist user data across restarts.

❌ **Limitations:**
- User data is stored **in memory** – not persistent across restarts.
- Not suitable for large-scale applications or production use.

✅ **Security Considerations:**
- Always use a secure `PasswordEncoder` like `BCryptPasswordEncoder` instead of `NoOpPasswordEncoder` for real applications.

---

## **Common Errors and Fixes**
| Issue | Cause | Fix |
|-------|-------|-----|
| `UsernameNotFoundException` | User not found in memory | Ensure username is correctly defined and matched |
| `PasswordEncoder error` | No `PasswordEncoder` bean defined | Define a `PasswordEncoder` bean using `BCryptPasswordEncoder` |
| `IllegalArgumentException: There is no PasswordEncoder mapped for the id "null"` | Missing password encoding | Use `passwordEncoder().encode(password)` when defining users |
| `403 Forbidden` on role-based endpoints | Incorrect role or no authorization | Ensure roles are correctly defined and matched in `authorizeRequests` |

---

## **When to Use `InMemoryUserDetailsManager`**
✅ Good for:
- Quick prototypes and MVPs.
- Demos or POCs.
- Testing Spring Security configuration.

❌ Avoid for:
- Production systems.
- Applications requiring dynamic user management and persistence.

---

## **Summary**
1. `InMemoryUserDetailsManager` allows defining user details directly in memory.
2. Ideal for simple or temporary setups.
3. Easy to configure using `User.withUsername()` and `roles()`.
4. Supports dynamic user management at runtime.
5. Always use secure password encoders in real-world applications.