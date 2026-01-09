In a **Spring Boot** application, a utility class like `AuthUtil` is not a standard Spring class—it's typically a **custom helper class** that developers create to handle **authentication-related logic** in a reusable way. The exact functionality can vary depending on the project, but here's what it's commonly used for:

---

### 🔐 Common Use Cases of `AuthUtil` in Spring Boot:

#### 1. **Getting the Currently Logged-in User**
```java
public class AuthUtil {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();  // or custom logic to get user details
        }
        return null;
    }
}
```
- Handy for controllers, services, etc., where you need the current user's username or ID.

---

#### 2. **Checking Roles or Authorities**
```java
public static boolean hasRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().stream()
               .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
}
```
- Useful for feature toggles or logic based on user roles.

---

#### 3. **Extracting JWT Claims (if using JWT)**
If you're using JWT (JSON Web Tokens), `AuthUtil` can help extract custom claims from the token.

```java
public static String getClaim(String claimKey) {
    // extract token and parse claims
}
```

---

#### 4. **Generic Authentication Checks**
- Is the user authenticated?
- Is the session anonymous?
- Is the user blocked or disabled?

---

### 🔧 Why Use It?
- **Code Reusability**: Avoid repeating auth-check code everywhere.
- **Centralization**: All your auth-related helpers live in one place.
- **Cleaner Controllers/Services**: Delegate logic like `getCurrentUser()` to a utility class.

---

