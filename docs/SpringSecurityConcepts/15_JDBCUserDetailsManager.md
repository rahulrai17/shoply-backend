# JDBCUserDetailsManager in Spring Security

`JDBCUserDetailsManager` is a built-in Spring Security class that provides **user authentication and authorization** using a **relational database** (like MySQL, PostgreSQL, H2, etc.).

It's a subclass of `UserDetailsManager`, and it works by:
- Loading user details (username, password, roles) from a database using **JDBC**.
- Creating and managing user accounts directly in the database.
- Allowing you to define custom SQL queries for user authentication and authorization.

---

## **When to Use `JDBCUserDetailsManager`**
- When you need to store user details in a database (instead of in-memory).
- When you want to enable dynamic user and role management via SQL.
- When you want to customize the user schema or queries.

---

## **How `JDBCUserDetailsManager` Works**
1. The `JDBCUserDetailsManager` reads user information from a **relational database**.
2. It expects a specific schema for storing **users** and **authorities**.
3. When a user logs in, Spring Security queries the database and loads the user details.
4. If the user is valid, authentication succeeds; otherwise, it fails.

---

## **Step 1: Create Database Tables**
By default, `JDBCUserDetailsManager` expects two tables:

### **1. `users` Table**
- Stores username and password information.
- Must include `enabled` column to determine if the user is active.

```sql
CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);
```

### **2. `authorities` Table**
- Stores roles/authorities for each user.
- The `username` column should match the `users` table.

```sql
CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY(username) REFERENCES users(username)
);
```

Each user can have **multiple authorities** (e.g., `ROLE_USER`, `ROLE_ADMIN`).

---

## **Step 2: Define a `JDBCUserDetailsManager` Bean**
You can define the `JDBCUserDetailsManager` using **`DataSource`**.

### Example Code:
```java
@Bean
public UserDetailsManager userDetailsManager(DataSource dataSource) {
    return new JdbcUserDetailsManager(dataSource);
}
```

- `DataSource` – Spring will inject the configured `DataSource` (linked to the database).
- `JdbcUserDetailsManager` – Uses this `DataSource` to load and manage user data.

---

## **Step 3: Define Security Configuration**
Set up `HttpSecurity` to use `JDBCUserDetailsManager` for authentication:

### Example Code:
```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasRole("USER")
            .requestMatchers("/").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(withDefaults()) // Enable form login
        .httpBasic(withDefaults()); // Enable basic auth

    return http.build();
}
```

---

## **Step 4: Add Test Data to the Database**
You can insert test data using plain SQL:

### Create Users
```sql
INSERT INTO users (username, password, enabled)
VALUES ('admin', '{noop}password', true);

INSERT INTO users (username, password, enabled)
VALUES ('user', '{noop}password', true);
```

### Assign Roles
```sql
INSERT INTO authorities (username, authority)
VALUES ('admin', 'ROLE_ADMIN');

INSERT INTO authorities (username, authority)
VALUES ('user', 'ROLE_USER');
```

`{noop}` → Spring uses `{noop}` to indicate that the password is stored as plaintext (for testing).  
For production, use password encoders like **BCrypt** instead of `{noop}`.

---

## **Step 5: Test the Application**
1. Start the Spring Boot application.
2. Open Postman or browser:
    - `/user/hello` → Requires `ROLE_USER`
    - `/admin/hello` → Requires `ROLE_ADMIN`
3. Login using:
    - **admin** → `password`
    - **user** → `password`

---

## **Full Example Code**
### Complete `SecurityConfig.java`
```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .requestMatchers("/").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(withDefaults())
            .httpBasic(withDefaults());

        return http.build();
    }
}
```

---

## **Using `BCrypt` for Passwords**
Instead of `{noop}`, you should use a secure password encoder like **BCrypt**:

### Example with BCrypt:
1. Encode the password using BCrypt:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

2. Insert encoded password into the database:
```sql
INSERT INTO users (username, password, enabled)
VALUES ('admin', '$2a$10$kNrfuO3uAohKZtk5kB2WxebXmcK3SROp7/9RQmPks3Kcwo2A4Ql5O', true);
```

Generate the password hash using:
```java
System.out.println(new BCryptPasswordEncoder().encode("password"));
```

3. Modify the `JDBCUserDetailsManager` to use the encoder:
```java
@Bean
public UserDetailsManager userDetailsManager(DataSource dataSource, PasswordEncoder passwordEncoder) {
    JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
    userDetailsManager.setPasswordEncoder(passwordEncoder);
    return userDetailsManager;
}
```

---

## **Custom SQL Queries**
You can modify the default queries using `setXxxQuery()`:

### Example:
```java
JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
manager.setUsersByUsernameQuery(
    "SELECT username, password, enabled FROM my_users WHERE username = ?"
);
manager.setAuthoritiesByUsernameQuery(
    "SELECT username, role FROM my_roles WHERE username = ?"
);
```

This allows you to customize the schema and query structure based on your database design.

---

## **CRUD Operations on Users**
You can use `JDBCUserDetailsManager` to create, update, and delete users:

### Create User:
```java
UserDetails user = User.withUsername("john")
                       .password("{noop}password")
                       .roles("USER")
                       .build();
userDetailsManager.createUser(user);
```

### Update User:
```java
userDetailsManager.updateUser(user);
```

### Delete User:
```java
userDetailsManager.deleteUser("john");
```

---

## **How It Works Under the Hood**
1. `JDBCUserDetailsManager` executes the SQL queries.
2. If authentication succeeds, it loads the `UserDetails` object.
3. If the user has the necessary roles, access is granted.
4. If not, a `403 Forbidden` response is returned.

---

## **Best Practices**
- Use `BCrypt` for password encoding (do not use plaintext passwords).
- Keep your schema secure — avoid exposing user details directly.
- Fine-tune queries to match your database schema.
- Limit database access to minimize security risks.

---

## **Why `JDBCUserDetailsManager` is Useful**
- Works with any JDBC-compliant database.
- Flexible and configurable queries.
- Supports dynamic user management.

---

Let me know if you need to modify or expand any part of this explanation.

