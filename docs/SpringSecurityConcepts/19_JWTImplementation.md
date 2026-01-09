## How do we implement JWT Authentication in java spring boot application (Example Application)

## **1. Project Structure Overview**
Here's the complete folder structure of the Spring Boot JWT project:

```
src/main/java/com/example
├── controller
│   └── AuthController.java
├── model
│   └── User.java
├── repository
│   └── UserRepository.java
├── security
│   ├── JwtFilter.java
│   └── SecurityConfiguration.java
├── service
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
├── util
│   └── JwtUtil.java
└── Application.java
```

---

## **2. Explanation of Each File**
### ✅ **2.1. `User.java` – User Entity (Model)**
**📁 Location:** `src/main/java/com/example/model/User.java`

### **Purpose:**
- Represents the user object in the database.
- JPA creates a table based on this entity.
- Contains user credentials like `username`, `password`, and `role`.

### **Why It's Needed:**
- User data is stored in a relational database.
- Authentication and authorization depend on user details.

---

### **Example Code:**
```java
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String role;
}
```

### **Explanation:**
- `@Entity` – Marks it as a JPA entity.
- `@Table(name = "users")` – Specifies the table name.
- `@Id` – Marks the primary key.
- `@GeneratedValue` – Auto-generates ID values.
- `@Column` – Maps fields to table columns.

---

### ✅ **2.2. `UserRepository.java` – Repository Interface**
**📁 Location:** `src/main/java/com/example/repository/UserRepository.java`

### **Purpose:**
- Handles database operations for `User` entity.
- Fetches user details during authentication.

### **Why It's Needed:**
- The `UserDetailsService` implementation will need to fetch user details from the database.

---

### **Example Code:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### **Explanation:**
- `JpaRepository` – Provides CRUD methods like `save`, `findById`, etc.
- `findByUsername` – Custom query method to fetch user by username.

---

### ✅ **2.3. `CustomUserDetails.java` – Custom UserDetails Implementation**
**📁 Location:** `src/main/java/com/example/service/CustomUserDetails.java`

### **Purpose:**
- Converts `User` entity into Spring Security's `UserDetails` object.
- `UserDetails` is used to manage user authentication.

### **Why It's Needed:**
- Spring Security expects `UserDetails` object for authentication.

---

### **Example Code:**
```java
public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
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
        return true;
    }
}
```

---

### ✅ **2.4. `CustomUserDetailsService.java` – Custom UserDetailsService Implementation**
**📁 Location:** `src/main/java/com/example/service/CustomUserDetailsService.java`

### **Purpose:**
- Loads user data from the database during authentication.
- Converts `User` entity to `UserDetails`.

### **Why It's Needed:**
- Spring Security requires a `UserDetailsService` implementation to handle authentication.

---

### **Example Code:**
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(user);
    }
}
```

---

### ✅ **2.5. `JwtUtil.java` – JWT Utility Class**
**📁 Location:** `src/main/java/com/example/util/JwtUtil.java`

### **Purpose:**
- Generates JWT tokens.
- Parses and validates tokens.

### **Why It's Needed:**
- JWT generation and validation must be handled manually.

---

### ✅ **2.6. `JwtFilter.java` – JWT Authentication Filter or `AuthTokenFilter`**
**📁 Location:** `src/main/java/com/example/security/JwtFilter.java`

### **Purpose:**
- Intercepts requests.
- Extracts and validates JWT tokens.
- Authenticates user based on JWT claims.

### **Why It's Needed:**
- Spring Security requires a filter to process tokens on every request.

---

### ✅ **2.7. `SecurityConfiguration.java` – Security Configuration**
**📁 Location:** `src/main/java/com/example/security/SecurityConfiguration.java`

### **Purpose:**
- Configures Spring Security to use JWT-based authentication.
- Disables session-based authentication.
- Registers the `JwtFilter`.

### **Why It's Needed:**
- Spring Security requires a configuration class to define authentication rules.

---

### **Example Code:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests()
        .requestMatchers("/auth/login").permitAll()
        .anyRequest().authenticated()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    http.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

### ✅ **2.8. `AuthController.java` – Authentication Controller or `AuthEntryPointJwt`**
**📁 Location:** `src/main/java/com/example/controller/AuthController.java`

### **Purpose:**
- Provides an endpoint for user login.
- Returns JWT token upon successful authentication.

### **Why It's Needed:**
- API endpoint to authenticate and generate JWT token.

---

### **Example Code:**
```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        return jwtUtil.generateToken(userDetails.getUsername());
    }
}
```

---

### ✅ **2.9. `Application.java` – Main Spring Boot Application**
**📁 Location:** `src/main/java/com/example/Application.java`

### **Purpose:**
- Entry point for the Spring Boot application.

### **Why It's Needed:**
- Spring Boot requires a main class to start the application.

---

### **Example Code:**
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## **3. How They Work Together**
1. `AuthController` handles login requests.
2. `CustomUserDetailsService` loads user from `UserRepository`.
3. `JwtUtil` generates JWT token.
4. `JwtFilter` extracts and validates the token.
5. `SecurityConfiguration` defines security rules.

---

