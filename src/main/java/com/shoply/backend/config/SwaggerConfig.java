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
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. Storefront API")
                .pathsToMatch("/api/public/**", "/api/auth/**", "/api/carts/**", "/api/addresses/**", "/api/order/**")
                .addOpenApiCustomizer(sortTagsAlphabetically())
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("2. Admin Portal")
                .pathsToMatch("/api/admin/**", "/api/auth/**", "/api/public/**", "/api/carts/**", "/api/addresses/**", "/api/order/**")
                .addOpenApiCustomizer(sortTagsAlphabetically())
                .build();
    }

    @Bean
    public OpenAPI springShoplyOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Shoply E-Commerce API")
                        .description("Backend API for the Shoply platform. Supports Product Management, Cart, and Order Processing.\n\n" +
                                "🚀 **Getting Started:**\n" +
                                "1. **Select a View:** Use the dropdown in the top-right corner to switch between:\n" +
                                "    - **Storefront API:** For Customer-facing features (Browse, Cart, Order).\n" +
                                "    - **Admin Portal:** For Back-office management (Inventory, Users, Analytics).\n\n" +
                                "🔐 **Authentication Guide:**\n" +
                                "- **Login:** Use `POST /api/auth/signin` with default creds (`admin`/`adminPass`, `user1`/`password1`).\n" +
                                "- **Register:** Create your own user via `POST /api/auth/signup`.\n" +
                                "- **Authorize:** Copy the JWT token (excluding `springCookie=`) and paste it into the **Authorize 🔓** button.\n\n" +
                                "🛡️ **Available Roles:**\n" +
                                "- `ROLE_USER`: Standard Customer access.\n" +
                                "- `ROLE_SELLER`: Merchant access (Manage own products).\n" +
                                "- `ROLE_ADMIN`: Full System access.")
                        .version("1.0")
                        .contact(new Contact().name("Rahul Rai").email("rahulrai200017@example.com").url("https://github.com/rahulrai17"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local Development Server")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Endpoints for user login, signup, and session management"),
                        new Tag().name("Categories").description("Catalog management for product categories"),
                        new Tag().name("Products").description("Manage and browse the product inventory"),
                        new Tag().name("Shopping Cart").description("Manage items in the user's shopping cart"),
                        new Tag().name("Addresses").description("Manage shipping and billing addresses"),
                        new Tag().name("Orders").description("Checkout flow and order history")
                ))
                // Configure Security Scheme
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token (excluding 'Bearer ' prefix).")));
    }

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            List<String> order = List.of("Authentication", "Categories", "Products", "Shopping Cart", "Addresses", "Orders");
            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            int index = order.indexOf(tag.getName());
                            return index != -1 ? index : 999;
                        }))
                        .collect(Collectors.toList()));
            }
        };
    }
}
