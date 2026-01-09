# Model Mapper in java

## Why Map?

- Applications often consist of similar but different object models, where the data in two models may be similar but the structure and concerns of the models are different. Object mapping makes it easy to convert one model to another, allowing separate models to remain segregated.

## Why ModelMapper?

- The goal of ModelMapper is to make object mapping easy, by automatically determining how one object model maps to another, based on conventions, in the same way that a human would - while providing a simple, refactoring-safe API for handling specific use cases.

# **📌 Deep Dive into Mappers in Spring Boot**
Mappers in Spring Boot **convert objects from one type to another**, typically between **Entities** (database objects) and **DTOs** (Data Transfer Objects). This helps in **structuring data properly across different application layers**, improving **security, performance, and maintainability**.

---

## **🔹 Why Do We Need Mappers?**
Mappers help in **decoupling the data structure** at different layers of the application.

### **1️⃣ Avoid Exposing Sensitive Data**
- Entities often have fields like `password`, `createdAt`, or `internalId`, which shouldn't be exposed in API responses.
- A **DTO (Data Transfer Object)** ensures only **safe** fields are shared.

✅ **Example:**
```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password; // Should not be exposed
}
```
```java
// DTO (Data Transfer Object)
public class UserDTO {
    private String username;
    private String email;
}
```

🔹 **Problem:** If we return `User` directly, **passwords are exposed**!  
🔹 **Solution:** **Convert User → UserDTO before sending the response**.

---

### **2️⃣ Prevent Direct Modification of Entities**
- Without DTOs, a frontend could **modify sensitive fields** like `role`, `balance`, or `adminStatus`.

✅ **Example - Direct Modification Issue**
```java
@PostMapping("/update")
public User updateUser(@RequestBody User user) {  // BAD PRACTICE
    return userRepository.save(user);  // Allows overriding any field
}
```
🔹 **Problem:** If the request contains `"adminStatus": true`, **any user can become an admin!** 🚨  
🔹 **Solution:** Use a DTO to **restrict modifiable fields**.

✅ **Safe DTO Approach**
```java
public class UpdateUserDTO {
    private String email;
    private String phoneNumber;
}
```
```java
@PostMapping("/update")
public User updateUser(@RequestBody UpdateUserDTO dto) {
    User user = userRepository.findById(userId).orElseThrow();
    user.setEmail(dto.getEmail());
    user.setPhoneNumber(dto.getPhoneNumber());
    return userRepository.save(user);
}
```
🔹 **Now, only `email` and `phoneNumber` can be updated!** ✅

---

### **3️⃣ Optimizing Database Queries**
- Entities often have **relationships** (`@OneToMany`, `@ManyToOne`), which can cause **performance issues** when fetching data.

✅ **Example - Fetching Users with Orders**
```java
@Entity
public class User {
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}
```
```java
public class UserDTO {
    private String username;
    private List<OrderDTO> orders;
}
```
🔹 **Problem:** If we return `User` directly, it may trigger **lazy loading issues** or **huge data transfers**.  
🔹 **Solution:** Using DTOs, we can **load only required fields**.

---

## **📌 Approaches to Implement Mappers**
### **✅ 1️⃣ Manual Mapping (Full Control)**
In this method, we manually copy fields between objects.

#### **🔹 Example**
```java
public class UserMapper {
    public static UserDTO toDTO(User user) {
        return new UserDTO(user.getUsername(), user.getEmail());
    }

    public static User toEntity(UserDTO dto) {
        return new User(dto.getUsername(), dto.getEmail());
    }
}
```
```java
UserDTO dto = UserMapper.toDTO(user);
```

🔹 **Pros:** Simple, no dependencies.  
🔹 **Cons:** Tedious for large objects, requires maintenance.

---

### **✅ 2️⃣ ModelMapper (Reflection-Based Auto-Mapping)**
[`ModelMapper`](https://modelmapper.org/) is a library that **automatically maps objects** by matching field names.

#### **🔹 Setup**
Add the dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>2.4.4</version>
</dependency>
```

#### **🔹 Define ModelMapper Bean**
```java
@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
```

#### **🔹 Use ModelMapper for Conversion**
```java
@Autowired
private ModelMapper modelMapper;

public UserDTO convertToDto(User user) {
    return modelMapper.map(user, UserDTO.class);
}

public User convertToEntity(UserDTO dto) {
    return modelMapper.map(dto, User.class);
}
```

🔹 **Pros:** Less boilerplate, easy to use.  
🔹 **Cons:** Slightly slower (uses reflection), difficult to debug.

---

### **✅ 3️⃣ MapStruct (Compile-Time Code Generation)**
[`MapStruct`](https://mapstruct.org/) is a **compile-time code generator** that creates optimized mappers.

#### **🔹 Setup**
Add dependencies:
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.3.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.3.Final</version>
    <scope>provided</scope>
</dependency>
```

#### **🔹 Create a Mapper Interface**
```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "username", target = "userName")
    UserDTO toDTO(User user);

    User toEntity(UserDTO dto);
}
```

#### **🔹 Use the Mapper**
```java
UserDTO dto = UserMapper.INSTANCE.toDTO(user);
User entity = UserMapper.INSTANCE.toEntity(dto);
```

🔹 **Pros:** **Fastest approach**, no runtime overhead.  
🔹 **Cons:** Needs extra setup.

---

## **📌 Comparing Mappers**
| **Approach** | **Performance** | **Setup Complexity** | **Best For** |
|-------------|--------------|----------------|--------------|
| **Manual Mapping** | ✅ Fastest | ❌ Tedious | Small projects |
| **ModelMapper** | ⚠️ Slower (Reflection) | ✅ Easy | Medium-sized projects |
| **MapStruct** | ✅ Fast (Compile-time) | ⚠️ Requires setup | Large-scale projects |

---

## **📌 Best Practices for Mappers**
✔ **Use DTOs to prevent over-exposing entity fields.**  
✔ **For simple projects, manual mapping is fine.**  
✔ **For medium complexity, use ModelMapper.**  
✔ **For performance-critical apps, use MapStruct.**  
✔ **Avoid deep object nesting in DTOs (keep it flat for APIs).**

---

## **🚀 Conclusion**
Mappers help **convert objects between different layers** efficiently.  
If you want **full control**, use **manual mapping**.  
If you want **automatic mapping**, use **ModelMapper** or **MapStruct**.

Would you like me to integrate **MapStruct or ModelMapper** in your Spring Boot project? 🚀😊