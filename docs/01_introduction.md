# 01. Project Introduction & Business Domain

## 1. Executive Summary
**Shoply Backend** is a multi-vendor e-commerce REST API built using **Java 17** and **Spring Boot 3.4.2**. It handles customer shopping, merchant inventory management, and system administration in a single clean backend service.

The system provides user registration, login, role-based security, shopping cart balance tracking, atomic order checkout, and clear RESTful APIs.

---

## 2. Business Model & Multi-Vendor Ecosystem

Unlike simple single-store apps, **Shoply** works as a **Multi-Vendor Marketplace** with three user roles:

```
                      +-------------------+
                      |   Shoply Platform |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
        v                       v                       v
+---------------+       +---------------+       +---------------+
|   Customer    |       |    Seller     |       |     Admin     |
| (Storefront)  |       |  (Merchant)   |       |  (Governance) |
+---------------+       +---------------+       +---------------+
```

### Users & Features

1. **Customer (`ROLE_USER`):**
   * Browses categories and products using search keywords, category filters, and sorting.
   * Manages a personal shopping cart (adds products, updates item quantities, removes items).
   * Saves shipping and billing addresses in an address book.
   * Places orders using an atomic checkout process with simulated payment processing.

2. **Seller / Merchant (`ROLE_SELLER`):**
   * Manages product listings (adds products, sets stock, updates prices, and configures discounts).
   * Uploads product images to media storage.

3. **Administrator (`ROLE_ADMIN`):**
   * Manages categories (creates, updates, and deletes product categories).
   * Manages system-wide user roles.
   * Audits active user carts and platform-wide order histories.

---

## 3. Core Problems & How Shoply Solves Them

| Real-World Problem | How Shoply Solves It |
| :--- | :--- |
| **Token Theft & Security Attacks** | Stores authentication tokens in secure HTTP-Only cookies so malicious scripts cannot steal them. |
| **Stolen Tokens Used After Logout** | Checks the logout timestamp on every request. If a token was issued before the user clicked log out, it gets blocked immediately. |
| **Cart Overselling & Order Errors** | Uses single-transaction checkouts (`@Transactional`). If payment or inventory updates fail, everything rolls back safely without losing money or stock. |
| **Confusing API Documentation** | Organizes Swagger UI into 3 clean, role-specific views (Customer, Seller, and Admin) with clear step-by-step instructions. |

---

## 4. Key Goals

1. **Production Quality:** Enforce clean security, database integrity, simple error handling, and automated integration tests.
2. **Clean Code Architecture:** Keep a clear separation of concerns (`Controller` ➔ `Service` ➔ `Repository` ➔ `Entity`).
3. **Data Protection:** Use DTOs (`ModelMapper`) so database entities are never exposed directly over public APIs.
4. **Developer Experience:** Provide easy-to-use API documentation via Swagger UI.
