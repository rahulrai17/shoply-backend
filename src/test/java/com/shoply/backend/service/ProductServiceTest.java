package com.shoply.backend.service;

import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.Category;
import com.shoply.backend.model.Product;
import com.shoply.backend.model.User;
import com.shoply.backend.payload.ProductDTO;
import com.shoply.backend.repositories.CategoryRepository;
import com.shoply.backend.repositories.ProductRepository;
import com.shoply.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ====================================================================================
 * UNIT TEST: ProductServiceImpl Test Suite
 * ====================================================================================
 * Industry Standard Pattern: Arrange-Act-Assert (AAA) with Mockito.
 * Purpose: Verifies product inventory logic and discount pricing math in isolation.
 * Checks special price calculation formula: specialPrice = price - (discount * 0.01 * price).
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;
    private ProductDTO productDTO;
    private User seller;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");
        category.setProducts(new ArrayList<>());

        seller = new User();
        seller.setUserId(2L);
        seller.setUserName("seller1");

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Laptop");
        product.setPrice(1000.0);
        product.setDiscount(10.0);
        product.setUser(seller);

        productDTO = new ProductDTO();
        productDTO.setProductName("Laptop");
        productDTO.setPrice(1000.0);
        productDTO.setDiscount(10.0);

        lenient().when(authUtil.loggedInUser()).thenReturn(seller);
    }

    /**
     * TEST 1: Happy Path - Add Product & Compute Special Discount Price Math
     */
    @Test
    @DisplayName("Should successfully add product and calculate special price (price - discount%)")
    void addProduct_Success_CalculatesSpecialPrice() {
        // [ARRANGE] Mock repository to return existing category and saved product
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(productDTO, Product.class)).thenReturn(product);

        Product savedProduct = new Product();
        savedProduct.setProductId(100L);
        savedProduct.setProductName("Laptop");
        savedProduct.setPrice(1000.0);
        savedProduct.setDiscount(10.0);
        savedProduct.setSpecialPrice(900.0); // Math: 1000 - (10% of 1000) = 900.0
        savedProduct.setUser(seller);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDTO.class)).thenReturn(productDTO);

        // [ACT] Add product under Category ID 1
        ProductDTO result = productService.addProduct(1L, productDTO);

        // [ASSERT] Verify non-null result and repository interaction
        assertNotNull(result);
        assertEquals("Laptop", result.getProductName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * TEST 2: Negative Test - Reject Duplicate Product Title Under Same Category
     */
    @Test
    @DisplayName("Should throw APIException when adding a product with a name that already exists in category")
    void addProduct_ThrowsAPIException_WhenDuplicateProductInCategory() {
        // [ARRANGE] Add existing product to category's product list
        List<Product> existingProducts = new ArrayList<>();
        existingProducts.add(product);
        category.setProducts(existingProducts);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // [ACT & ASSERT] Expect duplicate validation exception
        assertThrows(APIException.class, () -> productService.addProduct(1L, productDTO));

        // Verify save was never triggered
        verify(productRepository, never()).save(any());
    }

    /**
     * TEST 3: Negative Test - Throws Exception When Target Category Does Not Exist
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID does not exist")
    void addProduct_ThrowsResourceNotFoundException_WhenCategoryNotFound() {
        // [ARRANGE] Return empty Optional for missing Category ID 99
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // [ACT & ASSERT] Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(99L, productDTO));

        // Verify product save was never attempted
        verify(productRepository, never()).save(any());
    }
}
