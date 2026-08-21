# 05. REST API Specification & Client Integration Flow

## 1. REST API Route Catalog

### Authentication API (`/api/auth`)
| Method | Endpoint Path | Authorization | Request Body | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `POST` | `/api/auth/signup` | Public | `SignupRequest` | `MessageResponse` | `200 OK` / `400 BAD_REQUEST` |
| `POST` | `/api/auth/signin` | Public | `LoginRequest` | `UserInfoResponse` + Cookie | `200 OK` / `404 NOT_FOUND` |
| `GET` | `/api/auth/user` | Authenticated | None | `UserInfoResponse` | `200 OK` / `401 UNAUTHORIZED` |
| `POST` | `/api/auth/signout` | Authenticated | None | `MessageResponse` + Clear Cookie | `200 OK` |

### Categories API (`/api`)
| Method | Endpoint Path | Authorization | Request Body / Query Params | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/public/categories` | Public | `pageNumber, pageSize, sortBy, sortOrder` | `CategoryResponse` | `200 OK` |
| `POST` | `/api/admin/categories` | `ROLE_ADMIN` | `CategoryDTO` | `CategoryDTO` | `201 CREATED` |
| `PUT` | `/api/admin/categories/{categoryId}` | `ROLE_ADMIN` | `CategoryDTO` | `CategoryDTO` | `200 OK` |
| `DELETE` | `/api/admin/categories/{categoryId}` | `ROLE_ADMIN` | Path `categoryId` | `CategoryDTO` | `200 OK` |

### Products API (`/api`)
| Method | Endpoint Path | Authorization | Request Body / Query Params | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/public/products` | Public | `pageNumber, pageSize, sortBy, sortOrder` | `ProductResponse` | `200 OK` |
| `GET` | `/api/public/Categories/{categoryId}/products` | Public | `categoryId, pageNumber, pageSize, sortBy, sortOrder` | `ProductResponse` | `200 OK` |
| `GET` | `/api/public/products/keyword/{keyword}` | Public | Path `keyword`, paging params | `ProductResponse` | `302 FOUND` |
| `POST` | `/api/admin/categories/{categoryId}/product` | `ROLE_ADMIN` / `ROLE_SELLER` | `ProductDTO`, Path `categoryId` | `ProductDTO` | `201 CREATED` |
| `PUT` | `/api/admin/products/{productId}` | `ROLE_ADMIN` / `ROLE_SELLER` | `ProductDTO`, Path `productId` | `ProductDTO` | `200 OK` |
| `DELETE` | `/api/admin/products/{productId}` | `ROLE_ADMIN` / `ROLE_SELLER` | Path `productId` | `ProductDTO` | `200 OK` |
| `PUT` | `/api/products/{productId}/image` | `ROLE_SELLER` / `ROLE_ADMIN` | Multipart `image` file | `ProductDTO` | `200 OK` |

### Shopping Cart API (`/api`)
| Method | Endpoint Path | Authorization | Request Body / Path Params | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `POST` | `/api/carts/products/{productId}/quantity/{quantity}` | Authenticated | Path `productId`, Path `quantity` | `CartDTO` | `201 CREATED` |
| `GET` | `/api/carts/users/cart` | Authenticated | None | `CartDTO` | `200 OK` |
| `GET` | `/api/admin/carts` | `ROLE_ADMIN` | None | `List<CartDTO>` | `302 FOUND` |
| `PUT` | `/api/cart/products/{productId}/quantity/{operation}` | Authenticated | Path `productId`, Path `operation` (`"update"`/`"delete"`) | `CartDTO` | `200 OK` |
| `DELETE` | `/api/carts/{cartId}/product/{productId}` | Authenticated | Path `cartId`, Path `productId` | `String` status | `200 OK` |

### Orders & Checkout API (`/api`)
| Method | Endpoint Path | Authorization | Request Body | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `POST` | `/api/order/users/payments/{paymentMethod}` | Authenticated | `OrderRequestDTO`, Path `paymentMethod` | `OrderDTO` | `201 CREATED` |

### Address Book API (`/api`)
| Method | Endpoint Path | Authorization | Request Body / Path Params | Success Response | HTTP Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| `POST` | `/api/addresses` | Authenticated | `AddressDTO` | `AddressDTO` | `201 CREATED` |
| `GET` | `/api/users/addresses` | Authenticated | None | `List<AddressDTO>` | `200 OK` |
| `GET` | `/api/addresses/{addressId}` | Authenticated | Path `addressId` | `AddressDTO` | `200 OK` |
| `GET` | `/api/admin/addresses` | `ROLE_ADMIN` | None | `List<AddressDTO>` | `200 OK` |
| `PUT` | `/api/addresses/{addressId}` | Authenticated | Path `addressId`, `AddressDTO` | `AddressDTO` | `200 OK` |
| `DELETE` | `/api/addresses/{addressId}` | Authenticated | Path `addressId` | `String` status | `200 OK` |

---

## 2. Recommended Frontend / Consumer Execution Flow

To achieve a complete end-to-end customer journey, frontend clients should interact with the API in the following sequence:

```
Step 1: POST /api/auth/signup           ───► Register User Account
Step 2: POST /api/auth/signin           ───► Authenticate & Obtain Cookie / JWT Token
Step 3: GET  /api/public/categories     ───► Fetch Category Catalog
Step 4: GET  /api/public/products       ───► Browse / Search Products
Step 5: POST /api/addresses             ───► Add Shipping Address to User Profile
Step 6: POST /api/carts/products/...    ───► Add Target Product to Cart
Step 7: GET  /api/carts/users/cart      ───► Review Cart & Total Price
Step 8: POST /api/order/users/payments  ───► Execute Checkout & Place Order
```

---

## 3. Standardized Error Formats

### 1. Validation Failures (`MethodArgumentNotValidException`) — `400 BAD REQUEST`
```json
{
  "street": "Street name must be atleast 5 character",
  "email": "must be a well-formed email address"
}
```

### 2. Domain & Resource Exceptions (`ResourceNotFoundException`, `APIException`)
```json
{
  "message": "Product Robot not available in the cart!!!",
  "status": false
}
```
