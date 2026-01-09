# Category Implementation

- So This is how a beginner will design a category module for an E-commerce application.
- Here we will be creating APIs but without using database or other layers like service and all .
- If your understanding is clear here you can also implement this by making service layers and other things.
- From next tutorial we will move to implementing this with database and other layers.

Step 1 - We will be creating a Entity Class for Category.
```java
package com.shoply.backend.model;

public class Category {
    
    private Long categoryId;
    private String categoryName;
    
    public Category(Long categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

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
Step 2 - We will create a Controller Layer for Category.

```java
package com.shoply.backend.controller;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


// This is a Spring annotation that indicates this class is a controller where every method returns a domain object instead of a view.
// It's shorthand for @Controller and @ResponseBody rolled together.
@RestController
// This annotation specifies the base URL for all endpoints in this controller.
// In this case, all endpoints in this controller will start with "/api".
@RequestMapping("/api") 
public class CatergoryController {
    
    // This is a list that stores all categories
    private List<Category> categories = new ArrayList<>();

    // This annotation indicates that this method handles HTTP GET requests.
    // The value "/public/categories" specifies the URL path that this method will handle.
    @GetMapping("/public/categories")
    // This method returns all categories
    public List<Category> getAllCategories(){
        
        // Simply return the list of categories
        return categories;
    }

    // This annotation indicates that this method handles HTTP POST requests.
    // The value "/public/categories" specifies the URL path that this method will handle.
    @PostMapping("/public/categories")
    // This method creates a new category
    public String createCategory(@RequestBody Category category){
        // This annotation(@RequestBody) indicates that the method parameter "category" should be Taken from the request body.
       
        // Add the new category to the list
        categories.add(category);
        
        // Return a success message
        return "Category added Successfully";
    }

    // This annotation indicates that this method handles HTTP PUT requests.
    // The value "/public/categories/{categoryId}" specifies the URL path that this method will handle.
    // The "{categoryId}" part is a path variable that will be injected into the method parameter "categoryId".
    @PutMapping("/public/categories/{categoryId}")
    // This method updates an existing category
    public String updateCategory(@RequestBody Category category, @PathVariable Long categoryId){
        // This annotation(@RequestBody) indicates that the method parameter "category" should be Taken from the request body.
        // This annotation(@PathVariable) indicates that the method parameter "categoryId" should be injected from the URL path variable.
       
        // Loop through the list of categories to find the one with the matching ID
        for(Category c : categories){
            if(c.getCategoryId().equals(categoryId)){
                
                // Update the category with the new information
                c.setCategoryName(category.getCategoryName());
                
                // Return a success message
                return "Category with ID: " + categoryId + " Updated.";
            }
        }
        
        // If the category is not found, return an error message
        return "Category not found";
    }

    // This annotation indicates that this method handles HTTP DELETE requests.
    // The value "/admin/categories/{categoryId}" specifies the URL path that this method will handle.
    // The "{categoryId}" part is a path variable that will be injected into the method parameter "categoryId".
    @DeleteMapping("/admin/categories/{categoryId}")
    // This method deletes a category
    public String deleteCategory(@PathVariable Long categoryId){
        // This annotation(@PathVariable) indicates that the method parameter "categoryId" should be injected from the URL path variable.
        
        // Use a lambda expression to remove the category with the matching ID from the list
        categories.removeIf(c -> c.getCategoryId().equals(categoryId));
        
        // Return a success message
        return "Category with ID: " + categoryId + " deleted.";
    }
}
```

## Summary:





**Gist:**

This code sets up a basic CRUD (Create, Read, Update, Delete) system for managing categories in an e-commerce application. The `Category` entity class represents a single category, and the `CategoryController` class handles HTTP requests to perform operations on these categories.

**Why use an Entity Class?**

We used an entity class (`Category`) to encapsulate the data and behavior of a single category. This provides several benefits:

1. **Separation of Concerns**: By separating the data (category properties) from the business logic (controller methods), we can modify or replace either component independently without affecting the other.
2. **Reusability**: The `Category` class can be reused in other parts of the application, such as in a service layer or repository.
3. **Data Integrity**: The entity class ensures that each category has the required properties (e.g., `categoryId` and `categoryName`) and provides getter and setter methods to access and modify these properties.
4. **Easier Maintenance**: If we need to add or remove properties from the category, we can do so in a single place (the entity class), rather than updating multiple controller methods.

**Why use a Controller Class?**

We used a controller class (`CategoryController`) to handle HTTP requests and interact with the entity class. This provides several benefits:

1. **Decoupling**: The controller class decouples the HTTP requests from the business logic, allowing us to change the request handling without affecting the entity class.
2. **Single Responsibility Principle**: The controller class has a single responsibility (handling HTTP requests) and can be easily replaced or updated without affecting other parts of the application.
3. **Reusability**: The controller class can be reused to handle similar requests for other entities.

**Additional Layers:**

To further improve the architecture, we can add additional layers:

1. **Service Layer**: A service layer can be added to encapsulate the business logic and provide a layer of abstraction between the controller and the entity class. This layer can handle complex operations, such as validation, authentication, and authorization.
2. **Repository Layer**: A repository layer can be added to encapsulate data access and provide a layer of abstraction between the service layer and the database. This layer can handle CRUD operations and provide a standardized interface for data access.
3. **Data Access Object (DAO) Layer**: A DAO layer can be added to encapsulate data access and provide a layer of abstraction between the repository layer and the database. This layer can handle low-level database operations and provide a standardized interface for data access.
4. **API Gateway Layer**: An API gateway layer can be added to handle incoming requests, authenticate and authorize users, and route requests to the appropriate controller.

By adding these layers, we can further improve the scalability, maintainability, and security of the application.

Here's a high-level overview of the layers:
```
+---------------+
|  API Gateway  |
+---------------+
       |
       |
       v
+---------------+
|  Controller   |
+---------------+
       |
       |
       v
+---------------+
|  Service Layer |
+---------------+
       |
       |
       v
+---------------+
| Repository Layer |
+---------------+
       |
       |
       v
+---------------+
|  DAO Layer     |
+---------------+
       |
       |
       v
+---------------+
|  Database     |
+---------------+
```