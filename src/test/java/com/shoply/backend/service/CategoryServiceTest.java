package com.shoply.backend.service;

import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.Category;
import com.shoply.backend.payload.CategoryDTO;
import com.shoply.backend.payload.CategoryResponse;
import com.shoply.backend.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ====================================================================================
 * UNIT TEST: CategoryServiceImpl Test Suite
 * ====================================================================================
 * Industry Standard Pattern: Arrange-Act-Assert (AAA) with Mockito.
 * Purpose: Tests Category business logic in total isolation without starting Spring Boot
 * or connecting to a real database. All external dependencies (CategoryRepository, ModelMapper)
 * are mocked.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations in JUnit 5
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository; // Mocked database repository

    @Mock
    private ModelMapper modelMapper; // Mocked DTO mapper

    @InjectMocks
    private CategoryServiceImpl categoryService; // Target service under test (receives mocked dependencies)

    private Category category;
    private CategoryDTO categoryDTO;

    /**
     * Set up dummy data before each test execution.
     */
    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryID(1L);
        categoryDTO.setCategoryName("Electronics");
    }

    /**
     * TEST 1: Happy Path - Successfully Create a New Category
     */
    @Test
    @DisplayName("Should successfully create a category when category name is unique")
    void createCategories_Success() {
        // [ARRANGE] Define mock behaviors
        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryName("Electronics")).thenReturn(null); // Ensure name is available
        when(categoryRepository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        // [ACT] Execute the method under test
        CategoryDTO created = categoryService.createCategories(categoryDTO);

        // [ASSERT] Verify outputs and interactions
        assertNotNull(created);
        assertEquals("Electronics", created.getCategoryName());
        verify(categoryRepository, times(1)).save(category); // Verify DB save was invoked exactly once
    }

    /**
     * TEST 2: Negative Test - Reject Duplicate Category Name
     */
    @Test
    @DisplayName("Should throw APIException when attempting to create a category with duplicate name")
    void createCategories_ThrowsAPIException_WhenCategoryAlreadyExists() {
        // [ARRANGE] Mock repository to return an existing category with the same name
        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryName("Electronics")).thenReturn(category);

        // [ACT & ASSERT] Expect APIException when duplicate exists
        assertThrows(APIException.class, () -> categoryService.createCategories(categoryDTO));

        // Verify that save() was NEVER called due to fail-fast validation
        verify(categoryRepository, never()).save(any());
    }

    /**
     * TEST 3: Happy Path - Get All Categories with Pagination
     */
    @Test
    @DisplayName("Should return paginated category response when categories exist")
    void getAllCategories_Success() {
        // [ARRANGE] Mock paginated Spring Data JPA response
        Page<Category> categoryPage = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        // [ACT] Call service method with pagination parameters
        CategoryResponse response = categoryService.getAllCategories(0, 10, "categoryId", "asc");

        // [ASSERT] Verify pagination metadata and content
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Electronics", response.getContent().get(0).getCategoryName());
    }

    /**
     * TEST 4: Happy Path - Delete Category by ID
     */
    @Test
    @DisplayName("Should successfully delete category when valid ID is provided")
    void deleteCategory_Success() {
        // [ARRANGE] Mock repository to return category for ID 1
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        // [ACT] Execute deletion
        CategoryDTO deleted = categoryService.deleteCategory(1L);

        // [ASSERT] Verify returned DTO and delete invocation
        assertNotNull(deleted);
        assertEquals(1L, deleted.getCategoryID());
        verify(categoryRepository, times(1)).delete(category);
    }

    /**
     * TEST 5: Negative Test - Throws Exception When Deleting Non-Existent Category ID
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent category ID")
    void deleteCategory_ThrowsResourceNotFoundException_WhenIdNotFound() {
        // [ARRANGE] Mock repository to return empty Optional for missing ID 99
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // [ACT & ASSERT] Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(99L));

        // Verify delete was never called for invalid ID
        verify(categoryRepository, never()).delete(any());
    }
}
