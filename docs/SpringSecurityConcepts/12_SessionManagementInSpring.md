# Session management in Spring Security

In **Spring Security**, you can configure various **session management settings** to control how user sessions are created, maintained, and terminated. This is especially useful for securing web applications, preventing session fixation, handling concurrent sessions, and managing session timeouts.

---

##  **1. Configure Session Creation Policy**
You can control how Spring Security creates or manages sessions using the `sessionCreationPolicy()` setting:

###  Example:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
}
```

###  Options:
| Policy | Description |
|--------|-------------|
| `SessionCreationPolicy.ALWAYS` | Always create a new session if one doesn’t exist. |
| `SessionCreationPolicy.IF_REQUIRED` | Create a session only if needed (default). |
| `SessionCreationPolicy.NEVER` | Spring Security will never create a session, but will use one if it exists. |
| `SessionCreationPolicy.STATELESS` | Spring Security will not create or use a session (for stateless REST APIs). |

###  **When to Use:**
- `ALWAYS` → When you want to always maintain user session (e.g., stateful web app).
- `IF_REQUIRED` → When the app needs session only for authentication.
- `NEVER` → When you want to avoid session creation unless already present.
- `STATELESS` → For stateless APIs using JWT or OAuth2.

---

##  **2. Configure Session Fixation Protection**
**Session fixation** is an attack where an attacker sets a known session ID and tricks the user into using it, thereby hijacking the session.

Spring Security provides built-in protection against this using `sessionFixation().migrateSession()`:

###  Example:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
            .sessionFixation().migrateSession(); // Recommended setting
}
```

###  Options:
| Setting | Description |
|---------|-------------|
| `migrateSession()` | Creates a new session and copies attributes from the old one (default). |
| `newSession()` | Creates a completely new session without copying attributes. |
| `none()` | Does not change the session – vulnerable to fixation attacks. |

###  **Best Practice:**
- Use `migrateSession()` to reduce the risk of session hijacking.

---

##  **3. Set Maximum Concurrent Sessions Per User**
You can limit the number of active sessions a user can have at one time using `maximumSessions()`:

###  Example:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .sessionManagement()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true);
    return http.build();
}
```

###  Options:
| Setting | Description |
|---------|-------------|
| `maximumSessions(int)` | Number of allowed concurrent sessions per user. |
| `maxSessionsPreventsLogin(true)` | Prevents new logins if the max session limit is reached. |
| `maxSessionsPreventsLogin(false)` | Allows the new login and invalidates the oldest session. |

###  **Best Practice:**
- For sensitive applications (e.g., banking), limit to **one session per user**.  
- Set `maxSessionsPreventsLogin(true)` for better security.

---

##  **4. Set Session Timeout (Inactivity)**
You can define how long a session remains active before it times out due to inactivity.

###  Example (via `application.properties`):
```properties
server.servlet.session.timeout=30m
```

- `30m` → 30 minutes inactivity timeout.
- You can also configure it programmatically:

###  Example (Java Config):
```java
@Bean
public ServletContextInitializer servletContextInitializer() {
    return servletContext -> {
        servletContext.setSessionTimeout(30); // 30 minutes
    };
}
```

###  **Best Practice:**
- Set session timeout based on security needs (e.g., 15–30 minutes for secure apps).

---

##  **5. Enable "Remember Me" (Persistent Sessions)**
"Remember Me" allows the user to stay logged in even after the session expires.

###  Example:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .rememberMe()
            .key("uniqueAndSecret") // Encryption key for remember-me cookie
            .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
            .rememberMeParameter("remember-me");
}
```

###  Options:
| Setting | Description |
|---------|-------------|
| `key("key")` | Used to encode the remember-me token. |
| `tokenValiditySeconds(int)` | How long the remember-me cookie is valid (in seconds). |
| `rememberMeParameter("param")` | Name of the remember-me request parameter. |

###  **Best Practice:**
- Use a strong encryption key.  
- Keep expiration short for sensitive apps.

---

##  **6. Session Logout Settings**
You can configure session termination on logout to ensure that the session is invalidated when the user logs out.

###  Example:
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
}
```

###  Options:
| Setting | Description |
|---------|-------------|
| `invalidateHttpSession(true)` | Invalidates the session on logout. |
| `deleteCookies("JSESSIONID")` | Deletes the session cookie on logout. |
| `logoutSuccessUrl("/url")` | URL to redirect to after logout. |

###  **Best Practice:**
- Always delete cookies and invalidate sessions on logout to prevent session hijacking.

---

##  **7. Prevent Session Hijacking**
You can restrict session sharing across multiple devices or IP addresses.

###  Example:
- Track session IP:
```java
http
    .sessionManagement()
    .sessionFixation().migrateSession()
    .maximumSessions(1)
    .sessionRegistry(sessionRegistry());
```

- Create a custom `SessionRegistry` to track session details:
```java
@Bean
public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
}
```

###  **Best Practice:**
- Use `SessionRegistry` to monitor active sessions.  
- Restrict session reuse across devices or IPs.

---

##  **8. CSRF Protection**
CSRF (Cross-Site Request Forgery) protection is automatically enabled in Spring Security. If you need to configure or disable it:

###  Example:
```java
http
    .csrf()
        .disable(); // Disable if API is stateless
```

###  **Best Practice:**
- Keep CSRF enabled for session-based stateful applications.  
- Disable it for stateless REST APIs.

---

##  **9. Session Metadata Monitoring**
Spring Security allows you to monitor active sessions through `SessionRegistry`.

###  Example:
- Get active session details:
```java
Collection<SessionInformation> sessions = sessionRegistry.getAllSessions(user, false);
```

###  **Best Practice:**
- Use `SessionRegistry` to monitor and terminate sessions if needed.

---

##  **Complete Example**
Here's a complete example with session management settings:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .sessionFixation().migrateSession()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .and()
        .logout()
            .logoutUrl("/logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID");
}
```

---

##  **Best Practices Summary**
✔️ Use `STATELESS` for APIs and `IF_REQUIRED` for web apps.  
✔️ Use `migrateSession()` to prevent session fixation.  
✔️ Limit concurrent sessions to `1` for sensitive apps.  
✔️ Keep session timeout short for security.  
✔️ Enable `remember-me` only when necessary.  
✔️ Invalidate session and delete cookies on logout.  
✔️ Monitor active sessions using `SessionRegistry`.  