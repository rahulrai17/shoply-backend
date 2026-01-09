# CreateCategory Api Flow:

- Here we will define how this part of crud operation is designed here
- This is an operation to create Category in the database.

###  Step 1: We will need to create a connection with the database to read the data.

```properties
#application.properties
spring.application.name=shoply-backend

spring.datasource.url=jdbc:postgresql://localhost:5432/shoplydb
spring.datasource.username=postgres
spring.datasource.password=RahulRai

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

```

- First line contains the spring application name.
- Then we have the configuration for the database connection
- Then we have, hibernate configurations to where we allow the hibernate to create queries and perform operations.

--- 

### Step 2 : Create The Entity for our application.

```java
// Category.java
package com.shoply.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 5, message = "Category name must be at least 5 characters long")
    @Size(max = 50, message = "Category name must be at most 50 characters long")
    private String categoryName;
}
```

- This is the entity class we have created which is used in the context of Object-Relational Mapping (ORM), such as when using JPA (Java Persistence API) with frameworks like Hibernate. They represent database tables as Java objects, allowing developers to interact with a database using object-oriented programming rather than SQL queries.
- Here we also need to create getter and setter and constructor both args and no-args. Here we have implemented it using lombok.

#### No-Args constructor :
- Required by JPA/Hibernate, Hibernate and other JPA providers use reflection to create entity instances. A no-args constructor is necessary for this process.
- Also, Hibernate often creates proxy objects (lazy loading). A no-args constructor helps in dynamically creating instances.
- Also, If the entity is used in serialization/deserialization (e.g., JSON, XML), a default constructor ensures proper instantiation.

#### Args constructor (Parameterized Constructor):
- This is for the convenience for Object Creation, allows creating an instance with initial values without setting fields manually.
- Also, ensures Immutability. If fields are final, an all-args constructor is necessary.
- It Can also be used in DTO patterns, Helps in converting between Entity & DTOs (Data Transfer Objects).

---

### Step 3 : Create a Repository layer

```java
//CategoryRepository
package com.shoply.backend.repositories;

import com.shoply.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  
}
```
- The Repository Layer is a crucial part of the Spring Boot architecture (or any layered architecture in Java applications). It acts as an abstraction over data access logic, making interactions with databases cleaner and more manageable.
- The repository layer hides complex SQL queries, ensuring that the service layer doesn't directly interact with the database.
- ORM frameworks like Spring Data JPA and Hibernate optimize queries using caching, lazy loading, and batching.

---

### Step 4 : Create DTO for the entity

```java
// CategoryDTO
package com.shoply.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryID;
    private String categoryName;
}

```

- The DTO(Data transfer object) Layer is used to transfer data between different layers of an application while keeping concerns separate.
- It helps in structuring the data properly and prevents unnecessary exposure of sensitive or internal entity details.
- It helps to achieve Encapsulation & Abstraction.
- DTOs prevent direct exposure of database entities to external layers (e.g., API responses).
- Helps protect sensitive fields (e.g., passwords, internal IDs). This is done as if you return the response using the entity directly you will the return all the tables and values in db.
- So if you need to hide a certain field you can use DTO, as here we will only display what's necessary.
- Summery: It helps in decoupling the database entity from the API response.
- Here we will not create Use The Category response, as will be directly be working with the DTO.

---

### Step 5 : Create the service layer

- The service layer consists of two files one will the a interface and then other is the implementation of the interface.
- In our case we have : `CategoryService` `CategoryServiceImpl`.
- The interface defines what the service should do (contract).
- The implementation provides how it does it.
- This makes it easy to switch between different implementations without modifying dependent code.
- Benefit: If we later swap CategoryServiceImpl with NewCategoryServiceImpl, we don’t need to change the controller.
- Since in controller we will be calling the interface not the impl file so, it makes it easier to change business logic or the implementation file.

    ```java
    // CategoryService.java
    public interface CategoryService {
        CategoryDTO createCategories(CategoryDTO category);
    }
    ```
- So here we have are creating a method that will add category to the database.

    ```java
    // CategoryServiceImpl.java
    @Service
    public class CategoryServiceImpl implements CategoryService {
    
        @Autowired
        private CategoryRepository categoryRepository;
    
        @Autowired
        private ModelMapper modelMapper;
    
        // createCategories is to create and save new category in the database
        @Override
        public CategoryDTO createCategories(@RequestBody CategoryDTO categoryDTO){
    
            Category category = modelMapper.map(categoryDTO, Category.class);
            Category categoryFromBb = categoryRepository.findByCategoryName(category.getCategoryName());
    
            if(categoryFromBb != null){
                throw new APIException("Category wtih this name " + categoryDTO.getCategoryName() + " already exists !!! ");
            }
    
            Category savedCategory =  categoryRepository.save(category);
            return modelMapper.map(savedCategory, CategoryDTO.class);
    
        }
    }
    ```
- Here we will first take the body Response as the categoryDTO.
    ```json
    {
        "categoryName" : "Laptop"
    }
    ```
- Then we are using modelMapper object to Convert the DTO into the Category entity object so that we can perform the database operation.
- Next we will check if the Category that we are trying to create already exist in the database or not.
- If exist we throw Error APIException(This is custom-made) and print the message.
- or Else we save this category using the Category object and CategoryRepository.
- Then we use model mapper once again to map the Category object to the CategoryDTO and return it back to the User.

- The configuration for ModelMapper in done in AppConfig.java file.

    ```java
    // AppConfig.java
    package com.shoply.backend.config;
    
    import org.modelmapper.ModelMapper;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    
    @Configuration
    public class AppConfig {
    
        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }
    }
    ```

- Here is the code we are using for APIException.

    ```java
    //APIException.java
    package com.shoply.backend.exceptions;
    
    public class APIException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
        public APIException(){
        }
        public APIException(String message) {
            super(message);
        }
    
    }
    
    ```
- Yes! When you call APIException and pass a message, it will print the message because it extends RuntimeException, which already has built-in support for handling exception messages.
- When you throw the exception like this: 

    ```java
    throw new APIException("No category created till now");
    ```
- Internally, it calls the constructor:
    ```java
    public APIException(String message) {
        super(message);
    }
    ```
- Since RuntimeException (parent class) already stores and handles the message, calling getMessage() on the exception will return "No category created till now".

### Step 6 : Creating a Controller Layer.

```java
//CategoryController.java
package com.shoply.backend.controller;

import com.shoply.backend.config.AppConstants;
import com.shoply.backend.payload.CategoryDTO;
import com.shoply.backend.payload.CategoryResponse;
import com.shoply.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api") // This help to define path of endpoint at class level.
public class CategoryController {

  @Autowired
  private CategoryService categoryService;

  @PostMapping("/public/categories")
  public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
      CategoryDTO savedCategoryDTO = categoryService.createCategories(categoryDTO);
      return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
  }  
}
```

- This method is a REST API endpoint that creates an entry for Category in the database. 
- It takes a CategoryDTO as input and passes it to the method in CategoryService to save and then Returns the DTO back to the user.
- we have used ResponseEntity to handle the response and status.
