# 03. Functional & Non-Functional Requirements

## 1. Functional Requirements (FR)

### Module 1: Authentication & Identity Management
* **FR-1.1 (Registration):** The system shall allow users to register with a unique `username`, unique `email`, and a `password`. Optional role specification (`admin`, `seller`, or default `user`) is supported during sign-up.
* **FR-1.2 (Authentication):** The system shall authenticate users using username and password, returning user info and setting an HTTP-Only JWT cookie (`springBootEcommerce`) as well as returning a token string for Bearer header authentication.
* **FR-1.3 (Stateful Sign-out):** Upon user sign-out, the system shall record the current timestamp into `User.lastLogoutDate` in the database and clear the client HTTP cookie.
* **FR-1.4 (User Details):** Authenticated users shall be able to fetch their principal profile details (`userId`, `username`, assigned `roles`).

### Module 2: Category Management
* **FR-2.1 (Category Listing):** The system shall allow public access to list product categories with pagination (`pageNumber`, `pageSize`) and sorting (`sortBy`, `sortOrder`).
* **FR-2.2 (Category Creation):** Admins (`ROLE_ADMIN`) shall be able to create new categories. Duplicate category names shall be rejected with an exception.
* **FR-2.3 (Category Mutation):** Admins shall be able to update and delete existing categories by `categoryId`.

### Module 3: Product & Catalog Management
* **FR-3.1 (Product Add):** Sellers and Admins shall be able to add new products under a specific `categoryId`.
* **FR-3.2 (Price & Discount Calculation):** The system shall automatically compute `specialPrice` based on `price` and `discount` percentage: `specialPrice = price - (discount * 0.01 * price)`.
* **FR-3.3 (Catalog Search & Filter):** The system shall allow public users to browse all products, filter products by category, and perform case-insensitive keyword searches (`findByProductNameLikeIgnoreCase`).
* **FR-3.4 (Product Updates & Cart Propagation):** Updating product details (price, discount, description) shall automatically trigger price recalculations across all active shopping carts containing that product.
* **FR-3.5 (Product Deletion & Cart Purging):** Deleting a product shall automatically delete that product from all active customer shopping carts before deleting the product record.
* **FR-3.6 (Image Upload):** The system shall support image file uploads for products using multipart data, saving files to the configured media directory (`project.image=images/`).

### Module 4: Shopping Cart Management
* **FR-4.1 (Cart Initialization):** The system shall automatically instantiate a unique `Cart` bound to the authenticated user upon adding their first item.
* **FR-4.2 (Add Product to Cart):** Authenticated users shall be able to add products to their cart specifying a quantity. The system must validate stock availability before adding.
* **FR-4.3 (Cart Item Quantity Adjustment):** Users shall be able to increment (+1) or decrement (-1) product quantities in their cart. If item quantity reaches `0`, the item must be removed from the cart.
* **FR-4.4 (Cart Total Recalculation):** The system shall dynamically recalculate `Cart.totalPrice` whenever items are added, updated, or removed.

### Module 5: Order & Checkout Processing
* **FR-5.1 (Atomic Checkout):** Authenticated users shall be able to place an order specifying a valid delivery `addressId` and payment parameters (`paymentMethod`, `pgName`, `pgPaymentId`, `pgStatus`, `pgResponseMessage`).
* **FR-5.2 (Order & Payment Record):** The system shall create an `Order` record (status `"Order Accepted !"`) and associated `Payment` entity.
* **FR-5.3 (OrderItem Snapshotting):** The system shall snapshot each item in the user's cart into an `OrderItem` record preserving the price and discount at purchase time.
* **FR-5.4 (Inventory Stock Reduction):** Upon order placement, product stock (`Product.quantity`) in the database must be decremented by the purchased quantity.
* **FR-5.5 (Cart Purging):** The user's cart items shall be completely cleared upon successful order placement.

### Module 6: Address Book Management
* **FR-6.1 (Address CRUD):** Authenticated users shall be able to create, view, update, and delete their shipping/billing addresses (`street`, `buildingName`, `city`, `state`, `country`, `pincode`).
* **FR-6.2 (User Address Isolation):** Users shall only view and manage their personal address book entries.

---

## 2. Non-Functional Requirements (NFR)

* **NFR-1 (Security):**
  * Passwords must be hashed using `BCryptPasswordEncoder` before database persistence.
  * JWT tokens must be signed using HMAC-SHA with a Base64-encoded secret key.
  * Cookie storage must specify path `/api` and HTTP-Only protection.
* **NFR-2 (Data Integrity & ACID Guarantees):**
  * Critical workflows (order checkout, cart item updates, product deletions) must execute within Spring `@Transactional` boundaries.
  * Database entities must enforce foreign key integrity, unique constraints on `username` and `email`, and non-negative quantity constraints.
* **NFR-3 (Maintainability & Code Quality):**
  * Clean Layered Architecture (`Controller` ➔ `Service` ➔ `Repository` ➔ `Model`).
  * Strict decoupling between API payloads and JPA entities using `ModelMapper`.
  * Centralized constants (`AppConstants`, `OrderConstants`) eliminating hardcoded magic strings.
* **NFR-4 (Observability & Error Resilience):**
  * Centralized exception handling via `@RestControllerAdvice` (`MyGlobalExceptionHandler`).
  * Uniform error payload responses using `APIResponse` and HTTP status codes (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`).
* **NFR-5 (Self-Documentation):**
  * Complete REST API specification exposed via SpringDoc OpenAPI 3 UI at `/swagger-ui/index.html`.

---

## 3. Actor Permission Matrix

| Endpoint Pattern | Method | Anonymous | `ROLE_USER` | `ROLE_SELLER` | `ROLE_ADMIN` |
| :--- | :--- | :---: | :---: | :---: | :---: |
| `/api/auth/**` | `POST / GET` | ✅ | ✅ | ✅ | ✅ |
| `/api/public/**` | `GET` | ✅ | ✅ | ✅ | ✅ |
| `/api/admin/**` | `POST / PUT / DELETE` | ❌ | ❌ | ❌ | ✅ |
| `/api/carts/**` | `POST / GET / PUT / DELETE` | ❌ | ✅ | ✅ | ✅ |
| `/api/addresses/**` | `POST / GET / PUT / DELETE` | ❌ | ✅ | ✅ | ✅ |
| `/api/order/**` | `POST` | ❌ | ✅ | ✅ | ✅ |
| `/api/products/{id}/image` | `PUT` | ❌ | ❌ | ✅ | ✅ |
