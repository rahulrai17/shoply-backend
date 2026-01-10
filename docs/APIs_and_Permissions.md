# API Endpoints & Permission Matrix

This document serves as the **Source of Truth** for API security, role-based access control (RBAC), and URL naming conventions.

## 🎭 User Roles
*   **PUBLIC:** Unauthenticated Guest (e.g., browsing products).
*   **USER:** Authenticated Customer (e.g., shopping, checkout).
*   **SELLER:** Authenticated Merchant (e.g., managing own products/orders). *[Planned]*
*   **ADMIN:** System Administrator (e.g., managing categories, users, site-wide settings).

---

## 🔒 Permission Matrix

### 1. Authentication (`AuthController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Sign In | `POST` | `/api/auth/signin` | **PUBLIC** | Authenticate user & get JWT. |
| Sign Up | `POST` | `/api/auth/signup` | **PUBLIC** | Register a new account. |
| Sign Out | `POST` | `/api/auth/signout` | **USER+** | Invalidate session/cookie. |
| Get User | `GET` | `/api/auth/user` | **USER+** | Get current user details. |

### 2. Categories (`CategoryController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Browse Categories | `GET` | `/api/public/categories` | **PUBLIC** | View all categories. |
| Create Category | `POST` | `/api/admin/categories` | **ADMIN** | Create a new category. |
| Update Category | `PUT` | `/api/admin/categories/{id}` | **ADMIN** | Update an existing category. |
| Delete Category | `DELETE` | `/api/admin/categories/{id}` | **ADMIN** | Soft/Hard delete a category. |

### 3. Products (`ProductController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Browse Products | `GET` | `/api/public/products` | **PUBLIC** | View all products (paginated). |
| Search Products | `GET` | `/api/public/products/keyword/{kw}` | **PUBLIC** | Search by keyword. |
| Category Products | `GET` | `/api/public/categories/{id}/products` | **PUBLIC** | View products in a specific category. |
| Add Product | `POST` | `/api/admin/categories/{id}/product` | **ADMIN** | Add a product to the catalog. |
| Update Product | `PUT` | `/api/admin/products/{id}` | **ADMIN** | Update product details. |
| Update Image | `PUT` | `/api/admin/products/{id}/image` | **ADMIN** | Upload/Change product image. |
| Delete Product | `DELETE` | `/api/admin/products/{id}` | **ADMIN** | Remove a product. |

### 4. Shopping Cart (`CartController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Add to Cart | `POST` | `/api/carts/products/...` | **USER** | Add item to personal cart. |
| View My Cart | `GET` | `/api/carts/users/cart` | **USER** | View current user's cart. |
| Update Quantity | `PUT` | `/api/cart/products/...` | **USER** | Increment/Decrement quantity. |
| Remove Item | `DELETE` | `/api/carts/{id}/product/{id}` | **USER** | Remove specific item. |
| View All Carts | `GET` | `/api/admin/carts` | **ADMIN** | View all active carts (System Audit). |

### 5. Addresses (`AddressController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Create Address | `POST` | `/api/addresses` | **USER** | Add a new shipping address. |
| My Addresses | `GET` | `/api/users/addresses` | **USER** | List own addresses. |
| Get Address | `GET` | `/api/addresses/{id}` | **USER** | View specific address (Own only). |
| Update Address | `PUT` | `/api/addresses/{id}` | **USER** | Edit an address. |
| Delete Address | `DELETE` | `/api/addresses/{id}` | **USER** | Remove an address. |
| All Addresses | `GET` | `/api/admin/addresses` | **ADMIN** | View all user addresses (System Audit). |

### 6. Orders (`OrderController`)
| Endpoint | Method | Path | Access | Description |
| :--- | :--- | :--- | :--- | :--- |
| Place Order | `POST` | `/api/order/users/payments/...` | **USER** | Convert Cart to Order. |

---

## 🛠 Planned Improvements
*   **Seller Role:** Needs to be integrated into `ProductController`.
    *   Sellers should only Update/Delete *their own* products.
    *   Sellers should View Orders containing *their* products.
