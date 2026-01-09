# addProduct function

- This is for designing the product module.
- This the function that is called when we hit the Add api to add a product.

---

## Step 1 : Lets start with our database creation.

- This is configuration for H2 database you can configure any database and it is done in the `application.properties`

```properties
# application.properties
spring.application.name=shoply-backend

spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:test

#spring.jpa.show-sql=true
#spring.jpa.properties.hibernate.format_sql=true
```
---

## Step 2 : Creating an entity for the Product.

```java
// Product.java
package com.shoply.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;
    private String productName;
    private String image;
    private String description;
    private String quantity;
    private double price;
    private double discount;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

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

## Step 3 : Create a Repository layer

```java
// ProductRepository.java
package com.shoply.backend.repositories;

import com.shoply.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

```
- The Repository Layer is a crucial part of the Spring Boot architecture (or any layered architecture in Java applications). It acts as an abstraction over data access logic, making interactions with databases cleaner and more manageable.
- The repository layer hides complex SQL queries, ensuring that the service layer doesn't directly interact with the database.
- ORM frameworks like Spring Data JPA and Hibernate optimize queries using caching, lazy loading, and batching.

---

## Step 4 : Create DTO for the entity

```java
// ProductDTO
package com.shoply.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String image;
    private Integer quantity;
    private double price;
    private double discount;
    private double specialPrice;

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

## Step 5 : Create a Response layer for the category.

```java
//ProductResponse
package com.shoply.backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private List<ProductDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
```
- Now with this layer you might be thinking why are we creating so many layers for Data transfer.
- This is done so that we can promote loose coupling and follow best practices for software design like separation of concern(SoC).
- Now lets talk about This layer, this is the known as a response object.
- This is used to help Helps API consumers handle large data sets.
- We can use Response objects for various use cases.
- They can be used to improve API structure, error handling, and maintainability, etc.
- In this project we have used it for implementing pagination concept as you can see,
    - `content`: this will have the response according to dto.
    - `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `lastPage`: Pagination concepts.
- So to return a combined structure for this we have use Response Object.

---

## Step 6 : Create Service layer.

- Service layer consists of two files, here it is `ProductService`, `ProductServiceImpl`.
- We are using interface here which is given to the controller to call the functions.
- The `impl` class contains the main logic for the functions.

```java
// ProductService
package com.shoply.backend.service;

import com.shoply.backend.model.Product;
import com.shoply.backend.payload.ProductDTO;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, Product product);

}
```

```java
// ProductServiceImpl
package com.shoply.backend.service;

import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.Category;
import com.shoply.backend.model.Product;
import com.shoply.backend.payload.ProductDTO;
import com.shoply.backend.repositories.CategoryRepository;
import com.shoply.backend.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(Long categoryId, Product product) {
        // Getting category by category id for product as the product will have the category id in it.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId)); // this is our custom exception that we have created for these response only.

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() -
                ((product.getDiscount() * 0.01) * product.getPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);

        // mapping product to DTO class.
        return modelMapper.map(savedProduct, ProductDTO.class);
    }
}
```

- Here in the implementation we have implemented the logic for addProduct method.
  - We are getting `CategoryId`: to fetch the category from category table, `product` object for that the user send in body.
  - Category is mapped as `@manyToOne` relationship which means each product will have only one category.
  - This mapping is done in entity layer, you can check there.
  - `Next`, we are fetching the category.
  - If category is not found we throw a custom exception which we have made and defined in `ResourceNotFoundException.java`
  - and the above `Exception` file is called by another file `MyGlobalExceptionHandler.java`.
  - Since `MyGlobalExceptionHandler.java` is annotated by `@RestControllerAdvice`, This will intercept any exception throw by any controller in the application.
  - Which means we can create our exceptions then define it here and it will work smoothly.
  - `Next`, we use the `repository's` save method to add product to the database.
  - Then we map our product which is of Entity type to DTO type and send back to user.
  - Mapping is done using modelMapper library you can also implement it by custom mapping layer.

- Other files used here : 
```java
//ResourceNotFoundException.java
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

```java
// MyGlobalExceptionHandler.java
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
---

m

## Step 7 : Create a controller layer

```java
// ProductController.java
package com.shoply.backend.controller;

import com.shoply.backend.model.Product;
import com.shoply.backend.payload.ProductDTO;
import com.shoply.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody Product product,
                                                 @PathVariable Long categoryId){
        ProductDTO productDTO = productService.addProduct(categoryId, product);
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);

    }
}
```
- Now as the Api will be hit it will call the `addProduct` method from the service layer.
- From, controller we are getting the product as body of the request and `categoryId` form the Path.
- Then we have designed the service layer to handle these 
- `Note` : We are using `ResponseEntity` here, It is used to Send response to the user.