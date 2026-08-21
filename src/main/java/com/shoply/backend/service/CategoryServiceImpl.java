package com.shoply.backend.service;

import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.Category;
import com.shoply.backend.payload.CategoryDTO;
import com.shoply.backend.payload.CategoryResponse;
import com.shoply.backend.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    // getAllCategories is to list All the categories name in the database
    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // This is a class we are using to implement pagination with sorting, this package is from spring data jpa.
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        // List<Category> categories = categoryRepository.findAll();
        List<Category> categories = categoryPage.getContent();

        if (categories.isEmpty())
            throw new APIException("No category created till now");

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    // createCategories is to create and save new category in the database
    @Override
    public CategoryDTO createCategories(@RequestBody CategoryDTO categoryDTO){

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category categoryFromBb = categoryRepository.findByCategoryName(category.getCategoryName());

        if(categoryFromBb != null){
            throw new APIException("Category wtih this name " + categoryDTO.getCategoryName() + " already exists !!! ");
        }

        Category savedCategory =  categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);

    }


    // deleteCategory is to delete category from the database
    @Override
    public CategoryDTO deleteCategory(Long categoryId){

        Category category = categoryRepository.findById(categoryId)
                // We will do same as we did for update.
        //        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category Not Found"));
                  .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return modelMapper.map(category, CategoryDTO.class);
    }


    // updateCategory is to update category in the database
    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId){
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Category category = modelMapper.map(categoryDTO, Category.class);

        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }


}
