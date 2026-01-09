### **UserDetails and UserDetailsService in Spring Security**

In **Spring Security**, `UserDetails` and `UserDetailsService` are core interfaces used for authentication and user management.

---

## **1. UserDetails**
`UserDetails` is an interface that represents a user in the Spring Security framework. It contains user-related information such as:

- **Username**
- **Password** (usually encoded)
- **Granted Authorities (Roles & Permissions)**
- **Account Status (enabled/disabled, expired, locked, etc.)**

### **Example of Custom `UserDetails` Implementation**
```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private boolean isEnabled;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, boolean isEnabled,
                             Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
```

---

## **2. UserDetailsService**
`UserDetailsService` is a **service interface** that is responsible for loading user-specific data during authentication. It provides the method:

```java
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
```

It is used by **Spring Security's authentication mechanism** to fetch user details from a database, an API, or any other data source.

### **Example of Custom `UserDetailsService` Implementation**
```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Simulating a user fetched from a database
        if ("admin".equals(username)) {
            return new CustomUserDetails("admin", "$2a$10$7QG1z5VxQW5M7E5oUzr22OZoP7lF8hOJClFbV9Q6ZI2HfgvFN8L2q", true,
                    List.of(() -> "ROLE_ADMIN")); // Password is BCrypt encoded
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
```

---

## **3. How It Works in Spring Security**
When a user tries to authenticate:
1. **Spring Security** calls `loadUserByUsername(username)` from `UserDetailsService`.
2. This method fetches user details from the database or any other source.
3. If the user exists, it returns a `UserDetails` object, which Spring Security uses to authenticate the user.
4. If the user does not exist, it throws a `UsernameNotFoundException`.

---

## **4. Spring Security Configuration Example**
To use `UserDetailsService` in your Spring Security configuration:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(List.of(authProvider));
    }
}
```

---

## **5. Summary**
| Component          | Description |
|-------------------|-------------|
| **UserDetails** | Represents a user in Spring Security (username, password, roles, account status). |
| **UserDetailsService** | Loads user details from a database (or other sources) based on the username. |
| **Authentication Process** | Spring Security calls `loadUserByUsername()` to fetch the user and authenticate them. |

---

This is how `UserDetails` and `UserDetailsService` work together in **Spring Security authentication**. 