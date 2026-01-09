# UserDetails and UserDetailsService

## Let's  explore custom implementation of UserDetails and UserDetailsService

- In Spring Security, UserDetails and UserDetailsService are core components used for authentication and authorization.
  1. `UserDetails` : It is an interface that represents a user’s authentication information. Spring Security relies on UserDetails to store user-specific information during authentication.
     - It contains methods like:
       - `getUsername()`
       - `getPassword()`
       - `getAuthorities()`
       - `isAccountNonExpired()`, `isAccountNonLocked()`, etc.
     
  2. `UserDetailsService` : It is an interface that provides a method to load user details from a data source.
      ```java
      UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
      ```
     - Spring Security uses this method to fetch user information from databases, APIs, or other sources.
  

- Now the default implementation is good but is also limited and often doesn’t fit real-world applications, where:
    - Users are stored in a database (e.g., MySQL, PostgreSQL)
    - Additional user details (like roles, permissions, status) need to be fetched 
    - Business logic for authentication (e.g., account status checks) is required

- Now to implement UserDetails we are can create a CustomUserDetails class that implements UserDetails and fetches additional user data.
    ```java
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import java.util.Collection;
    
    public class CustomUserDetails implements UserDetails {
    
        private String username;
        private String password;
        private boolean isActive;
        private Collection<? extends GrantedAuthority> authorities;
    
        public CustomUserDetails(String username, String password, boolean isActive, Collection<? extends GrantedAuthority> authorities) {
            this.username = username;
            this.password = password;
            this.isActive = isActive;
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
            return true; // Modify based on business logic
        }
    
        @Override
        public boolean isAccountNonLocked() {
            return true; // Modify based on business logic
        }
    
        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Modify based on business logic
        }
    
        @Override
        public boolean isEnabled() {
            return isActive;
        }
    }
    
    ```
  - `GrantedAuthority` is an interface in Spring Security that represents an authority (or role/permission) granted to a user.
  - `Authority` = What the user is allowed to do (e.g., ROLE_ADMIN, ROLE_USER, READ_PRIVILEGE).
  - Comes from org.springframework.security.core.GrantedAuthority. 
  - Used for role-based or permission-based authentication and authorization.
  
- Next we will be working with the UserDetailsService :
    ```java
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;
    import java.util.List;
    import java.util.stream.Collectors;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    
    @Service
    public class CustomUserDetailsService implements UserDetailsService {
    
        @Autowired
        private UserRepository userRepository; 
    
        // Loading user info using the Repository and User Entity
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    
            // Convert roles from database to GrantedAuthority
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());
    
            // Returning CustomUserDetails Object 
            return new CustomUserDetails(
                    user.getUsername(),
                    user.getPassword(),
                    user.isActive(),
                    authorities
            );
        }
    }
    
    ```
- So as here the logic will be as :
  - with every request the details get fetched using the User entity then they are mapped to you CustomUserDetails object and then returned back
- Most codes do follow this pattern but maybe many other ways you can implement this by.



### **Summary for Spring Security Authentication Flow (Default vs. Custom Implementation)**

#### **1️⃣ Default Spring Security Implementation (Without Customization)**
- **UserDetails (Default Implementation)** → Uses `org.springframework.security.core.userdetails.User` (in-memory users).
- **UserDetailsService (Default Implementation)** → Uses `InMemoryUserDetailsManager`.
- **Authentication Provider** → Uses `DaoAuthenticationProvider` to authenticate users.
- **Security Filter** → Spring Security filters authenticate requests.
- **Database** → ❌ No database interaction (users stored in memory).

#### **💡 Flow (Default)**
🔹 **User enters credentials** → **Spring Security fetches user from `InMemoryUserDetailsManager`** → **AuthenticationManager authenticates** → **User is granted/denied access.**

---

#### **2️⃣ Custom Implementation (With Database and Entities)**
- **Entity (`User`)** → Represents user stored in the database.
- **Entity (`Role`)** → Stores user roles (Many-to-Many with `User`).
- **Repository (`UserRepository`)** → Fetches users from DB using JPA.
- **Custom `UserDetails` (`CustomUserDetails`)** → Converts `User` entity into `UserDetails`.
- **Custom `UserDetailsService` (`CustomUserDetailsService`)** → Fetches user from DB and returns `CustomUserDetails`.
- **Authentication Provider (`DaoAuthenticationProvider`)** → Uses `CustomUserDetailsService` for authentication.
- **Security Filter (`UsernamePasswordAuthenticationFilter`)** → Handles authentication logic.
- **Database (`MySQL/PostgreSQL/etc.`)** → Stores user details & roles.

#### **💡 Flow (Custom)**
🔹 **User enters credentials** → **Spring Security calls `CustomUserDetailsService.loadUserByUsername()`** → **User fetched from `UserRepository`** → **Converted into `CustomUserDetails`** → **Spring Security authenticates using `DaoAuthenticationProvider`** → **User is granted/denied access.**


