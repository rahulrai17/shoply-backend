# **Authentication **

---

## **1. Introduction to Authentication**
Authentication is the process of verifying the identity of a user or system trying to access a resource. It ensures that the entity making the request is who they claim to be.

### **Purpose of Authentication**
- To protect sensitive information from unauthorized access.
- To enable secure communication between clients and servers.
- To establish a user's identity before allowing access to resources.

---

## **2. Types of Authentication (Without JWT)**
Before the rise of JWT-based authentication, several traditional methods were used for authentication. These include:

### ✅ **1. Session-Based Authentication**
### ✅ **2. Basic Authentication**
### ✅ **3. Digest Authentication**
### ✅ **4. OAuth (Without JWT)**

---

## **3. Session-Based Authentication**
### **How Session-Based Authentication Works**
1. **User Login** – The user provides a valid username and password.
2. **Server Validation** – The server validates the credentials against a database.
3. **Session Creation** – If the credentials are valid, the server creates a session and stores it in memory or a database.
4. **Session ID Generation** – A unique session ID (e.g., a random string) is generated and sent to the client as a **cookie**.
5. **Cookie Storage** – The client stores the session ID in the browser's cookies.
6. **Subsequent Requests** – For every request, the client sends the session ID in the cookie.
7. **Session Verification** – The server verifies the session ID against the stored session data.
8. **Response** – If the session ID is valid, the server processes the request and returns the response.

---

### **Example of Session-Based Authentication**
1. **User Login Request**  
   **Request:**
```http
POST /login
{
    "username": "john",
    "password": "password123"
}
```

2. **Response with Set-Cookie Header**  
   **Response:**
```http
HTTP/1.1 200 OK
Set-Cookie: SESSION_ID=abcd1234; HttpOnly; Secure
```

3. **Subsequent Request with Cookie**  
   **Request:**
```http
GET /dashboard
Cookie: SESSION_ID=abcd1234
```

4. **Server Side Code Example**
   **Java Example with Spring Security:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/login", "/register").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(withDefaults()) // Enables session-based authentication
        .logout(logout -> logout.logoutSuccessUrl("/login"));
    return http.build();
}
```

---

### **How Server Stores the Session**
- The session is stored in **server memory**, **Redis**, or **database** (e.g., MySQL).
- Example session object in memory:
```json
{
    "sessionId": "abcd1234",
    "username": "john",
    "role": "USER",
    "expiry": "2025-03-25T10:00:00"
}
```

---

### **Session Expiry**
- Sessions are usually configured to expire after a certain time (e.g., 30 minutes).
- On expiry, the user is logged out automatically.

---

### **Drawbacks of Session-Based Authentication**
1. **Scalability Issues**
    - Storing sessions on the server increases memory consumption.
    - If the system scales horizontally (multiple servers), session synchronization between servers becomes complex.

2. **Stateful**
    - The server must maintain session state, leading to increased resource usage.

3. **Session Hijacking**
    - If the session ID is exposed (e.g., in logs), an attacker can use it to impersonate the user.

4. **CSRF (Cross-Site Request Forgery)**
    - If cookies are used for authentication, an attacker can exploit CSRF vulnerabilities.

---

## **4. Basic Authentication**
### **How Basic Authentication Works**
1. The client sends a request with the **Authorization** header.
2. The header contains the username and password in **Base64** encoding.
3. The server decodes the Base64 string and verifies the credentials.
4. If the credentials are valid, access is granted; otherwise, a `401 Unauthorized` response is returned.

---

### **Example of Basic Authentication**
1. **Request:**
```http
GET /dashboard
Authorization: Basic am9objpwYXNzd29yZDEyMw==
```
(Base64 decoded → `john:password123`)

2. **Server Side Code Example**
   **Java Example with Spring Security:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .anyRequest().authenticated()
        )
        .httpBasic(withDefaults());
    return http.build();
}
```

---

### **Drawbacks of Basic Authentication**
1. **Credentials Exposure**
    - Base64 encoding is not encryption; an attacker can decode the string easily.
2. **No State Management**
    - Credentials need to be sent with every request, increasing overhead.
3. **Not Secure Over HTTP**
    - If HTTPS is not used, the credentials can be intercepted.

---

## **5. Digest Authentication**
### **How Digest Authentication Works**
1. The server sends a **nonce** (random number) to the client.
2. The client creates a hash using the nonce, username, password, and request data.
3. The client sends the hash back to the server.
4. The server recreates the hash and compares it with the client’s hash.
5. If the hashes match, the user is authenticated.

---

### **Example of Digest Authentication**
**Request:**
```http
Authorization: Digest username="john",
               realm="myApp",
               nonce="abc123",
               uri="/dashboard",
               response="12345abcd"
```

### **Drawbacks of Digest Authentication**
1. **More Complex**
    - Requires hashing and nonce generation.
2. **Still Vulnerable to Replay Attacks**
    - If the nonce is not unique, an attacker can reuse it.

---

## **6. OAuth (Without JWT)**
### **How OAuth Works**
1. The client requests an **access token** using the user's credentials.
2. The OAuth provider issues a token.
3. The client sends the token in the `Authorization` header for each request.
4. The server validates the token.
5. If the token is valid, the request is processed.

---

### **Example OAuth Request**
**Request:**
```http
GET /dashboard
Authorization: Bearer abcdef12345
```

### **Drawbacks of OAuth Without JWT**
1. **Stateful**
    - The server must track the token state.
2. **Token Revocation Complexity**
    - Revoking tokens requires coordination between multiple servers.

---

## **7. Comparison of Methods**
| Method | State | Security | Scalability | Use Case |
|--------|-------|----------|-------------|----------|
| **Session-Based** | Stateful | Vulnerable to CSRF and hijacking | Poor | Web Applications |
| **Basic Authentication** | Stateless | Weak | High | APIs |
| **Digest Authentication** | Stateless | Moderate | High | Secure HTTP Connections |
| **OAuth (Without JWT)** | Stateful | Strong | Moderate | APIs, Microservices |

---

## **8. Why JWT Is Preferred Over Traditional Methods**
| Feature | Traditional Authentication | JWT-Based Authentication |
|---------|----------------------------|--------------------------|
| Stateless | No (Requires session management) | Yes |
| Scalability | Poor | High |
| Security | Moderate | High (Signature-based) |
| Storage | Server-side | Client-side |
| Performance | Slower | Faster |
| CSRF Protection | No | Yes |

---

## **9. Conclusion**
- Traditional methods like **session-based** and **basic authentication** are still widely used.
- Session-based authentication is suitable for monolithic applications.
- Basic authentication is suitable for quick and simple API security.
- OAuth (without JWT) is more secure but less scalable.
- JWT-based authentication is preferred for modern microservices due to scalability, security, and performance.