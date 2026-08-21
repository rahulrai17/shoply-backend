# Shoply E-Commerce Platform — Exhaustive End-to-End Testing Guide (All 26 APIs)

This master testing guide covers **every single API endpoint** in the Shoply backend application. For each route, you will find:
* **HTTP Method & Path**
* **Swagger View Perspective** (`1. Customer Portal`, `2. Merchant Portal`, `3. Admin Portal`)
* **Role Permission Required** (`Public`, `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`)
* **Sample Request Payload / Parameters**
* **Expected Success Response** (`200 OK` / `201 CREATED`)
* **Error Test Cases & Expected Responses** (`400 BAD REQUEST`, `404 NOT FOUND`, `403 FORBIDDEN`, `401 UNAUTHORIZED`)

---

## Table of Contents
1. [Authentication & User Management APIs (4 Routes)](#1-authentication--user-management-apis)
2. [Category Management APIs (4 Routes)](#2-category-management-apis)
3. [Product Catalog Management APIs (7 Routes)](#3-product-catalog-management-apis)
4. [Address Book Management APIs (6 Routes)](#4-address-book-management-apis)
5. [Shopping Cart APIs (4 Routes)](#5-shopping-cart-apis)
6. [Order Processing & Checkout API (1 Route)](#6-order-processing--checkout-api)
7. [Master Error Response Matrix](#7-master-error-response-matrix)

---

## Baseline Test Credentials

| Account | Username | Password | Role |
| :--- | :--- | :--- | :--- |
| **Customer** | `user1` | `password1` | `ROLE_USER` |
| **Merchant Seller** | `seller1` | `password2` | `ROLE_SELLER` |
| **System Admin** | `admin` | `adminPass` | `ROLE_ADMIN` |

---

## 1. Authentication & User Management APIs

### 1.1 `POST /api/auth/signin`
* **Swagger View:** All Portals
* **Permission:** `Public`
* **Request Body:**
  ```json
  {
    "username": "user1",
    "password": "password1"
  }
  ```
* **Success Response (`200 OK`):**
  ```json
  {
    "id": 1,
    "username": "user1",
    "roles": ["ROLE_USER"],
    "jwtCookie": "springBootEcommerce=eyJhbGciOi..."
  }
  ```
* **Error Case (`401 UNAUTHORIZED`):** Invalid credentials (`username`: `"wrong"`, `"password"`: `"bad"`).

### 1.2 `POST /api/auth/signup`
* **Swagger View:** Customer Portal
* **Permission:** `Public`
* **Request Body:**
  ```json
  {
    "username": "customer2",
    "email": "customer2@example.com",
    "password": "Password123!",
    "role": ["user"]
  }
  ```
* **Success Response (`200 OK`):** `{"message": "User registered successfully!"}`
* **Error Case (`400 BAD REQUEST`):** Duplicate username `user1` ➔ `{"message": "Error: Username is already taken!", "status": false}`.

### 1.3 `GET /api/auth/user`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER` / `ROLE_SELLER` / `ROLE_ADMIN` (Requires Auth)
* **Success Response (`200 OK`):** Returns currently authenticated user details.
* **Error Case (`401 UNAUTHORIZED`):** Unauthenticated call ➔ `{"message": "User is not authenticated. Please log in first.", "status": false}`.

### 1.4 `POST /api/auth/signout`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER` / `ROLE_SELLER` / `ROLE_ADMIN`
* **Success Response (`200 OK`):** Clears authentication cookie and records `lastLogoutDate`.
* **Token Invalidation Test:** Any request made using the pre-logout token will fail with `401 UNAUTHORIZED` (`Rejected invalidated token`).

---

## 2. Category Management APIs

### 2.1 `GET /api/public/categories`
* **Swagger View:** Customer Portal
* **Permission:** `Public`
* **Query Parameters:** `pageNumber=0`, `pageSize=10`, `sortBy=categoryId`, `sortOrder=asc`
* **Success Response (`200 OK`):** Returns list of public categories.

### 2.2 `POST /api/admin/categories`
* **Swagger View:** Admin Portal
* **Permission:** `ROLE_ADMIN`
* **Request Body:**
  ```json
  {
    "categoryName": "Books & Stationery"
  }
  ```
* **Success Response (`201 CREATED`):** `{"categoryID": 3, "categoryName": "Books & Stationery"}`
* **Error Case A (`400 BAD REQUEST`):** Validation failure (`"categoryName": "Bo"`) ➔ `{"categoryName": "Category name must contain at least 3 characters"}`.
* **Error Case B (`400 BAD REQUEST`):** Duplicate category ➔ `{"message": "Category with the name Books & Stationery already exists !!!", "status": false}`.
* **Error Case C (`403 FORBIDDEN`):** Called by `user1` (`ROLE_USER`) ➔ `{"message": "Access Denied...", "status": false}`.

### 2.3 `PUT /api/admin/categories/{categoryId}`
* **Swagger View:** Admin Portal
* **Permission:** `ROLE_ADMIN`
* **Request Body:** `{"categoryName": "Home & Living"}`
* **Success Response (`200 OK`):** `{"categoryID": 3, "categoryName": "Home & Living"}`
* **Error Case (`404 NOT FOUND`):** `categoryId = 9999` ➔ `{"message": "Category not found with categoryId: 9999", "status": false}`.

### 2.4 `DELETE /api/admin/categories/{categoryId}`
* **Swagger View:** Admin Portal
* **Permission:** `ROLE_ADMIN`
* **Success Response (`200 OK`):** Returns deleted category DTO.

---

## 3. Product Catalog Management APIs

### 3.1 `GET /api/public/products`
* **Swagger View:** Customer Portal
* **Permission:** `Public`
* **Query Parameters:** `pageNumber=0`, `pageSize=10`, `sortBy=productId`, `sortOrder=asc`
* **Success Response (`200 OK`):** Returns list of public products.

### 3.2 `GET /api/public/Categories/{categoryId}/products`
* **Swagger View:** Customer Portal
* **Permission:** `Public`
* **Success Response (`200 OK`):** Returns products filtered by `categoryId`.

### 3.3 `GET /api/public/products/keyword/{keyword}`
* **Swagger View:** Customer Portal
* **Permission:** `Public`
* **Path Variable:** `keyword = Headphones`
* **Success Response (`302 FOUND` / `200 OK`):** Returns matching products.

### 3.4 `POST /api/admin/categories/{categoryId}/product`
* **Swagger View:** Merchant Portal / Admin Portal
* **Permission:** `ROLE_ADMIN` or `ROLE_SELLER`
* **Request Body:**
  ```json
  {
    "productName": "Bluetooth Gaming Earbuds",
    "description": "Ultra low latency wireless earbuds",
    "quantity": 40,
    "price": 60.00,
    "discount": 15.00
  }
  ```
* **Success Response (`201 CREATED`):** Returns saved product with `specialPrice = 51.00` and `seller_id` attached.

### 3.5 `PUT /api/admin/products/{productId}`
* **Swagger View:** Merchant Portal / Admin Portal
* **Permission:** `ROLE_ADMIN` or `ROLE_SELLER`
* **Success Response (`200 OK`):** Returns updated product.

### 3.6 `DELETE /api/admin/products/{productId}`
* **Swagger View:** Merchant Portal / Admin Portal
* **Permission:** `ROLE_ADMIN` or `ROLE_SELLER`
* **Success Response (`200 OK`):** Removes product and updates affected carts.

### 3.7 `PUT /api/products/{productId}/image`
* **Swagger View:** Merchant Portal / Admin Portal
* **Permission:** `ROLE_ADMIN` or `ROLE_SELLER`
* **Form-Data Parameter:** `image` (File picker)
* **Success Response (`200 OK`):** Returns product with updated image filename.

---

## 4. Address Book Management APIs

### 4.1 `POST /api/addresses`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Request Body:**
  ```json
  {
    "street": "123 Tech Boulevard",
    "buildingName": "Innovation Heights",
    "city": "San Francisco",
    "state": "California",
    "country": "United States",
    "pincode": "941051"
  }
  ```
* **Success Response (`201 CREATED`):** `{"addressId": 1, "street": "123 Tech Boulevard", ...}`

### 4.2 `GET /api/users/addresses`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Returns user addresses.

### 4.3 `GET /api/addresses/{addressId}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Returns specific address.

### 4.4 `GET /api/admin/addresses`
* **Swagger View:** Admin Portal
* **Permission:** `ROLE_ADMIN`
* **Success Response (`200 OK`):** Returns all system addresses.

### 4.5 `PUT /api/addresses/{addressId}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Returns updated address.

### 4.6 `DELETE /api/addresses/{addressId}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** `"Address deleted successfully with addressId: 1"`.

---

## 5. Shopping Cart APIs

### 5.1 `POST /api/carts/products/{productId}/quantity/{quantity}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Returns updated cart.

### 5.2 `GET /api/carts/users/cart`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Returns active cart details (auto-initializes empty cart if new).

### 5.3 `PUT /api/carts/products/{productId}/quantity/{operation}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Adjusts item quantity in cart.

### 5.4 `DELETE /api/carts/{cartId}/product/{productId}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Success Response (`200 OK`):** Removes item line from cart.

---

## 6. Order Processing & Checkout API

### 6.1 `POST /api/order/users/payments/{paymentMethod}`
* **Swagger View:** Customer Portal
* **Permission:** `ROLE_USER`
* **Request Body:**
  ```json
  {
    "addressId": 1,
    "pgName": "Stripe",
    "pgPaymentId": "tx_cc_987654",
    "pgStatus": "SUCCESS",
    "pgResponseMessage": "Payment Authorized Successfully"
  }
  ```
* **Success Response (`201 CREATED`):** Returns order with `"Order Accepted !"`.

---

## 7. Master Error Response Matrix

| Error Condition | Triggering API | HTTP Code | Returned JSON Error Structure |
| :--- | :--- | :--- | :--- |
| **Field Validation Failure** | `POST /api/addresses` | `400 BAD REQUEST` | `{"city": "City name must be at least 5 characters"}` |
| **Duplicate Entity Key** | `POST /api/auth/signup` | `400 BAD REQUEST` | `{"message": "Error: Username is already taken!", "status": false}` |
| **Resource Not Found** | `GET /api/addresses/9999` | `404 NOT FOUND` | `{"message": "Address not found with addressId: 9999", "status": false}` |
| **Role Access Denied** | `POST /api/admin/categories` (Customer token) | `403 FORBIDDEN` | `{"message": "Access Denied: You do not have permission to perform this action", "status": false}` |
| **Unauthenticated Request** | Protected API without token | `401 UNAUTHORIZED` | `{"message": "User is not authenticated. Please log in first.", "status": false}` |
