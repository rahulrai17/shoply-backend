# Entity mapping 

- For shopping cart module we are working with 4 entities :
  1. Cart
  2. CartItem
  3. User
  4. product

## There relationships are as follows :

1. `User` and `cart` :
   - `Relationship` : `OneToOne`
   - `Direction` : 
     - A `user` has one `cart`.
     - A `Cart` belongs to one `User`
   - Mapped By : `user` field in `cart`
   - When we fetch the Cart we will be able to see the user_id column.

```java
// Cart.java
@OneToOne
@JoinColumn(name = "user_id")
private User user;
```

```java
// User.java
@ToString.Exclude
@OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        orphanRemoval = true)
private Cart cart;
```
---

2. `Cart` ↔ `CartItem`
   - `Relationship`: `One-to-Many`
   - `Direction`:
     - A `Cart` has many `CartItems` 
     - A `CartItem` belongs to one `Cart` 
     - Mapped By: `cart` field in `CartItem`

```java
// Cart.java
@OneToMany(mappedBy = "cart", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true)
private List<CartItem> cartItem = new ArrayList<>();
```

```java
// CartItem.java
@ManyToOne
@JoinColumn(name = "cart_id")
private Cart cart;
```

3. `CartItem` ↔ `Product`
   - `Relationship`: `Many-to-One` 
   - `Direction`:
     - A `CartItem` points to one `Product` 
     - A `Product` can be part of many `CartItems`

```java
// CartItem.java
@ManyToOne
@JoinColumn(name = "product_id")
private Product product;
```

```java
// Product.java
@OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
private List<CartItem> products = new ArrayList<>();
```


