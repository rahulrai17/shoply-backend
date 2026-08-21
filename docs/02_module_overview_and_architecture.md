# 02. Module Overview & Architectural Context

## 1. High-Level Architectural Context

**Shoply Backend** follows a **Layered Monolith Architecture** adhering to clean code principles and separation of concerns. The application is structured into four primary layers:

```
+-----------------------------------------------------------------------+
|                            Client Applications                        |
|             (React Frontend, Mobile Apps, Postman, Swagger UI)        |
+-----------------------------------┬-----------------------------------+
                                    | HTTP / JSON (Cookies / Bearer Token)
                                    v
+-----------------------------------------------------------------------+
|                       Spring Security Filter Chain                    |
|          (WebSecurityConfig, AuthTokenFilter, AuthEntryPointJwt)       |
+-----------------------------------┬-----------------------------------+
                                    | Authenticated Principal / Context
                                    v
+-----------------------------------------------------------------------+
|                            Controller Layer                           |
|       (AuthController, CategoryController, ProductController, etc.)   |
+-----------------------------------┬-----------------------------------+
                                    | DTO Objects
                                    v
+-----------------------------------------------------------------------+
|                              Service Layer                            |
|       (AuthUtil, ProductServiceImpl, OrderServiceImpl, CartServiceImpl)|
+-----------------------------------┬-----------------------------------+
                                    | JPA Entities / Repositories
                                    v
+-----------------------------------------------------------------------+
|                         Persistence / Data Layer                      |
|         (Spring Data JPA Repositories, PostgreSQL / H2 Database)      |
+-----------------------------------------------------------------------+
```

---

## 2. Package & Module Breakdown

The codebase is organized under `com.shoply.backend` into distinct functional modules:

```
com.shoply.backend/
├── config/              # Central configuration (AppConfig, AppConstants, SwaggerConfig)
├── controller/          # REST Controllers handling HTTP requests & response status codes
├── exceptions/          # Global Exception Handler & Custom Exception definitions
├── model/               # JPA Domain Entities representing database tables
├── payload/             # DTOs (Data Transfer Objects) and Request/Response wrappers
├── repositories/        # Spring Data JPA Repository interfaces
├── security/            # Spring Security, JWT Utilities, UserDetails implementations
│   ├── jwt/             # AuthTokenFilter, JwtUtils, AuthEntryPointJwt
│   ├── request/         # LoginRequest, SignupRequest
│   ├── response/        # MessageResponse, UserInfoResponse
│   └── service/         # UserDetailsImpl, UserDetailsServiceImpl
├── service/             # Business Logic Layer Interfaces & Implementations
└── util/                # Security Context & Authentication helper utilities (AuthUtil)
```

---

## 3. Detailed Core Modules

### 1. Authentication & Security Module (`com.shoply.backend.security`)
* **Responsibility:** Handles user registration, credentials authentication, password encoding (BCrypt), JWT issuance, cookie handling, and stateful token revocation on logout.
* **Key Components:**
  * `WebSecurityConfig`: Configures `SecurityFilterChain`, path authorizations, password encoder, and seeds baseline roles/users via `CommandLineRunner`.
  * `JwtUtils`: Generates, parses, and validates JWT tokens signed with HMAC-SHA secret key.
  * `AuthTokenFilter`: Request filter executing per HTTP request to extract tokens from headers/cookies and enforce logout timestamp validation.
  * `AuthUtil`: Helper injected into business services to retrieve authenticated `User` entities directly from `SecurityContextHolder`.

### 2. Category Module (`com.shoply.backend.controller.CategoryController`)
* **Responsibility:** Manages global product categories taxonomy.
* **Key Components:**
  * `CategoryController`: Exposes public GET and admin POST/PUT/DELETE endpoints.
  * `CategoryServiceImpl`: Validates category duplicate names, interacts with `CategoryRepository`, maps to `CategoryDTO` and `CategoryResponse`.

### 3. Product & Catalog Module (`com.shoply.backend.controller.ProductController`)
* **Responsibility:** Product management, search, category filtering, seller pricing, discount calculation, and multipart image uploads.
* **Key Components:**
  * `ProductController`: Public catalog search/filtering endpoints and admin management routes.
  * `ProductServiceImpl`: Handles product creation, dynamic `specialPrice` calculation, keyword search (`findByProductNameLikeIgnoreCase`), category search, image upload integration (`FileServiceImpl`), and price cascade updates to active shopping carts (`updateProductInCarts`).

### 4. Shopping Cart Module (`com.shoply.backend.controller.CartController`)
* **Responsibility:** User cart creation, cart item addition, quantity adjustments (+1 / -1), item deletion, and cart balance synchronization.
* **Key Components:**
  * `CartController`: Exposes cart management endpoints for authenticated users and cart auditing for admins.
  * `CartServiceImpl`: Performs stock availability validation, creates/updates `CartItem` records, auto-recalculates total cart price, and purges zero-quantity items.

### 5. Order & Payment Module (`com.shoply.backend.controller.OrderController`)
* **Responsibility:** Orchestrates atomic order checkout processing, payment metadata generation, stock reduction, and cart purging.
* **Key Components:**
  * `OrderController`: Exposes order placement route (`POST /api/order/users/payments/{paymentMethod}`).
  * `OrderServiceImpl`: Executes `@Transactional` order workflow—linking user address, generating `Order` and `Payment` records, snapshotting `OrderItem` list, updating stock in `ProductRepository`, and deleting cart items.

### 6. Address Module (`com.shoply.backend.controller.AddressController`)
* **Responsibility:** Customer shipping and billing address book management.
* **Key Components:**
  * `AddressController`: Manages customer address CRUD endpoints.
  * `AddressServiceImpl`: Binds address entities to the logged-in user retrieved via `AuthUtil`.

---

## 4. Layer Interaction & Data Flow

```
[ HTTP Request ]
       │
       ▼
[ AuthTokenFilter ] ──(Validates JWT & Logout Timestamp)──► [ SecurityContextHolder ]
       │
       ▼
[ REST Controller ] ──(Validates Input DTO via @Valid)────► [ Service Layer ]
                                                                 │
                                                    (AuthUtil / Business Logic)
                                                                 │
                                                                 ▼
[ Spring Data Repository ] ◄──(JPA / SQL Queries)───────── [ Entity Model ]
       │
       ▼
[ PostgreSQL / H2 Database ]
```
