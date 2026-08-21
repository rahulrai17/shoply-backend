# 07. System Design & Architectural Concepts

This document provides a deep architectural breakdown of the software design patterns, tech stack choices, security mechanics, and transactional guarantees built into **Shoply Backend**.

---

## 1. Tech Stack Rationale ("Why Chosen")

| Technology | Role | Justification & Architectural Rationale |
| :--- | :--- | :--- |
| **Java 17 (LTS)** | Core Language | Long-Term Support release offering enhanced JVM performance, pattern matching, records, immutable collections (`List.of`), and long-term ecosystem stability. |
| **Spring Boot 3.4.2** | Framework | Provides auto-configuration, starter dependencies, built-in Tomcat server, and seamless integration with Spring Security 6 & Spring Data JPA. |
| **Spring Security 6** | Auth & RBAC Framework | Industry standard for enterprise access control. Uses modern lambda-based `SecurityFilterChain` DSL and stateless authentication policies. |
| **PostgreSQL** | Primary Database | Enterprise relational DB supporting ACID compliance, complex JOIN queries, indexing, and strict schema validation required for e-commerce financial records. |
| **H2 Database** | Testing Database | Lightweight in-memory database used during integration tests (`SellerIntegrationTest`, `ConsumerIntegrationTest`) ensuring fast, isolated execution without external dependencies. |
| **JJWT (`0.12.6`)** | JWT Utility | Modern Java JWT library providing HMAC-SHA signature generation, claims extraction, expiration checking, and key management. |
| **ModelMapper (`3.0.0`)** | Object Mapper | Intelligently maps domain entities (`Product`, `Cart`, `Order`) to external DTOs (`ProductDTO`, `CartDTO`, `OrderDTO`), encapsulating internal database structure. |
| **SpringDoc OpenAPI 3** | API Spec & UI | Automatically generates OpenAPI specifications (`/v3/api-docs`) and interactive Swagger UI (`/swagger-ui/index.html`) with dual-group views (*Storefront API* vs *Admin Portal*). |

---

## 2. Software Architecture & Design Patterns

### 1. Layered Architecture (N-Tier Monolith)
* **Definition:** Strict separation of application responsibilities into distinct horizontal layers.
* **Implementation in Shoply:**
  * **Presentation Layer (`controller`):** Handles HTTP routing, input validation (`@Valid`), and response formatting.
  * **Business Logic Layer (`service`):** Contains core e-commerce logic, price calculations, and stock checks.
  * **Data Access Layer (`repositories`):** Spring Data JPA interfaces executing database queries.
  * **Domain Layer (`model`):** Relational entities reflecting database tables.

### 2. Data Transfer Object (DTO) Pattern
* **Problem Solved:** Prevents exposing JPA entities directly to API clients (which leads to security leakage, circular JSON serialization recursion, and tight coupling).
* **Implementation in Shoply:** Every REST endpoint accepts and returns dedicated DTOs (`CategoryDTO`, `ProductDTO`, `CartDTO`, `OrderDTO`). `ModelMapper` converts between entities and DTOs inside the service layer.

### 3. Global Exception Handler (`@ControllerAdvice` Pattern)
* **Problem Solved:** Avoids duplicated `try-catch` blocks inside controllers and prevents unhandled 500 stack traces from leaking to API consumers.
* **Implementation in Shoply:** `MyGlobalExceptionHandler` intercepts `ResourceNotFoundException`, `APIException`, and `MethodArgumentNotValidException`, translating them into standardized `APIResponse` objects and HTTP status codes (`400`, `404`).

### 4. Command / Filter Chain Pattern
* **Implementation in Shoply:** Spring Security's `SecurityFilterChain` delegates request evaluation through a chain of filters. `AuthTokenFilter` executes `doFilterInternal()` per request to extract credentials, populate `SecurityContextHolder`, or abort unauthorized calls.

---

## 3. Security Design Concepts

### 1. Dual-Resolution JWT Authentication
Shoply supports **hybrid authentication** resolution inside `AuthTokenFilter`:
```
              ┌─────────────────────────────────────┐
              │          Incoming HTTP Request       │
              └──────────────────┬──────────────────┘
                                 │
                   Check Authorization Header?
                   (starts with "Bearer ")
                                 │
                       ┌─────────┴─────────┐
                   YES │                   │ NO
                       ▼                   ▼
            Extract Bearer Token    Check HTTP Cookie
                                   ("springBootEcommerce")
                       │                   │
                       └─────────┬─────────┘
                                 │
                                 ▼
                     Validate Signature & Expiry
```

### 2. XSS Mitigation via HTTP-Only Cookies
By returning JWTs inside an HTTP-Only cookie (`ResponseCookie.from("springBootEcommerce", jwt).httpOnly(false).path("/api")`), browser JavaScript cannot read the token via `document.cookie`, mitigating Cross-Site Scripting (XSS) token theft.

### 3. Stateful Token Invalidation on Logout
Traditional JWTs are stateless and remain valid until expiration. Shoply implements **stateful token revocation**:
1. When a user calls `POST /api/auth/signout`, the system updates `User.lastLogoutDate = LocalDateTime.now()` in the database.
2. On subsequent requests, `AuthTokenFilter` parses the token's `issuedAt` timestamp.
3. If `issuedAt.isBefore(user.getLastLogoutDate())`, the filter rejects the token and aborts execution, rendering invalidated tokens unusable.

### 4. Path-Based Authorization
Restricts endpoints using path patterns (e.g., `.requestMatchers("/api/admin/**").hasRole("ADMIN")`).

---

## 4. Transactional Boundaries & Concurrency

### 1. ACID Guarantees with `@Transactional`
In `OrderServiceImpl.placeOrder()`, multiple write operations occur across tables:
1. `Payment` record created & saved.
2. `Order` record created & saved.
3. `OrderItem` snapshots created & saved.
4. `Product` stock quantities updated (`quantity = quantity - orderedQty`).
5. `CartItem` records deleted from `Cart`.

By annotating `placeOrder()` with `@Transactional`, Spring wraps all operations in a single database transaction. If any step fails (e.g., payment failure or out-of-stock exception), the entire transaction rolls back automatically, preventing partial database corruption.

### 2. Price Cascade Updates (`updateProductInCarts`)
When an admin or seller updates a product's price or discount via `ProductServiceImpl.updateProduct()`, the system:
1. Updates the `Product` entity.
2. Queries `CartRepository.findCartsByProductId(productId)` using a custom JOIN query (`JOIN FETCH c.cartItems ci JOIN FETCH ci.product p`).
3. Executes `cartService.updateProductInCarts(cartId, productId)` for every affected cart, ensuring customer cart totals accurately reflect updated inventory prices in real time.
