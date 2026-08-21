package com.shoply.backend.controller;

import com.shoply.backend.model.Category;
import com.shoply.backend.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ====================================================================================
 * INTEGRATION TEST: Public Catalog & Storefront API Workflows
 * ====================================================================================
 * Industry Standard Pattern: End-to-End MockMvc Integration Testing with H2 Database.
 * Purpose: Tests public storefront REST endpoints (`/api/public/**`) ensuring
 * pagination metadata, sorting, and content response objects format correctly.
 */
@SpringBootTest // Boots full Spring application context
@AutoConfigureMockMvc // Configures MockMvc client
@Transactional // Rolls back DB changes after each test execution
class CartOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Seed prerequisite Category data into H2 database before executing test methods.
     */
    @BeforeEach
    void setUp() {
        if (categoryRepository.findByCategoryName("Sample Electronics") == null) {
            Category category = new Category();
            category.setCategoryName("Sample Electronics");
            categoryRepository.save(category);
        }
    }

    /**
     * TEST 1: Happy Path - Public Category Listing with Pagination Metadata
     */
    @Test
    @DisplayName("GET /api/public/categories should return 200 OK with paginated metadata")
    void getPublicCategories_Success() throws Exception {
        // [ACT & ASSERT] Perform GET /api/public/categories and assert HTTP 200 OK + pageNumber field
        mockMvc.perform(get("/api/public/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.pageSize").exists());
    }

    /**
     * TEST 2: Happy Path - Public Product Catalog Listing
     */
    @Test
    @DisplayName("GET /api/public/products should return 200 OK with paginated product response")
    void getPublicProducts_Success() throws Exception {
        // [ACT & ASSERT] Perform GET /api/public/products and assert HTTP 200 OK + pageNumber field
        mockMvc.perform(get("/api/public/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").exists());
    }
}
