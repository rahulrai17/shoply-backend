# Integration Steps: Swagger/OpenAPI (Phase 1)

This document outlines the specific steps we will take to integrate Swagger UI into `shoply-backend`.

## Step 1: Add Dependencies
We need to add the **SpringDoc** library, which automates OpenAPI generation for Spring Boot 3.

**File:** `pom.xml`
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Step 2: Configure JWT Security
Since our API uses JWT (Cookies), Swagger needs to know how to authenticate requests. We will create a configuration class.

**File:** `src/main/java/com/shoply/backend/config/SwaggerConfig.java`
**Key Actions:**
1.  Define the API Info (Title, Version).
2.  Define the **Security Scheme** (Tell Swagger to send the Cookie or Bearer Token).

## Step 3: Run & Verify
1.  Start the Spring Boot Application.
2.  Navigate to: `http://localhost:8080/swagger-ui/index.html`
3.  Check if all Controllers (Auth, Product, Cart) are listed.

## Step 4: Documentation Refinement (Optional but Recommended)
By default, Swagger generates docs from code. We can make them "Senior Level" by adding descriptions.

**Example Actions:**
*   Add `@Operation(summary = "Login User")` to `AuthController`.
*   Add `@ApiResponse(responseCode = "200", description = "Success")` to `OrderController`.
