# The Ultimate Guide to Spring Boot 3 + Swagger (SpringDoc)

## 1. What is Swagger/OpenAPI?
*   **OpenAPI:** The *specification* (a standard JSON/YAML format) for describing REST APIs.
*   **Swagger:** The *tools* (like Swagger UI) that render this specification into a webpage.
*   **SpringDoc:** The *library* for Spring Boot that automatically looks at your code (`@RestController`, `@GetMapping`) and generates the OpenAPI JSON for you.

---

## 2. Core Concepts & Annotations

### A. Setup (`@OpenAPIDefinition`)
Instead of a complex XML file, we use a Java Config class.
```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
             .info(new Info().title("Shoply API").version("1.0"));
    }
}
```

### B. Documenting Controllers (`@Tag` & `@Operation`)
Use these to make the UI readable.

*   **`@Tag`**: Groups endpoints (e.g., "Product Management").
*   **`@Operation`**: Describes a specific API endpoint.

**Example:**
```java
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product API", description = "Manage catalog items")
public class ProductController {

    @GetMapping
    @Operation(
        summary = "Fetch all products",
        description = "Returns paginated list of products. Public access."
    )
    public ResponseEntity<List<ProductDTO>> getAll() { ... }
}
```

### C. Describing Data (`@Schema`)
Use this on your DTOs to show example values in the UI.

**Example:**
```java
public class LoginRequest {
    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "Password (min 6 chars)", example = "secret123")
    private String password;
}
```

---

## 3. Handling Security (JWT)
To test protected endpoints (like "Place Order") in Swagger, you need to configure the Security Scheme.

### The "Bearer Token" Approach
If you send tokens in the header (`Authorization: Bearer xyz`), configure this:

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
}
```

### The "Cookie" Approach (Shoply uses this)
Since Shoply uses HTTP-Only cookies, Swagger cannot automatically set them (browser security prevents JS from reading/setting HTTP-only cookies).
*   **Development:** You usually login via Postman or the Browser, and the Cookie is saved. When you refresh Swagger, the browser automatically sends the cookie.
*   **Production:** We often switch to "Bearer Tokens" for mobile apps, but stick to Cookies for Web.

---

## 4. Best Practices for Senior Devs
1.  **Don't expose internal models:** Always return DTOs, never Entities (`User.java` has the password hash!).
2.  **Define Error Responses:** Use `@ApiResponse` to tell the frontend what a "400 Bad Request" looks like.
    ```java
    @ApiResponse(responseCode = "404", description = "Product not found",
        content = @Content(schema = @Schema(implementation = APIResponse.class)))
    ```
3.  **Hide Internal APIs:** Use `@Hidden` on endpoints that shouldn't be public (e.g., internal health checks).
