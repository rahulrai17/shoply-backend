## **Why Use Entity and Repository in Java?**

In **Spring Boot with JPA (Java Persistence API)**, we use **Entities** and **Repositories** to manage database interactions efficiently.

### **1️⃣ Why Use an Entity?**
An **Entity** represents a **table in a database**. It allows us to define the structure of the table using Java classes.

### **2️⃣ Why Use a Repository?**
A **Repository** provides an **abstraction over database operations** (CRUD: Create, Read, Update, Delete). It helps us interact with the database **without writing SQL queries manually**.

---

## **Step-by-Step Guide to Implementing Entity and Repository in Java (Spring Boot)**

### **1. Create a Spring Boot Application**
If you don’t have a Spring Boot project, create one using **Spring Initializr** with:
- **Spring Web** (for REST API)
- **Spring Data JPA** (for database interaction)
- **H2 Database / MySQL** (for storing data)

---

### **2. Define an Entity (Database Table)**
An **Entity class** maps to a **database table**.

```java
import jakarta.persistence.*;

@Entity  // Marks this class as a database entity
@Table(name = "users")  // Specifies table name
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;
    
    @Column(nullable = false)  // Cannot be null
    private String name;
    
    @Column(unique = true)  // Email must be unique
    private String email;

    // Constructors
    public User() {}
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

#### **How This Works:**
- `@Entity` tells **JPA** that this class represents a table.
- `@Id` marks the **primary key**.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` makes the **ID auto-increment**.
- `@Column(nullable = false)` ensures a field **cannot be null**.
- `@Column(unique = true)` ensures **emails are unique**.

---

### **3. Create a Repository (Data Access Layer)**
A **Repository** interfaces with the **database**.

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods (optional)
    User findByEmail(String email);
}
```

#### **How This Works:**
- `JpaRepository<User, Long>` allows us to perform **CRUD operations** on the `User` entity.
- The method `findByEmail(String email)` is **automatically implemented** by Spring Data JPA.

---

### **4. Create a Service Layer (Business Logic)**
The **service layer** handles business logic and interacts with the repository.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Fetch all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Fetch a user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Fetch a user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Create a new user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Delete a user by ID
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

---

### **5. Create a Controller (Expose API Endpoints)**
The **controller layer** exposes APIs that interact with the service layer.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // GET all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // GET user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // POST - Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // DELETE user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## **How It All Works Together**
| **Layer**       | **Purpose** |
|----------------|------------|
| **Entity (User.java)** | Represents the database table |
| **Repository (UserRepository.java)** | Provides CRUD operations |
| **Service (UserService.java)** | Handles business logic |
| **Controller (UserController.java)** | Handles API requests |

---

## **Testing the API**
1. **Run your Spring Boot application** (`@SpringBootApplication` main class).
2. Use **Postman** or a browser to test the endpoints.

| **HTTP Method** | **Endpoint** | **Functionality** |
|---------------|-------------|----------------|
| `GET` | `/users` | Get all users |
| `GET` | `/users/{id}` | Get user by ID |
| `GET` | `/users/email/{email}` | Get user by email |
| `POST` | `/users` | Create a new user |
| `DELETE` | `/users/{id}` | Delete user by ID |

---

## **Why Use Entity and Repository?**
✅ **Entities** are used to **map Java objects to database tables**.  
✅ **Repositories** allow **easy CRUD operations** **without writing SQL**.  
✅ **Reduces boilerplate code**, making applications **faster to develop and maintain**.  
✅ Works well with **Spring Boot, Hibernate, and JPA** for seamless database interactions.

Would you like an example with **DTOs, Exception Handling, and Validation**? 🚀