# 04. Data Schema & Entity-Relationship (ER) Model

## 1. Complete Entity-Relationship Diagram

![](images/schema-diagram.png)

---

## 2. Entity Field Specifications

### 1. `User` Entity (`users` table)
* `user_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Unique user identifier.
* `username` (`String`, Unique, Not Blank, max 20 chars): User handle.
* `email` (`String`, Unique, Not Blank, max 50 chars, `@Email`): Account email address.
* `password` (`String`, Not Blank, max 120 chars): BCrypt-encoded password hash.
* `last_logout_date` (`LocalDateTime`): Timestamp of user's last explicit sign-out for stateful JWT validation.
* **Relationships:**
  * `@ManyToMany(fetch = FetchType.EAGER)` with `Role` via join table `user_role`.
  * `@OneToMany(mappedBy = "user", cascade = {PERSIST, MERGE}, orphanRemoval = true)` with `Address`.
  * `@OneToOne(mappedBy = "user", cascade = {PERSIST, MERGE}, orphanRemoval = true)` with `Cart`.
  * `@OneToMany(mappedBy = "user", cascade = {PERSIST, MERGE}, orphanRemoval = true)` with `Product` (as Seller).

### 2. `Role` Entity (`roles` table) & `AppRole` Enum
* `role_id` (`Integer`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Role identifier.
* `role_name` (`AppRole` Enum): Role designation (`ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`).

### 3. `Address` Entity (`addresses` table)
* `address_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Address identifier.
* `street` (`String`, Not Blank, min 5 chars): Street address.
* `building_name` (`String`, Not Blank, min 5 chars): Building or apartment name.
* `city` (`String`, Not Blank, min 5 chars): City name.
* `state` (`String`, Not Blank, min 2 chars): State/Province name.
* `country` (`String`, Not Blank, min 5 chars): Country name.
* `pincode` (`String`, Not Blank, min 6 chars): Zip/Postal code.
* `user` (`@ManyToOne`, FK `user_id`): Owner user reference.

### 4. `Category` Entity (`categories` table)
* `category_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Category identifier.
* `category_name` (`String`, Not Blank, min 5 chars): Unique category title.
* `products` (`@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)`): Child products list.

### 5. `Product` Entity (`products` table)
* `product_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Product identifier.
* `product_name` (`String`, Not Blank, min 3 chars): Product title.
* `description` (`String`, Not Blank, min 6 chars): Product description text.
* `image` (`String`): Image filename stored in `/images/`.
* `quantity` (`Integer`): In-stock quantity count.
* `price` (`double`): Base unit price.
* `discount` (`double`): Discount percentage (0.0 to 100.0).
* `special_price` (`double`): Discounted unit price calculated as `price - (discount * 0.01 * price)`.
* `category` (`@ManyToOne`, FK `category_id`): Category reference.
* `user` (`@ManyToOne`, FK `seller_id`): Merchant seller user reference.

### 6. `Cart` Entity (`carts` table)
* `cart_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Cart identifier.
* `user` (`@OneToOne`, FK `user_id`): Associated user reference.
* `cartItems` (`@OneToMany(mappedBy = "cart", cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true)`): Active items list.
* `total_price` (`Double`): Sum total of all items in cart.

### 7. `CartItem` Entity (`cart_item` table)
* `cart_item_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Cart item identifier.
* `cart` (`@ManyToOne`, FK `cart_id`): Parent cart reference.
* `product` (`@ManyToOne`, FK `product_id`): Product reference.
* `quantity` (`Integer`): Selected quantity.
* `discount` (`Double`): Discount snapshot.
* `product_price` (`Double`): Price per unit snapshot.

### 8. `Order` Entity (`orders` table)
* `order_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Order identifier.
* `email` (`String`, Not Null, `@Email`): Customer email.
* `order_date` (`LocalDate`): Order placement date.
* `total_amount` (`Double`): Final order total.
* `order_status` (`String`): Processing status (e.g., `"Order Accepted !"`).
* `payment` (`@OneToOne`, FK `payment_id`): Payment metadata reference.
* `address` (`@ManyToOne`, FK `address_id`): Shipping address reference.
* `orderItems` (`@OneToMany(mappedBy = "order", cascade = {PERSIST, MERGE})`): Ordered items list.

### 9. `OrderItem` Entity (`order_items` table)
* `order_item_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Order item identifier.
* `order` (`@ManyToOne`, FK `order_id`): Parent order reference.
* `product` (`@ManyToOne`, FK `product_id`): Purchased product reference.
* `quantity` (`Integer`): Purchased quantity.
* `discount` (`double`): Applied discount snapshot.
* `ordered_product_price` (`double`): Applied price per unit snapshot.

### 10. `Payment` Entity (`payments` table)
* `payment_id` (`Long`, PK, `@GeneratedValue(strategy = GenerationType.IDENTITY)`): Payment record identifier.
* `payment_method` (`String`, Not Blank, min 4 chars): e.g., `"Credit Card"`, `"PayPal"`, `"UPI"`.
* `pg_payment_id` (`String`): Payment gateway transaction ID.
* `pg_status` (`String`): Payment gateway status code/text.
* `pg_response_message` (`String`): Payment gateway response message.
* `pg_name` (`String`): Gateway provider name (e.g., `"Stripe"`, `"Razorpay"`).
