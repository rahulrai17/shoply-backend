package com.shoply.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
                .group("1. Customer Portal (User APIs)")
                .pathsToMatch("/api/public/**", "/api/auth/**", "/api/carts/**", "/api/cart/**", "/api/addresses/**", "/api/order/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.setInfo(new Info()
                            .title("Shoply - Customer Portal (User APIs)")
                            .version("1.0.0")
                            .description("### Customer Storefront API Reference\n\n" +
                                    "---\n\n" +
                                    "### Default Credentials:\n" +
                                    "* **Pre-seeded Account:** `username`: `user1` | `password`: `password1` (`ROLE_USER`)\n\n" +
                                    "### Dedicated Customer Registration Payload (`POST /api/auth/signup`):\n" +
                                    "```json\n" +
                                    "{\n" +
                                    "  \"username\": \"new_customer\",\n" +
                                    "  \"email\": \"customer@example.com\",\n" +
                                    "  \"password\": \"customerPass123!\",\n" +
                                    "  \"role\": [\"user\"]\n" +
                                    "}\n" +
                                    "```\n\n" +
                                    "---\n\n" +
                                    "### Recommended Execution Sequence:\n" +
                                    "1. **Authenticate:** Call `POST /api/auth/signin` (`user1`/`password1`) and copy the raw JWT token string.\n" +
                                    "2. **Authorize Session:** Click the **Authorize** button in the top right and paste the token string.\n" +
                                    "3. **Browse Catalog:** Call `GET /api/public/products` to view available products.\n" +
                                    "4. **Add Address:** Call `POST /api/addresses` to register a shipping address.\n" +
                                    "5. **Manage Cart:** Call `POST /api/carts/products/1/quantity/2` to add items to cart.\n" +
                                    "6. **Update Quantity:** Call `PUT /api/cart/products/1/quantity/delete` or `add` to adjust item quantity.\n" +
                                    "7. **Process Checkout:** Call `POST /api/order/users/payments/CreditCard` to place an order."));
                    sortTagsAlphabetically(openApi);
                })
                .build();
    }

    @Bean
    public GroupedOpenApi sellerApi() {
        return GroupedOpenApi.builder()
                .group("2. Merchant Portal (Seller APIs)")
                .pathsToMatch("/api/auth/**", "/api/public/**", "/api/admin/categories/*/product", "/api/admin/products/*", "/api/products/*/image")
                .addOpenApiCustomizer(openApi -> {
                    openApi.setInfo(new Info()
                            .title("Shoply - Merchant Portal (Seller APIs)")
                            .version("1.0.0")
                            .description("### Merchant Inventory & Catalog API Reference\n\n" +
                                    "---\n\n" +
                                    "### Default Credentials:\n" +
                                    "* **Pre-seeded Account:** `username`: `seller1` | `password`: `password2` (`ROLE_SELLER`)\n\n" +
                                    "### Dedicated Merchant Registration Payload (`POST /api/auth/signup`):\n" +
                                    "```json\n" +
                                    "{\n" +
                                    "  \"username\": \"new_merchant\",\n" +
                                    "  \"email\": \"merchant@shoply.com\",\n" +
                                    "  \"password\": \"merchantPass123!\",\n" +
                                    "  \"role\": [\"seller\"]\n" +
                                    "}\n" +
                                    "```\n\n" +
                                    "---\n\n" +
                                    "### Recommended Execution Sequence:\n" +
                                    "1. **Authenticate:** Call `POST /api/auth/signin` (`seller1`/`password2`) and copy the raw JWT token string.\n" +
                                    "2. **Authorize Session:** Click the **Authorize** button in the top right and paste the token string.\n" +
                                    "3. **Add Product Listing:** Call `POST /api/admin/categories/1/product` to list a new product under Category 1.\n" +
                                    "4. **Upload Image:** Call `PUT /api/products/1/image` to attach a product image.\n" +
                                    "5. **Update Inventory:** Call `PUT /api/admin/products/1` to modify pricing, stock quantity, or discount parameters."));
                    sortTagsAlphabetically(openApi);
                })
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("3. Admin Portal (Administrator APIs)")
                .pathsToMatch("/api/admin/**", "/api/auth/**", "/api/public/**", "/api/carts/**", "/api/cart/**", "/api/addresses/**", "/api/order/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.setInfo(new Info()
                            .title("Shoply - Admin Portal (System Admin APIs)")
                            .version("1.0.0")
                            .description("### System Governance & Administration API Reference\n\n" +
                                    "---\n\n" +
                                    "### Default Credentials:\n" +
                                    "* **Pre-seeded Account:** `username`: `admin` | `password`: `adminPass` (`ROLE_ADMIN`)\n\n" +
                                    "### Dedicated Admin Registration Payload (`POST /api/auth/signup`):\n" +
                                    "```json\n" +
                                    "{\n" +
                                    "  \"username\": \"new_admin\",\n" +
                                    "  \"email\": \"admin@shoply.com\",\n" +
                                    "  \"password\": \"adminSecretPass123!\",\n" +
                                    "  \"role\": [\"admin\"]\n" +
                                    "}\n" +
                                    "```\n\n" +
                                    "---\n\n" +
                                    "### Recommended Execution Sequence:\n" +
                                    "1. **Authenticate:** Call `POST /api/auth/signin` (`admin`/`adminPass`) and copy the raw JWT token string.\n" +
                                    "2. **Authorize Session:** Click the **Authorize** button in the top right and paste the token string.\n" +
                                    "3. **Create Category:** Call `POST /api/admin/categories` to define system category taxonomy.\n" +
                                    "4. **Audit Carts:** Call `GET /api/admin/carts` to inspect active customer carts.\n" +
                                    "5. **Audit Addresses:** Call `GET /api/admin/addresses` to view registered shipping addresses."));
                    sortTagsAlphabetically(openApi);
                })
                .build();
    }

    @Bean
    public OpenAPI springShoplyOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Shoply Multi-Vendor E-Commerce API")
                        .version("1.0.0")
                        .contact(new Contact().name("Rahul Rai").email("rahulrai200017@example.com").url("https://github.com/rahulrai17"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .servers(List.of(
                        new Server().url("/").description("Default Server"),
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .tags(List.of(
                        new Tag().name("Authentication").description("User login, registration, profile lookup, and stateful signout"),
                        new Tag().name("Categories").description("Catalog taxonomy management for product categories"),
                        new Tag().name("Products").description("Browse, search, and manage product inventory & image uploads"),
                        new Tag().name("Shopping Cart").description("Manage items, quantities, and totals in active shopping carts"),
                        new Tag().name("Addresses").description("Customer shipping and billing address book management"),
                        new Tag().name("Orders").description("Atomic order checkout processing and order management")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token string (excluding 'Bearer ' prefix).")));
    }

    private void sortTagsAlphabetically(OpenAPI openApi) {
        List<String> order = List.of("Authentication", "Categories", "Products", "Shopping Cart", "Addresses", "Orders");
        if (openApi.getTags() != null) {
            openApi.setTags(openApi.getTags().stream()
                    .sorted(Comparator.comparingInt(tag -> {
                        int index = order.indexOf(tag.getName());
                        return index != -1 ? index : 999;
                    }))
                    .collect(Collectors.toList()));
        }
    }
}
