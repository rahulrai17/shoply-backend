# **📌 Creating Custom Beans in Spring Boot**
In **Spring Boot**, a **bean** is an object managed by the **Spring IoC (Inversion of Control) container**.  
Custom beans allow us to define reusable components such as **utility classes, configuration objects, and third-party libraries**.

---

## **🔹 Why Create Custom Beans?**
✔ **Encapsulation of logic** – Helps keep the application modular and maintainable.  
✔ **Dependency injection (DI)** – Eliminates the need for `new` keyword, making testing easier.  
✔ **Centralized management** – Beans are managed in a single place and reused throughout the application.

---

## **✅ 1️⃣ Creating a Custom Bean**
You can **register a custom bean** by annotating a method with `@Bean` inside a `@Configuration` class.

### **🔹 Example: Custom Bean for ModelMapper**
```java
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Marks this as a configuration class
public class AppConfig {

    @Bean  // Defines a bean for ModelMapper
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
```
### **🔹 Injecting & Using the Custom Bean**
Once registered, the bean can be **injected into any class** using `@Autowired`:

```java
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final ModelMapper modelMapper;

    @Autowired
    public UserService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public void mapEntities() {
        // Example usage of ModelMapper
        System.out.println("ModelMapper bean is working: " + modelMapper);
    }
}
```
✔ **Spring automatically injects `ModelMapper` into `UserService`.** ✅

---

## **✅ 2️⃣ Creating a Custom Utility Bean**
A common use case is to **define reusable utility classes**.

### **🔹 Example: Creating a Utility Bean**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilityConfig {

    @Bean
    public StringFormatter stringFormatter() {
        return new StringFormatter();
    }
}
```

```java
public class StringFormatter {
    public String format(String text) {
        return text.toUpperCase();
    }
}
```

### **🔹 Inject & Use Utility Bean**
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextService {

    private final StringFormatter stringFormatter;

    @Autowired
    public TextService(StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    public void printFormattedText() {
        System.out.println(stringFormatter.format("hello world!")); // Output: HELLO WORLD!
    }
}
```

---

## **✅ 3️⃣ Creating a Custom Bean with Constructor Arguments**
Sometimes, we need to **pass parameters to beans** (e.g., configurations).

### **🔹 Example: Bean with Parameters**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyService("Custom Parameter");
    }
}
```

```java
public class MyService {
    private final String message;

    public MyService(String message) {
        this.message = message;
    }

    public void printMessage() {
        System.out.println("Message: " + message);
    }
}
```
✔ **This ensures the service is created with a custom message.** ✅

---

## **✅ 4️⃣ Creating a Singleton Bean**
By default, Spring beans are **singleton** (only one instance per application context).

### **🔹 Example: Singleton Service**
```java
import org.springframework.stereotype.Service;

@Service
public class SingletonService {

    public SingletonService() {
        System.out.println("SingletonService instance created!");
    }

    public void execute() {
        System.out.println("Executing singleton service...");
    }
}
```
- The service is **automatically registered as a Spring bean** because of `@Service`.
- **Spring ensures only one instance exists** across the entire application.

---

## **✅ 5️⃣ Creating a Prototype Bean (Multiple Instances)**
By default, Spring beans are **singleton**, but we can create a **prototype bean** (new instance every time).

### **🔹 Example: Prototype Bean**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PrototypeConfig {

    @Bean
    @Scope("prototype")  // Every injection creates a new instance
    public MyPrototypeBean myPrototypeBean() {
        return new MyPrototypeBean();
    }
}
```

```java
public class MyPrototypeBean {
    public MyPrototypeBean() {
        System.out.println("New Prototype Bean Created!");
    }

    public void action() {
        System.out.println("Executing prototype bean action...");
    }
}
```

### **🔹 Using Prototype Bean**
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrototypeService {

    @Autowired
    private MyPrototypeBean myPrototypeBean;

    public void execute() {
        myPrototypeBean.action();
    }
}
```
✔ **Every time `PrototypeService` calls `execute()`, a new instance of `MyPrototypeBean` is created.** ✅

---

## **📌 Summary**
| **Bean Type** | **Annotation** | **Scope** | **Use Case** |
|-------------|-------------|---------|-------------|
| **Singleton (Default)** | `@Bean` or `@Service` | `singleton` (default) | Shared instance (e.g., Services, Repositories) |
| **Prototype Bean** | `@Bean @Scope("prototype")` | `prototype` | New instance per request (e.g., non-shared objects) |
| **Utility Bean** | `@Bean` | `singleton` | Utility classes (e.g., StringFormatter) |
| **Third-Party Bean** | `@Bean` | `singleton` | External libraries (e.g., ModelMapper, ObjectMapper) |

---

## **🚀 Conclusion**
✔ **Spring Boot allows you to create custom beans** for utility classes, services, and third-party libraries.  
✔ **Singleton beans** are used most often, but **prototype beans** are useful when you need multiple instances.  
✔ **Using `@Bean` inside a `@Configuration` class is the best way to register custom beans.**

Would you like to see a **real-world example using custom beans in a project**? 🚀😊