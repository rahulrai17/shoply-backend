# **JWT Authentication – Full Mechanism Explained**

---

## **1. What is JWT (JSON Web Token)?**
JWT (JSON Web Token) is a secure, compact, URL-safe token format used to represent claims between two parties (usually a client and a server).

- JWT is a **stateless** authentication mechanism.
- It allows secure data transmission without maintaining session state on the server.
- JWT is signed using a secret key (HMAC) or a public/private key pair (RSA or ECDSA).

---

## **2. Structure of a JWT**
A JWT is composed of **three parts** separated by dots (`.`):
```text
Header.Payload.Signature
```

### ✅ **Example:**
```text
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

### **1. Header**
- Describes the type of token and the signing algorithm used.
- Encoded using **Base64URL** encoding.

**Example Header (Base64 Decoded):**
```json
{
  "alg": "HS256", 
  "typ": "JWT"
}
```

- `alg` → Algorithm used for signing (e.g., HS256 for HMAC-SHA256)
- `typ` → Type of token (JWT)

---

### **2. Payload**
- Contains the **claims** (statements about an entity).
- Encoded using **Base64URL** encoding.
- Claims are divided into three types:
    - **Registered Claims** – Standard claims defined by JWT specification (e.g., `iss`, `sub`, `exp`)
    - **Public Claims** – Custom claims (e.g., `name`, `role`)
    - **Private Claims** – Used for communication between parties (e.g., `userId`)

**Example Payload (Base64 Decoded):**
```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022,
  "exp": 1516249022,
  "role": "admin"
}
```

- `sub` → Subject (User ID)
- `name` → Username
- `iat` → Issued at timestamp
- `exp` → Expiration timestamp
- `role` → User's role

---

### **3. Signature**
- Ensures that the token has not been altered.
- Created using the specified algorithm.

**Example Signature Generation:**
```text
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

- Ensures token integrity and authenticity.
- The server validates the signature using the secret key or public key.

---

## **3. How JWT Authentication Works – Step by Step**
### **Step 1: User Sends Credentials (Login)**
1. User sends a `POST` request with credentials (username and password) to the authentication endpoint.

**Example Request:**
```http
POST /login
{
    "username": "john",
    "password": "password123"
}
```

---

### **Step 2: Server Validates Credentials**
1. The server verifies the credentials against the database.
2. If valid, the server generates a JWT token containing user information.

---

### **Step 3: Create JWT Token**
1. The server generates a JWT with:
    - **Header** → Algorithm and type
    - **Payload** → User info and claims
    - **Signature** → HMAC256 or RSA/ES256

**Example Code (Java with Spring Security):**
```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

String jwt = Jwts.builder()
        .setSubject("john")
        .claim("role", "USER")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
        .signWith(SignatureAlgorithm.HS256, "mysecretkey")
        .compact();

System.out.println(jwt);
```

---

### **Step 4: Send JWT to Client**
1. The generated JWT token is sent back to the client in the response header.

**Example Response:**
```http
HTTP/1.1 200 OK
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...
```

---

### **Step 5: Client Stores JWT**
1. The client stores the JWT in:
    - **LocalStorage** – Suitable for SPAs and mobile apps.
    - **SessionStorage** – Suitable for temporary storage.
    - **HTTP-Only Cookie** – More secure, prevents XSS attacks.

---

### **Step 6: Client Sends JWT with Requests**
1. For each subsequent request, the client sends the JWT in the `Authorization` header.

**Example Request with JWT:**
```http
GET /profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...
```

---

### **Step 7: Server Validates JWT**
1. The server extracts the JWT from the request header.
2. The server decodes and verifies:
    - Header → Valid algorithm
    - Payload → Valid claims (e.g., `exp`, `iat`)
    - Signature → Matches server-generated signature

**Example Code for Validation:**
```java
String token = "eyJhbGciOiJIUzI1NiIsInR...";
Claims claims = Jwts.parser()
        .setSigningKey("mysecretkey")
        .parseClaimsJws(token)
        .getBody();

System.out.println("User: " + claims.getSubject());
System.out.println("Role: " + claims.get("role"));
```

---

### **Step 8: Grant or Deny Access**
1. If the token is valid, the server processes the request.
2. If the token is invalid, the server returns a `401 Unauthorized` or `403 Forbidden` response.

---

### **Step 9: Token Expiry**
1. Once the token expires, the client needs to log in again or use a **refresh token** (if implemented).

---

## **4. Complete Code Example in Spring Security**
### **Dependencies (Maven)**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

### **Security Configuration (JWT Based)**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

---

### **JWT Filter Example**
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .setSigningKey("mysecretkey")
                    .parseClaimsJws(token)
                    .getBody();

            // Get user details from claims and set in security context
            String username = claims.getSubject();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## **5. Advantages of JWT Authentication**
✅ **Stateless** – No session management needed on the server.  
✅ **Scalable** – Works well with microservices and distributed systems.  
✅ **Compact** – Small size, easy to transmit over HTTP.  
✅ **Secure** – If signed correctly and used over HTTPS.

---

## **6. Drawbacks of JWT Authentication**
❌ **No Revocation** – Cannot revoke individual tokens without using a token blacklist.  
❌ **Token Size** – Larger than a session ID.  
❌ **Stateless** – Cannot store sensitive data in JWT.

---

## **7. When to Use JWT**
- Best for microservices, REST APIs, and mobile apps.
- Avoid for stateful authentication (use sessions instead).  