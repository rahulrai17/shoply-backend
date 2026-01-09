### **What is `ResponseEntity` in Spring Boot?**
`ResponseEntity` is a class in **Spring Boot** that represents the entire **HTTP response**, including the status code, headers, and body. It is used to send customized responses from a REST API.

#### **Key Features of `ResponseEntity`:**
1. **Control over HTTP Status Code** – Allows setting different status codes like `200 OK`, `404 Not Found`, `500 Internal Server Error`, etc.
2. **Custom Headers** – Can include custom HTTP headers in the response.
3. **Response Body** – Can send any Java object as a response body (like `String`, `List`, `Map`, or custom objects).
4. **Exception Handling** – Useful in handling errors by returning appropriate status codes.

---

### **Internal Working of `ResponseEntity`**
When a Spring Boot REST API method returns a `ResponseEntity`, Spring uses an internal mechanism to convert the response into an **HTTP response**.

#### **Step-by-Step Flow of `ResponseEntity` Processing:**
1. **Controller Method Execution**:
    - The controller method executes and returns a `ResponseEntity` object.

2. **Spring Boot's DispatcherServlet**:
    - The request first reaches **`DispatcherServlet`**, which is the front controller in Spring Boot.

3. **Handler Mapping**:
    - `DispatcherServlet` consults the **handler mapping** to find the appropriate controller method.

4. **Handler Adapter**:
    - The method is executed, and a `ResponseEntity` object is returned.

5. **HttpMessageConverter**:
    - The body of the `ResponseEntity` is converted into a suitable HTTP format (JSON/XML) using **`HttpMessageConverter`**.

6. **Response Sent to Client**:
    - The final HTTP response is created with the **status code, headers, and body** and sent back to the client.

---

### **Example Usage of `ResponseEntity`**
```java
@RestController
@RequestMapping("/api")
public class MyController {

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello, World!");
    }
}
```
**Response from API:**
```http
HTTP/1.1 200 OK
Content-Type: text/plain
Content-Length: 13

Hello, World!
```

---

### **Customizing ResponseEntity**
#### **1. Setting Custom Status Code**
```java
@GetMapping("/notfound")
public ResponseEntity<String> notFoundExample() {
    return new ResponseEntity<>("Resource Not Found", HttpStatus.NOT_FOUND);
}
```
🔹 **Response Status:** `404 NOT FOUND`

#### **2. Adding Headers**
```java
@GetMapping("/custom-header")
public ResponseEntity<String> customHeaderExample() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Custom-Header", "MyHeaderValue");

    return ResponseEntity.ok().headers(headers).body("Response with Custom Header");
}
```
🔹 **Custom header included in response**

#### **3. Returning JSON Object**
```java
@GetMapping("/user")
public ResponseEntity<User> getUser() {
    User user = new User(1, "John Doe", "john@example.com");
    return ResponseEntity.ok(user);
}
```
🔹 Converts the `User` object into JSON using `HttpMessageConverter`.

---

### **When to Use `ResponseEntity`?**
✅ When you need to **customize HTTP responses** (status, headers, body).  
✅ When returning **error responses** with different status codes.  
✅ When implementing **global exception handling** using `@ExceptionHandler`.

