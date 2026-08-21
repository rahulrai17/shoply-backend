package com.shoply.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoply.backend.model.*;
import com.shoply.backend.payload.OrderRequestDTO;
import com.shoply.backend.repositories.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ConsumerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testProductId;
    private Long testAddressId;

    @BeforeEach
    void setup() {
        // 1. Setup Roles if not existing
        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));
        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_SELLER)));
        roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

        // 2. Setup Seller
        User seller = new User("seller", "seller@shoply.com", passwordEncoder.encode("password"));
        seller.setRoles(new HashSet<>(Collections.singletonList(sellerRole)));
        seller = userRepository.save(seller);

        // 3. Setup Product
        Category cat = categoryRepository.save(new Category(null, "Gadgets", null));
        Product product = new Product();
        product.setProductName("Smartphone");
        product.setDescription("High-end phone");
        product.setPrice(500.00);
        product.setQuantity(10);
        product.setDiscount(0.0);
        product.setSpecialPrice(500.00);
        product.setImage("default.png");
        product.setCategory(cat);
        product.setUser(seller);
        product = productRepository.save(product);
        testProductId = product.getProductId();
    }

    @Test
    void testConsumerFlow() throws Exception {
        // ===================================================================================
        // 1. Register & Login Consumer
        // ===================================================================================
        SignupRequest signup = new SignupRequest();
        signup.setUsername("consumer");
        signup.setEmail("consumer@shoply.com");
        signup.setPassword("password123");
        signup.setRole(new HashSet<>(Collections.singletonList("user")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setUsername("consumer");
        login.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String cookieHeader = loginResult.getResponse().getHeader("Set-Cookie");
        String token = cookieHeader.split(";")[0].split("=", 2)[1];
        Cookie jwtCookie = new Cookie("springBootEcommerce", token);

        // ===================================================================================
        // 2. Add Address (Needed for Order)
        // ===================================================================================
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setBuildingName("Apt 4B");
        address.setCity("Metropolis");
        address.setState("NY");
        address.setCountry("USA");
        address.setPincode("100010");

        MvcResult addressResult = mockMvc.perform(post("/api/addresses")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String addressResp = addressResult.getResponse().getContentAsString();
        testAddressId = objectMapper.readTree(addressResp).get("addressId").asLong();

        // ===================================================================================
        // 3. Search Product
        // ===================================================================================
        mockMvc.perform(get("/api/public/products/keyword/Smartphone")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName", is("Smartphone")));

        // ===================================================================================
        // 4. Add to Cart
        // ===================================================================================
        mockMvc.perform(post("/api/carts/products/{productId}/quantity/{quantity}", testProductId, 2)
                .cookie(jwtCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.products[0].productName", is("Smartphone")))
                .andExpect(jsonPath("$.products[0].quantity", is(2)));

        // ===================================================================================
        // 5. Place Order
        // ===================================================================================
        OrderRequestDTO orderRequest = new OrderRequestDTO();
        orderRequest.setAddressId(testAddressId);
        orderRequest.setPaymentMethod("CASH_ON_DELIVERY");
        orderRequest.setPgName("MockPG");
        orderRequest.setPgPaymentId("12345");
        orderRequest.setPgStatus("SUCCESS");
        orderRequest.setPgResponseMessage("Payment Successful");

        mockMvc.perform(post("/api/order/users/payments/{paymentMethod}", "CASH_ON_DELIVERY")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus", is("Order Accepted !")));
    }
}
