# Lets discuss the flow of JWT :

- Let's talk about how a user works with jwt.
- After a login user gets a jwt. But what is user supposed to do with a jwt like manually enter or something like that so he can use it?
- The ans is no, the client (user or application) **doesn’t manually enter the authorization header**. Instead, it’s handled automatically by the frontend/backend logic. Here's how it works in practice:


## **1. How the Client Gets the JWT Token**
When a user logs in, the backend generates the JWT and sends it back. The frontend needs to store it and use it for subsequent requests.

### **Step-by-Step Flow**
1. **User Logs In:**
    - User enters their credentials on a login form.
    - The frontend sends a `POST` request to the authentication API.

   **Request (Frontend → Backend)**
   ```json
   POST /login
   {
     "email": "user@example.com",
     "password": "securepassword"
   }
   ```

2. **Backend Generates JWT:**
    - Verifies credentials.
    - Creates a JWT and returns it in the response.

   **Response (Backend → Frontend)**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

3. **Frontend Stores the Token:**
    - JavaScript stores it in `localStorage`, `sessionStorage`, or a secure `HttpOnly` cookie.

   ```js
   localStorage.setItem("jwt", token);
   ```


## **2. How the Token is Sent Automatically**
After storing the token, the client needs to **attach it to every API request**. The frontend does this automatically.

### **Example Using Fetch API (Frontend)**
```js
const token = localStorage.getItem("jwt");

fetch("/profile", {
  method: "GET",
  headers: {
    "Authorization": `Bearer ${token}`
  }
})
  .then(response => response.json())
  .then(data => console.log(data));
```
- This **automatically includes** the token in the `Authorization` header.

### **Example Using Axios (Frontend)**
```js
import axios from "axios";

// Set token for all requests
axios.defaults.headers.common["Authorization"] = `Bearer ${localStorage.getItem("jwt")}`;

axios.get("/profile")
  .then(response => console.log(response.data));
```



## **3. How Backend Validates the Token**
When the client sends a request with the `Authorization` header, the backend verifies it.


## **4. Alternative: Secure Cookies Instead of Headers**
Some apps **don’t store JWTs in localStorage** due to security concerns (XSS attacks). Instead, they use `HttpOnly` cookies.

### **How It Works with Secure Cookies**
1. **Login Response Sets Cookie**
   ```http
   Set-Cookie: token=abc123; HttpOnly; Secure; SameSite=Strict
   ```
2. **Client Automatically Sends Cookie on Next Request**
    - No need to manually add the header.
   ```http
   GET /profile
   Cookie: token=abc123
   ```

3. **Backend Reads Cookie and Verifies JWT**
   ```js
   const token = req.cookies.token; // Extract token from cookie
   ```

---

## **In short we can say :**
- **Frontend gets JWT during login and stores it.**
- **For every API request, it automatically attaches the token in the header.**
- **Backend verifies the token to allow access.**
- **Alternative:** Use `HttpOnly` cookies so the browser handles the token automatically.

# Let's get a deep dive for cookies also :

Now let’s go through **cookie-based authentication** and how the client automatically gets and sends the authentication cookie without manual intervention.



## **1. How the Client Gets the Cookie**
Unlike JWTs stored in `localStorage`, authentication cookies are **automatically handled by the browser** when the backend sets them.

### **Step-by-Step Flow**
### **(a) User Logs In**
- The user enters their credentials.
- The frontend sends a `POST` request to the authentication API.

  **Request (Frontend → Backend)**
   ```json
   POST /login
   {
     "email": "user@example.com",
     "password": "securepassword"
   }
   ```

### **(b) Backend Verifies and Sets the Cookie**
- The server verifies the credentials.
- Instead of sending a JWT in the response, the server sets a **session cookie** in the `Set-Cookie` header.

  **Response (Backend → Frontend)**
   ```http
   HTTP/1.1 200 OK
   Set-Cookie: sessionId=abc123; HttpOnly; Secure; SameSite=Strict
   ```

- The **`HttpOnly`** flag makes sure JavaScript cannot access it (protects against XSS attacks).
- The **`Secure`** flag ensures the cookie is only sent over HTTPS.
- The **`SameSite=Strict`** flag prevents CSRF attacks.

---

## **2. How the Cookie is Sent Automatically**
Once the cookie is stored in the browser, it is **automatically sent** with every request to the server.

### **Example Request (Frontend → Backend)**
The client doesn’t need to manually include anything. The browser **automatically** sends the cookie.

```
GET /profile
Cookie: sessionId=abc123
```

#### **Example Fetch API Request (Frontend)**
```js
fetch("/profile", {
  method: "GET",
  credentials: "include" // Ensures cookies are sent
})
  .then(response => response.json())
  .then(data => console.log(data));
```
✅ No need to manually add an `Authorization` header!  
✅ The browser handles everything.


## **3. How the Backend Validates the Cookie**
- When the request arrives, the backend reads the cookie from the `Cookie` header.
- It then checks if the session ID is valid in the database or cache.
- If the session exists, the user is authenticated.
- Otherwise, the request is denied.



## **4. How the User is Logged Out**
To log out, the frontend makes a request to delete the cookie.

### **Frontend Logout Request**
```js
fetch("/logout", {
  method: "POST",
  credentials: "include" // Important for sending cookies
});
```

### **Backend Clears Cookie**
```js
app.post("/logout", (req, res) => {
    res.clearCookie("sessionId");
    res.json({ message: "Logged out" });
});
```
- `res.clearCookie("sessionId")` removes the cookie.
- The client is now logged out.

---

## **5. JWT vs. Cookie-Based Authentication (Summary Table)**

| Feature | JWT | Cookies |
|---------|-----|---------|
| Storage | `localStorage` / `sessionStorage` (or HttpOnly cookie) | HttpOnly Cookie |
| Sent with Requests | Manually added in `Authorization` header | Automatically sent by browser |
| Security | Can be stolen if stored in localStorage | More secure if using HttpOnly, Secure flags |
| Logout | Requires token invalidation strategy | Simple `res.clearCookie()` |
| Best For | Mobile apps, APIs, Microservices | Web apps, secure browser-based authentication |

---

## **summary**
- **Cookies are automatically handled by the browser.**
- **User logs in, backend sets a session cookie.**
- **Browser automatically sends the cookie in every request.**
- **Backend verifies the session to authenticate the user.**
- **Logging out simply requires clearing the cookie.**




