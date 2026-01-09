# Category Implementation 

- This is the second way we will be implementing the creation of Category Method.
- This is also just for learning as i am following the basic to advance structure for learning and building SpringBoot Application.

## What we will be learning about Here?

- In this will we create the CRUD Operation for category service using all the main layers except Data Access Layer.
- Data Access Layer will be implemented in the next section Category III.

- Here we will be learning 
  1. Use of application.properties file : A configuration file used to store application-wide settings, such as database connections, server ports, and other environment-specific properties.
  2. About Model Layer : Represents the data structures and business logic of the application. It defines the shape of the data, including entities, value objects, and data transfer objects (DTOs).
  3. About Controller Layer : Handles incoming HTTP requests and sends responses. Controllers receive input from the user, invoke business logic, and return data to the user through views or APIs.
  4. About Service Layer : Encapsulates the business logic of the application. Services orchestrate the interactions between models, repositories, and other components to perform complex operations.
  5. About Repository Layer :  Abstracts the data storage and retrieval logic. Repositories encapsulate the data access layer, providing a standardized interface for interacting with databases, file systems, or other data sources.
  6. About Entity Layer : Represents the underlying data structures in the database. It defines the structure and behavior of the entities that make up the application's data model.

- Above we have explained the basics Overview of what we will be learning in this section.

## Practical Approach 

Step 1 : We will start with the defining the application.properties file.

```properties
spring.application.name=shoply-backend

spring.datasource.url=jdbc:postgresql://localhost:5432/shoplydb
spring.datasource.username=postgres
spring.datasource.password=RahulRai

# Hibernate settings
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# what the below line does is it checks if the table exist in db or not if it doesn't exist it creates a new table. you can even delete
# the table and restart the application and it will create a new table in the database i have tested it.
# you can also choose from other values too.
spring.jpa.hibernate.ddl-auto=update 

# What this will do is if true it will show you the queries created by hibernate in the console of this application.
spring.jpa.show-sql=true

```



This is a configuration file (`application.properties`) for a Spring Boot application. It sets the following properties:

* `spring.application.name`: The name of the application, set to "shoply-backend".
* `spring.datasource`: Database connection settings, including:
    + `url`: The URL of the PostgreSQL database, located on `localhost` at port `5432`, with database name `shoplydb`.
    + `username` and `password`: Credentials for the database connection, set to `postgres` and `RahulRai`, respectively.
* `spring.jpa.hibernate`: Hibernate settings, including:
    + `ddl-auto`: Set to `update`, which means that Hibernate will update the database schema if it already exists, or create a new one if it doesn't.
    + `show-sql`: Set to `true`, which means that Hibernate will print the SQL queries it generates to the console.

In summary, this configuration file sets up a Spring Boot application to connect to a PostgreSQL database and use Hibernate for database operations.


Step 2 : We will start with the defining the Model Layer

```java
// Category
package com.shoply.backend.model;

import javax.persistence.*;

@Entity(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    
    private String categoryName;
    
    // All args Constructor
    public Category(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // No args constructor
    public Category() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
}

```



Here's a detailed explanation of the code snippet:

**Package and Import Statements**

* `package com.shoply.backend.model;` declares that this Java class belongs to the `com.shoply.backend.model` package.
* `import javax.persistence.*;` imports all classes and annotations from the `javax.persistence` package, which is part of the Java Persistence API (JPA). JPA is a standard for accessing, persisting, and managing data between Java objects/classes and a relational database.

**Entity Annotation**

* `@Entity(name = "categories")` is an annotation that indicates this Java class is an entity, which represents a table in a relational database. The `name` attribute specifies the name of the table, which in this case is "categories".
* The `@Entity` annotation comes from the `javax.persistence` package and is used to define the mapping between the Java class and the database table.

**Id Annotation**

* `@Id` is an annotation that indicates the `categoryId` field is the primary key of the entity.
* The `@Id` annotation comes from the `javax.persistence` package and is used to define the primary key of an entity.

**GeneratedValue Annotation**

* `@GeneratedValue(strategy = GenerationType.IDENTITY)` is an annotation that indicates the `categoryId` field should be automatically generated by the database using an identity strategy.
* The `@GeneratedValue` annotation comes from the `javax.persistence` package and is used to define how the primary key should be generated.
* The `strategy` attribute specifies the generation strategy, which in this case is `GenerationType.IDENTITY`. This means the database will automatically generate a unique identifier for each new entity.

**Constructors**

* **All-args Constructor**: `public Category(Long categoryId, String categoryName) { ... }` is a constructor that takes two parameters: `categoryId` and `categoryName`. This constructor is used to create a new `Category` object with the specified `categoryId` and `categoryName`.
* **No-args Constructor**: `public Category() { }` is a constructor that takes no parameters. This constructor is used to create a new `Category` object with default values for `categoryId` and `categoryName`.
* The two constructors serve different purposes:
    + The all-args constructor is used when you want to create a new `Category` object with specific values for `categoryId` and `categoryName`.
    + The no-args constructor is used when you want to create a new `Category` object without specifying any values for `categoryId` and `categoryName`. This constructor is often used by JPA providers to create a new entity object when retrieving data from the database.

**Getter and Setter Methods**

* **Getter Methods**: `public Long getCategoryId() { ... }` and `public String getCategoryName() { ... }` are methods that return the values of the `categoryId` and `categoryName` fields, respectively.
* **Setter Methods**: `public void setCategoryId(Long categoryId) { ... }` and `public void setCategoryName(String categoryName) { ... }` are methods that set the values of the `categoryId` and `categoryName` fields, respectively.
* Getter and setter methods are used to encapsulate the data within the `Category` object and provide a way to access and modify the data in a controlled manner.
* The benefits of using getter and setter methods include:
    + Encapsulation: The data is hidden within the object, and access to it is controlled through the getter and setter methods.
    + Flexibility: The getter and setter methods can be modified to perform additional logic or validation when accessing or modifying the data.
    + Reusability: The getter and setter methods can be reused in different parts of the application.

In summary, the `Category` class is a Java entity that represents a table in a relational database. The annotations `@Entity`, `@Id`, and `@GeneratedValue` define the mapping between the Java class and the database table. The constructors provide a way to create new `Category` objects with specific values or default values. The getter and setter methods provide a way to access and modify the data within the `Category` object in a controlled manner.

step 3 : We will start with the defining the Repository Layer:

```java 
// CategoryRepository
package com.shoply.backend.repositories;

import com.shoply.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

```



Let's break down the code snippet and explore the details.

**What is `JpaRepository`?**

`JpaRepository` is an interface provided by Spring Data JPA (Java Persistence API) that simplifies the interaction with a database using JPA. It's a part of the Spring Data project, which aims to reduce the amount of boilerplate code needed to implement data access layers.

**What are the parameters of `JpaRepository`?**

`JpaRepository` takes two type parameters:

1. `T`: The type of the entity (in this case, `Category`).
2. `ID`: The type of the primary key (in this case, `Long`).

These type parameters allow Spring Data JPA to generate the necessary queries and mappings for the entity.

**What functions does `JpaRepository` offer?**

By extending `JpaRepository`, the `CategoryRepository` interface inherits a set of methods that provide basic CRUD operations for the `Category` entity. Here are some of the most commonly used methods:

### CRUD Operations

* `findAll()`: Returns a list of all entities.
* `findById(ID id)`: Returns an entity by its primary key.
* `save(T entity)`: Saves or updates an entity.
* `delete(T entity)`: Deletes an entity.
* `deleteById(ID id)`: Deletes an entity by its primary key.

### Query Methods

* `findAll(Sort sort)`: Returns a list of entities sorted by the specified criteria.
* `findAll(Pageable pageable)`: Returns a page of entities, allowing for pagination.
* `findBy[Property](Property value)`: Returns a list of entities that match the specified property value (e.g., `findByCategoryName(String name)`).

### Custom Query Methods

You can also define custom query methods by adding Spring Data JPA annotations, such as `@Query`, to the method signature. For example:
```java
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findCategoriesByName(@Param("name") String name);
}
```
This custom method uses a JPA query to find categories with names matching the specified pattern.

**What's happening behind the scenes?**

When you extend `JpaRepository`, Spring Data JPA generates an implementation of the interface at runtime, using the entity metadata and the JPA provider (e.g., Hibernate). This implementation takes care of the underlying database interactions, allowing you to focus on writing business logic.

In summary, by extending `JpaRepository`, you get a set of basic CRUD operations and query methods for your entity, without having to write explicit database query code. You can also define custom query methods using Spring Data JPA annotations.


step 4 : We will start with the defining the Service Layer:

- For implementing the service layer first we need to define the interface for the service layer.
- Then we need to define the implementation for the service layer in a java class.
- For example if we created a CategoryService interface and CategoryServiceImp class then we can use this interface in our controller layer.
- To access the Repository layer we will create a object of "CategoryRepository" and through that we will be calling all the functions in the controller layer.
- Also remember the dependency injection part(@Autowired or Constructor Injection).
- 
```java
//CategoryService.java
package com.shoply.backend.service;

import com.shoply.backend.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    void createCategories(Category category);
    String deleteCategory(Long categoryId);

    Category updateCategory(Category category, Long categoryId);
}

```

```java
//CategoryServiceImpl.java
package com.shoply.backend.service;

import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.Category;
import com.shoply.backend.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{
    
    // Below, This is the object for the CategoryRepository interface which inherits functions like save(), findAll(), etc, from JpaRepository.
    // "@Autowired" is important as if you miss this dependency injection will not work and it will thorw this error : "Cannot invoke "com.shoply.backend.repositories.CategoryRepository.findAll()" because "this.categoryRepository" is null".
    // You con also implement this using constructor injection here, But this is more readable. 
    @Autowired 
    private CategoryRepository categoryRepository;

    
    // Method to fetch all the data in the database
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Method to create and save new category in the database.
    @Override
    public void createCategories(@RequestBody Category category){
        categoryRepository.save(category);
    }
    
    // Method to delete category from the database
    @Override
    public String deleteCategory(Long categoryId) {
        List<Category> categories = categoryRepository.findAll();

        Category category = categories.stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource Not Found"));

        categoryRepository.delete(category);
        return "Category with categoryId: " + categoryId + " is deleted Successfully !! ";
    }
    
    // Method to update Category data. Eg: from "categoryName: Travel" to "categoryName: Health"
    @Override
    public Category updateCategory(Category category, Long categoryId) {
        List<Category> categories = categoryRepository.findAll();

        // Here the approach that is seen is maybe different from the delete one, you can use both the methods, these are just different approach.
        Optional<Category> optionalCategory = categories.stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst();

        if(optionalCategory.isPresent()){
            Category exitstingCategory = optionalCategory.get();
            exitstingCategory.setCategoryName(category.getCategoryName());
            Category savedCategory = categoryRepository.save(exitstingCategory); // added this step for JpaRepository use.
            // return exitstingCategory;
            return savedCategory;
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
    }
    

  

}

```

Step 5 : Now we will start working with the controller layer :

- Now as we have created a service Layer, Next all we need to do is define a place where we will map the url endpoints to a function.
- This is known as Controller Layer.
- To access the service layer we will create a object of "CategoryService" and through that we will be calling all the functions in the controller layer.

```java
//CategoryController
package com.shoply.backend.controller;

import com.shoply.backend.model.Category;
import com.shoply.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api") // This help to define path of endpoint at class level.
public class CatergoryController {


  // This is the example if you want to use constructor injection, but for this program we have used (@Autowired)
  /*   
  public CatergoryController(CategoryService categoryService) {
      this.categoryService = categoryService;
  }
  */
  
  
  // dependency Injection  
  @Autowired
  private CategoryService categoryService;
  
  
  // Instead of @GetMapping/PostMapping/PutMapping/DeleteMapping you can use @RequestMapping also.
  // @RequestMapping(value = "/api/public/categories", method = RequestMethod.GET)
  @GetMapping("/public/categories")
  public List<Category> getAllCategories(){
    List<Category> categories = categoryService.getAllCategories();
    return categories; 
    
    // or you can simply implement the below line.
    // return categoryService.getAllCategories(); - also can be done like this.
  }

  // @RequestMapping(value = "/api/public/categories", method = RequestMethod.POST)
  @PostMapping("/public/categories")
  public String createCategory(@Valid @RequestBody Category category){
    categoryService.createCategories(category);
    return "Category added Successfully";
  }

  @PutMapping("/public/categories/{categoryId}")
  public String updateCategory(@RequestBody Category category, @PathVariable Long categoryId){
    try {
      Category savedCategory = categoryService.updateCategory(category, categoryId);
      return ("Category with ID: " + categoryId + " Updated.");
    } catch (ResponseStatusException e){
      return e.getReason();
    }
  }

  @DeleteMapping("/admin/categories/{categoryId}")
  public String deleteCategory(@PathVariable Long categoryId){
    try {
      String status = categoryService.deleteCategory(categoryId);
      // return ResponseEntity.ok(status); - You can write it in these ways also
      // return ResponseEntity.status(HttpStatus.OK).body(status);
      // below is the most common one.
      return status;
    } catch (ResponseStatusException e) {
      return e.getReason();
    }
  }
  
}

```



This is a comprehensive guide to building a Spring Boot application with a Category entity, including the implementation of the Model, Repository, Service, and Controller layers.

Here's a succinct summary of the code snippets:

**Step 1: Defining the application.properties file**

* This file sets up the Spring Boot application's configuration, including the database connection settings and Hibernate properties.

**Step 2: Defining the Model Layer**

* The `Category` class represents the Category entity, with fields for `categoryId` and `categoryName`.
* The class is annotated with `@Entity`, `@Id`, and `@GeneratedValue` to define the mapping between the Java class and the database table.

**Step 3: Defining the Repository Layer**

* The `CategoryRepository` interface extends `JpaRepository` to provide basic CRUD operations for the Category entity.
* The interface is used to define the data access layer for the Category entity.

**Step 4: Defining the Service Layer**

* The `CategoryService` interface defines the business logic for the Category entity, including methods for creating, reading, updating, and deleting categories.
* The `CategoryServiceImpl` class implements the `CategoryService` interface and provides the implementation for the business logic.

**Step 5: Defining the Controller Layer**

* The `CategoryController` class defines the REST endpoints for the Category entity, including methods for creating, reading, updating, and deleting categories.
* The class uses the `CategoryService` interface to access the business logic for the Category entity.

Overall, this code provides a comprehensive example of how to build a Spring Boot application with a Category entity, including the implementation of the Model, Repository, Service, and Controller layers.