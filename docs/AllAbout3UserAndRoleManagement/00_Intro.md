# User and Role Management

- This is the `third part` of our project after we have worked on `category` and `product`.
- Here we will try to implement the Spring Security part of the code(User Authentication and User Management).

## Let's understand how User table is managed in this project.

1. We have a user table which has :
    - `user_id`
    - `email`
    - `password`
    - `username`
   
2. We have different tables linked to user:
   - `user_role`
   - `user_address`
   - also `user_role` is linked to `roles` and 
   - `user_address` is linked to `address`.
   
3. `roles table` has information like :
   - `role_id`
   - `role_name`
   - All the types of roles are going to be managed by this. eg : `admin`, `user`, `seller` etc
   - We are linking those table we use `user_role` also known as junction table.
   - This is used to link the both user table and roles table using `user_id` and `roles_id`.
   - This is done by JPA.
   - This gives the condition for many-to-many relationship.
   
4. `address table` has information like :
   - `address_id`
   - `building_name`
   - `city`
   - `country`
   - `pincode`
   - `state`
   - `street`
   - We are linking those table we use `user_address` also known as junction table.
   - This is used to link the both user table and roles table using `user_id` and `address_id`.
   - This is done by JPA.
   - This gives the condition for many-to-many relationship.

## Let's start with the creation of these Entities and setting up there relationship :

- `User` Entity :
   ```java
   package com.shoply.backend.model;
   
   import jakarta.persistence.*;
   import jakarta.validation.constraints.Email;
   import jakarta.validation.constraints.NotBlank;
   import jakarta.validation.constraints.Size;
   import lombok.*;
   
   import java.util.ArrayList;
   import java.util.HashSet;
   import java.util.List;
   import java.util.Set;
   
   @Entity
   @Data
   @NoArgsConstructor
   @Table(name = "users",
           uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
           })
   public class User {
   
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       @Column(name = "user_id")
       private Long userId;
   
       @NotBlank
       @Size(max = 20)
       @Column(name = "username")
       private String userName;
   
       @NotBlank
       @Size(max = 50)
       @Email
       @Column(name = "email")
       private String email;
   
       @NotBlank
       @Size(max = 120)
       @Column(name = "password")
       private String password;
   
       public User(String userName, String email, String password) {
           this.userName = userName;
           this.email = email;
           this.password = password;
       }
   
       @Setter
       @Getter
       @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
               fetch = FetchType.EAGER)
       @JoinTable(name = "user_role",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
       private Set<Role> roles = new HashSet<>();
   
       @Setter
       @Getter
       @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
       @JoinTable(name = "user_address",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "address_id"))
       private List<Address> addresses = new ArrayList<>();
   
       @ToString.Exclude
       @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
               orphanRemoval = true)
       private Set<Product> products;
   
   
   }
   
   ```

- `Role` Entity :
   ```java
   package com.shoply.backend.model;
   
   import jakarta.persistence.*;
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   import lombok.ToString;
   
   @Entity
   @NoArgsConstructor
   @AllArgsConstructor
   @Data
   @Table(name = "roles")
   public class Role {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       @Column(name = "role_id")
       private Integer roleId;
   
       @ToString.Exclude
       @Enumerated(EnumType.STRING)
       @Column(length = 20, name = "role_name")
       private AppRole roleName;
   
       public Role(AppRole roleName) {
           this.roleName = roleName;
       }
   }
   
   ```
  
- `Address` Entity :
   ```java
   package com.shoply.backend.model;
   
   import jakarta.persistence.*;
   import jakarta.validation.constraints.NotBlank;
   import jakarta.validation.constraints.Size;
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   import lombok.ToString;
   
   import java.util.ArrayList;
   import java.util.List;
   
   @Entity
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @Table(name = "addresses")
   public class Address {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long addressId;
   
       @NotBlank
       @Size(min = 5, message = "Street name must be atleast 5 character")
       private String street;
   
       @NotBlank
       @Size(min = 5, message = "Building name must be atleast 5 characters")
       private String buildingName;
   
       @NotBlank
       @Size(min = 5, message = "City name must be atleast 5 characters")
       private String city;
   
       @NotBlank
       @Size(min = 2, message = "State name must be atleast 2 characters")
       private String state;
   
       @NotBlank
       @Size(min = 2, message = "Country name must be atleast 5 characters")
       private String country;
   
       @NotBlank
       @Size(min = 6, message = "Pincode name must be atleast 5 characters")
       private String pincode;
   
       @ToString.Exclude
       @ManyToMany(mappedBy = "addresses")
       private List<User> users = new ArrayList<>();
   
       public Address(String street, String buildingName, String city, String state, String country, String pincode, List<User> users) {
           this.street = street;
           this.buildingName = buildingName;
           this.city = city;
           this.state = state;
           this.country = country;
           this.pincode = pincode;
           this.users = users;
       }
   }
   
   ```
  
- Also made some change in `Product` entity :
   ```java
   package com.shoply.backend.model;
   
   import jakarta.persistence.*;
   import jakarta.validation.constraints.NotBlank;
   import jakarta.validation.constraints.Size;
   import lombok.AllArgsConstructor;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   import lombok.ToString;
   
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @Entity
   @Table(name = "products")
   @ToString
   public class Product {
       @Id
       @GeneratedValue(strategy = GenerationType.AUTO)
       private Long productId;
   
       @NotBlank
       @Size(min = 3, message = "Product name must contain atleast 3 characters. ")
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
   
       // New added after creation of user
       @ManyToOne
       @JoinColumn(name = "seller_id")
       private User user;
   
   }
   
   ```
   - Here we have updated added the `user` and `product` relationship.
