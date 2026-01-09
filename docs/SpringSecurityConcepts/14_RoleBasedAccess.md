##  Role-Based Access Control (RBAC) in Spring Security


###  **What is Role-Based Access Control (RBAC)?**
Role-Based Access Control (RBAC) is a security model where access to resources or actions is granted based on a user's **role**. A role represents a specific set of permissions or capabilities.

###  **Key Concepts of RBAC:**
1. **User:** A person or service trying to access the application.
2. **Role:** A logical grouping of permissions.
    - Example roles: `USER`, `ADMIN`, `MANAGER`
3. **Permission:** A specific action that a user can perform.
    - Example permissions: `READ`, `WRITE`, `DELETE`
4. **Resource:** A secured part of the application (e.g., API endpoint, web page).

###  **Example:**
| Role | Permissions | Accessible Resources |
|------|-------------|----------------------|
| `USER` | `READ` | `/user/*` |
| `ADMIN` | `READ`, `WRITE`, `DELETE` | `/admin/*` |

In Spring Security:
- Roles are represented by the `GrantedAuthority` interface.
- Permissions are assigned to roles.
- Roles are prefixed with `"ROLE_"` internally by Spring Security.

---

##  **How Spring Security Works with RBAC**
Spring Security handles RBAC through:
1. **Authentication** – Verifying who the user is.
2. **Authorization** – Determining what the user can do based on their roles.

###  **Authentication Flow:**
1. User submits login credentials.
2. Spring Security calls the `UserDetailsService` to load user details.
3. Credentials are verified using a `PasswordEncoder`.
4. If successful, an `Authentication` object is created and stored in the `SecurityContext`.

###  **Authorization Flow:**
1. User sends a request to a secured endpoint.
2. Spring Security checks the `SecurityContext` for the user's `Authentication` object.
3. Spring checks if the user has the required role using:
    - URL-based security rules (`authorizeHttpRequests`)
    - Method-based rules (`@PreAuthorize`)
    - Role hierarchy (if defined)

---

##  **1. Define Roles and Users**
The simplest way to define roles is by using `InMemoryUserDetailsManager` or `UserDetailsService`.

###  **Example 1: Using `InMemoryUserDetailsManager`**
You can define roles directly in the configuration:

```java
@Bean
public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();

    UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, admin);
}
```

###  **Example 2: Using `UserDetailsService` and Database**
You can load user details from a database using `JdbcUserDetailsManager`:

```java
@Bean
public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
    UserDetails user = User.withUsername("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
    manager.createUser(user);

    return manager;
}
```

###  **Example 3: Using a Custom `UserDetailsService`**
You can create your own `UserDetailsService` to load users from a database or external service:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
```

---

##  **2. Configure URL-Based Role Access**
You can define role-based access using `HttpSecurity`.

###  **Example: URL-Based Authorization**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN") // Only ADMIN can access
            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // USER or ADMIN can access
            .anyRequest().authenticated() // All other endpoints require authentication
        )
        .formLogin(withDefaults()) // Enable form-based login
        .httpBasic(); // Enable HTTP Basic Authentication

    return http.build();
}
```

### **Example: More Granular Access**
You can define more specific patterns:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/user/**").hasAnyRole("USER", "ADMIN")
    .requestMatchers("/public/**").permitAll()
    .anyRequest().authenticated()
)
```

---

##  **3. Method-Level Role-Based Access**
Instead of securing endpoints at the URL level, you can secure individual methods using annotations.

###  **Enable Method-Level Security:**
```java
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
}
```

###  **Using `@PreAuthorize` (Preferred)**
Use SpEL (Spring Expression Language) for flexibility:

```java
@PreAuthorize("hasRole('ADMIN')")
public String getAdminData() {
    return "Admin data";
}

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public String getUserData() {
    return "User data";
}
```

###  **Using `@Secured`**
Use `@Secured` when you don't need SpEL:

```java
@Secured("ROLE_ADMIN")
public String adminMethod() {
    return "Admin data";
}
```

###  **Using `@RolesAllowed` (JEE-Compatible)**
Similar to `@Secured`, but JEE-compatible:

```java
@RolesAllowed("ROLE_ADMIN")
public String adminMethod() {
    return "Admin data";
}
```

---

##  **4. Define Role Hierarchy**
You can define a role hierarchy where higher roles inherit lower-role permissions:

###  **Example: Defining a Role Hierarchy**
```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    return hierarchy;
}
```

**Effect:**
- `ROLE_ADMIN` inherits `ROLE_USER` permissions automatically.

---

## 📑 **5. Handle Permission-Based Access (Optional)**
Instead of just roles, you can define fine-grained permissions:

### **Example: Permission-Based Access**
```java
@PreAuthorize("hasAuthority('PERM_READ')")
public String readData() {
    return "Read data";
}
```

Instead of `roles`, Spring Security will check for `authorities` in the `Authentication` object.

---

##  **6. Testing the Role-Based Access**
###  **Test as Admin:**
```bash
curl -u admin:admin http://localhost:8080/admin
```

###  **Test as User:**
```bash
curl -u user:password http://localhost:8080/user
```

###  **Test Unauthorized Access:**
```bash
curl -u user:password http://localhost:8080/admin
```

---

##  **Complete Example Structure**
✅ User storage ➡️ `UserDetailsService`  
✅ Password encoding ➡️ `BCryptPasswordEncoder`  
✅ URL-based security ➡️ `HttpSecurity`  
✅ Method-based security ➡️ `@PreAuthorize`  
✅ Role hierarchy ➡️ `RoleHierarchyImpl`

---

## 🏆 **Best Practices**
✔️ Always use `BCryptPasswordEncoder`  
✔️ Define clear role hierarchies  
✔️ Use `@PreAuthorize` for flexibility  
✔️ Keep role definitions consistent  
✔️ Follow the principle of **least privilege**

