package com.shoply.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoply.backend.model.AppRole;
import com.shoply.backend.model.Category;
import com.shoply.backend.model.Role;
import com.shoply.backend.payload.ProductDTO;
import com.shoply.backend.repositories.CategoryRepository;
import com.shoply.backend.repositories.ProductRepository;
import com.shoply.backend.repositories.RoleRepository;
import com.shoply.backend.repositories.UserRepository;
import com.shoply.backend.security.request.LoginRequest;
import com.shoply.backend.security.request.SignupRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SellerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setup() {
        // 1. Setup Roles if not existing
        roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_SELLER)));
        roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));
        roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

        // 2. Setup Category (Admin creates this usually)
        testCategory = new Category();
        testCategory.setCategoryName("TestCategory");
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void testSellerEcosystemFlow() throws Exception {
        // ===================================================================================
        // Step 1: Register a Seller
        // ===================================================================================
        SignupRequest sellerSignup = new SignupRequest();
        sellerSignup.setUsername("testSeller");
        sellerSignup.setEmail("seller@test.com");
        sellerSignup.setPassword("password123");
        sellerSignup.setRole(new HashSet<>(Collections.singletonList("seller")));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sellerSignup)))
                .andExpect(status().isOk());

        // ===================================================================================
        // Step 2: Login as Seller to get Token/Cookie
        // ===================================================================================
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testSeller");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jwtCookieHeader = loginResult.getResponse().getHeader("Set-Cookie");
        String jwtCookie = jwtCookieHeader.split(";")[0].split("=", 2)[1];

        // ===================================================================================
        // Step 3: Create a Product (as Seller)
        // ===================================================================================
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductName("My Unique Product");
        productDTO.setDescription("A description that is long enough.");
        productDTO.setPrice(100.0);
        productDTO.setDiscount(10.0);
        productDTO.setQuantity(50);

        MvcResult createResult = mockMvc.perform(post("/api/categories/{categoryId}/product", testCategory.getCategoryId())
                        .cookie(new Cookie("springBootEcommerce", jwtCookie))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerName", is("testSeller"))) // Verify Ownership in Response
                .andReturn();
        
        String responseContent = createResult.getResponse().getContentAsString();
        ProductDTO createdProduct = objectMapper.readValue(responseContent, ProductDTO.class);
        Long productId = createdProduct.getProductId();

        // ===================================================================================
        // Step 4: Verify Products Search
        // ===================================================================================
        mockMvc.perform(get("/api/public/products/keyword/Unique")
                        .cookie(new Cookie("springBootEcommerce", jwtCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName", is("My Unique Product")))
                .andExpect(jsonPath("$.content[0].sellerName", is("testSeller")));

        // ===================================================================================
        // Step 5: Seller Deletes Product (Success)
        // ===================================================================================
        mockMvc.perform(delete("/api/products/{productId}", productId)
                        .cookie(new Cookie("springBootEcommerce", jwtCookie)))
                .andExpect(status().isOk());
    }
}
