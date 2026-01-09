# Delete Category API Overview

- Here we will define how this part of crud operation is designed here
- This is an operation to DeleteCategory in the Data Base.


### Step 1: We will need to create a connection with the database to read the data :

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

### Step 2 : Create The Entity for our application :

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

### Step 3 : Create a Repository layer :

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

### Step 4 : Create DTO for the entity :

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

### Step 5 : Create the service layer :

- The service layer consists of two files one will be a interface and then other is the implementation of the interface.
- In our case we have : `CategoryService` `CategoryServiceImpl`.
- The interface defines what the service should do (contract).
- The implementation provides how it does it.
- This makes it easy to switch between different implementations without modifying dependent code.
- Benefit: If we later swap CategoryServiceImpl with NewCategoryServiceImpl, we don’t need to change the controller.
- Since in controller we will be calling the interface not the impl file so, it makes it easier to change business logic or the implementation file.

    ```java
    // CategoryService.java
    public interface CategoryService {
        CategoryDTO deleteCategory(Long categoryId);
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
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    // deleteCategory is to delete category from the database
    @Override
    public CategoryDTO deleteCategory(Long categoryId){

        Category category = categoryRepository.findById(categoryId)
                  .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryDTO.class);
    }
    
}

```

- Here we are returning the CategoryDTO object as the return type.
- We are taking the categoryId as the input. Because we need the id of the product to work with.
- Then we are using the `findById`(Inbuilt JPARepo method) method to find the category with the given `id` to delete it.
- Then we will simply use the `delete` method from `JPARepository` to delete the data.
- Then we are mapping the data to categoryDTO using the modelMapper library.
- Then we return the deleted data to the user.

### Step 6 : Create the controller Layer :

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
    

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
            CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
            // return ResponseEntity.ok(status); - You can write it in these ways also
            // return ResponseEntity.status(HttpStatus.OK).body(status);
            // below is the most common one.
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }


}

```

- Now here we have the controller layer where we have the endpoint defined.
- This endpoint is the entry point to this function.
- Here we are first passing the `CategoryId` then we further call the service layer to perform the delete operation.
- Then using the ResponseEntity we are just simply giving back the user the deleted data.
- `Note` : This is the whole flow of this api.


