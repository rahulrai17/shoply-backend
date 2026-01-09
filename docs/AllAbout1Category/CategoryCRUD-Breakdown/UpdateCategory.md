# UpdateCategory API Flow :

- Here we will define how this part of crud operation is designed here
- This is an operation to updateCategory in the Data Base.

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
- Then we have, hibernate configurations to where we allow, hibernate to create queries and perform operations.

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
        CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
    }
    ```
- So here we have are creating a method that will update the category in the database.

    ```java
    package com.shoply.backend.service;
    
    import com.shoply.backend.exceptions.APIException;
    import com.shoply.backend.exceptions.ResourceNotFoundException;
    import com.shoply.backend.model.Category;
    import com.shoply.backend.payload.CategoryDTO;
    import com.shoply.backend.payload.CategoryResponse;
    import com.shoply.backend.repositories.CategoryRepository;
    import org.modelmapper.ModelMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import org.springframework.web.bind.annotation.RequestBody;
    
    import java.util.List;
    
    
    @Service
    public class CategoryServiceImpl implements CategoryService {
    
        @Autowired
        private CategoryRepository categoryRepository;
    
        @Autowired
        private ModelMapper modelMapper;
    
        // updateCategory is to update category in the database
        @Override
        public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId){
            Category savedCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));
    
            Category category = modelMapper.map(categoryDTO, Category.class);
    
            category.setCategoryId(categoryId);
            savedCategory = categoryRepository.save(category);
            return modelMapper.map(savedCategory, CategoryDTO.class);
        }
    }
    ```
  
- This is updateCategory method which takes CategoryDTO object as the input and the categoryId:
- The CategoryDTO will have the updated details of the category, and the categoryId will be used to fetch the category for updating the details.
- Then we try to find the category using the CategoryId, if category exist then we create a Category object and store it there.
- Then we need to map the Category that we received in the body from CategoryDTO to Category Object.
- Then we set the id for the category object. 
- Then using the repository object we save it in the database using the same id, and this is how the category is updated in the database.
- Then we convert it to DTO again and return it to the controller, where using the Response entity we send it back to the user.
  - Here you can see that we are also using the `ResourceNotFoundException`, This is also a custom exception.

    ```java
    //ResourceNoFoundException.java
    package com.shoply.backend.exceptions;
  
    public class ResourceNotFoundException extends RuntimeException {
        String resourceName;
        String field;
        String fieldName;
  
        Long fieldId;
  
        public ResourceNotFoundException(String resourceName, String field, String fieldName) {
            super(String.format("%s not found with %s : '%s'", resourceName, field, fieldName));
            this.resourceName = resourceName;
            this.field = field;
            this.fieldName = fieldName;
        }
  
        public ResourceNotFoundException(String resourceName, String field, Long fieldId) {
            super(String.format("%s not found with %s : '%d'", resourceName, field, fieldId));
            this.resourceName = resourceName;
            this.field = field;
            this.fieldId = fieldId;
        }
  
        public ResourceNotFoundException() {
        }
    }
    ```
- This method extends `RuntimeException`, and using the constructors we are passing the values, Then we are using the super keyword to generate a string message and throwing it back to the method.
  - For `exceptions` we know they are being handled by the `MyGlobalExceptionHandler.java` class.

    ```java
    package com.shoply.backend.exceptions;
  
  
    import com.shoply.backend.payload.APIResponse;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.FieldError;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
  
    import java.util.HashMap;
    import java.util.Map;
  
    @RestControllerAdvice // This will intercept any exception throw by any controller in the application
    public class MyGlobalExceptionHandler {
  
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e){
            Map<String, String> response = new HashMap<>();
            e.getBindingResult().getAllErrors().forEach(err -> {
                String fieldName = ((FieldError)err).getField();
                String message = err.getDefaultMessage();
                response.put(fieldName,message);
            });
            return new ResponseEntity<Map<String,String>>(response, HttpStatus.BAD_REQUEST);
        }
  
        // This is a custom exception handler
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<APIResponse> myResponseNotFoundException(ResourceNotFoundException e){
            String message = new String(e.getMessage());
            APIResponse apiResponse = new APIResponse(message, false);
  
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
  
        @ExceptionHandler(APIException.class)
        public ResponseEntity<APIResponse> myAPIException(APIException e){
            String message = e.getMessage();
            APIResponse apiResponse = new APIResponse(message, false);
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
  
  
  
    }
  
    ```


### Step 6 : Creating a Controller Layer Method :

```java
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

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId){
        CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
    }
} 
```

- Here we are using this method to update the category data in database.
- Using ResponseEntity for Returning response.
- Taking CategoryDTO, CategoryId as input.
- Then we are calling the updateCategroy function from the CategoryService layer.
- Then we return the DTO using the ResponseEntity.