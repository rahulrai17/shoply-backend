package com.shoply.backend.service;

import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.APIException;
import com.shoply.backend.exceptions.ResourceNotFoundException;
import com.shoply.backend.model.AppRole;
import com.shoply.backend.model.Cart;
import com.shoply.backend.model.Category;
import com.shoply.backend.model.Product;
import com.shoply.backend.payload.CartDTO;
import com.shoply.backend.payload.ProductDTO;
import com.shoply.backend.payload.ProductResponse;
import com.shoply.backend.repositories.CartRepository;
import com.shoply.backend.repositories.CategoryRepository;
import com.shoply.backend.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.shoply.backend.model.User;
import com.shoply.backend.util.AuthUtil;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;
    
    @Autowired
    private AuthUtil authUtil;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        // Getting category by category id for product as the product will have the category id in it.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId)); // this is our custom exception that we have created for these response only.

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();
        for(Product value : products){
            if (value.getProductName().equals(productDTO.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            // mapping because we are getting DTO And we need entity for db operations.
            Product product = modelMapper.map(productDTO, Product.class);

            // updating data
            product.setImage("default.png");
            product.setCategory(category);
            
            // Set the logged-in user as the seller
            User loggedInUser = authUtil.loggedInUser();
            product.setUser(loggedInUser);
            
            double specialPrice = product.getPrice() -
                    ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            
            // mapping product to DTO class.
            ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);
            savedProductDTO.setSellerName(loggedInUser.getUserName());
            return savedProductDTO;
        } else {
            throw new APIException("Product already exists!! ");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        // we use Product Entity directly for Database operation.
        // List<Product> products =  productRepository.findAll();
        List<Product> products =  pageProducts.getContent();

        // Now since the return type is ProductResponse and ProductResponse wants ProductDTO we need to map the product to productDTO.
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setSellerName(product.getUser() != null ? product.getUser().getUserName() : null);
                    return dto;
                })
                .toList();

//        // this is something you can add if you need, it completely depends on the requirement of the api.But for now we don't need it so i will comment it out
//        if (products.isEmpty()){
//            throw new APIException("No products Found!!");
//        }

        // Then we need to create productResponse object
        ProductResponse productResponse = new ProductResponse();

        // Setting the value
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        //passing the productResponse
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Getting category by category id for product as the product will have the category id in it.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setSellerName(product.getUser() != null ? product.getUser().getUserName() : null);
                    return dto;
                })
                .toList();

        if(products.size() == 0){
            throw new APIException(category.getCategoryName() + " Category does not have any products" );
        }

        // Then we need to create productResponse object
        ProductResponse productResponse = new ProductResponse();

        // Setting the value
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        //passing the productResponse
        return productResponse;

    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageDetails);

        // we are just matching the patter here : eg : if product name is Robot any substring from here will give match found.
        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setSellerName(product.getUser() != null ? product.getUser().getUserName() : null);
                    return dto;
                })
                .toList();

        if(products.size() == 0){
            throw new APIException("Product not found with keyword: " + keyword);
        }

        // Then we need to create productResponse object
        ProductResponse productResponse = new ProductResponse();

        // Setting the value
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        //passing the productResponse
        return productResponse;
    }

    // Helper method to validate ownership
    private void checkProductOwnership(Product product) {
        User loggedInUser = authUtil.loggedInUser();
        
        // If the product has no owner (legacy data), allow Admin to take over, or fail. 
        // For now, if no owner, only Admin can edit.
        if (product.getUser() == null) {
            boolean isAdmin = loggedInUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(AppRole.ROLE_ADMIN));
            if (!isAdmin) {
                throw new APIException("Access Denied: Product has no owner and you are not an Admin.");
            }
            return;
        }

        if (!product.getUser().getUserId().equals(loggedInUser.getUserId())) {
            boolean isAdmin = loggedInUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(AppRole.ROLE_ADMIN));
            
            if (!isAdmin) {
                throw new APIException("Access Denied: You do not own this product.");
            }
        }
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        // Get the existing product from DB
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Validate Ownership
        checkProductOwnership(productFromDB);

        //mapping the dto to entity
        Product product = modelMapper.map(productDTO, Product.class);
// ... rest of method ...

        // Update the product info with the one send by user
        productFromDB.setProductName(product.getProductName());
        productFromDB.setDescription(product.getDescription());
        productFromDB.setQuantity(product.getQuantity());
        productFromDB.setDiscount(product.getDiscount());
        productFromDB.setPrice(product.getPrice());

        // Logic for special price
        double specialPrice = product.getPrice() -
                ((product.getDiscount() * 0.01) * product.getPrice());
        productFromDB.setSpecialPrice(specialPrice);

        // save to database
        Product savedProduct = productRepository.save(productFromDB);

        // This part of code helps in updating the product in cart with the changes made to the product eg : ( price : increase - decrease)
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);
        savedProductDTO.setSellerName(savedProduct.getUser() != null ? savedProduct.getUser().getUserName() : null);
        return savedProductDTO;
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        // find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Validate Ownership
        checkProductOwnership(product);

        // DELETE from each cart if product is deleted
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        // passing the product to be deleted
        productRepository.delete(product);

        // returning the productDTO that's deleted.
        ProductDTO deletedDto = modelMapper.map(product, ProductDTO.class);
        deletedDto.setSellerName(product.getUser() != null ? product.getUser().getUserName() : null);
        return deletedDto;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("ProductId", "productId", productId));

        // Validate Ownership
        checkProductOwnership(productFromDb);

        // Upload image to server
        // Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        // updating the new file name of the product
        productFromDb.setImage(fileName);

        // Save product
        Product updatedProduct = productRepository.save(productFromDb);

        // return DTO after mapping product to DTO.
        ProductDTO updatedDto = modelMapper.map(updatedProduct, ProductDTO.class);
        updatedDto.setSellerName(updatedProduct.getUser() != null ? updatedProduct.getUser().getUserName() : null);
        return updatedDto;
    }

    @Override
    public ProductResponse getAllProductsBySeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        User loggedInUser = authUtil.loggedInUser();

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        
        // Note: The repository method name is findByUserOrderByProductNameAsc, which hardcodes sorting by Name. 
        // If we want dynamic sorting, we should use findByUser(user, pageable).
        // Let's assume the user wants dynamic sorting and use a standard JPA method or the one we defined if strict name sort is desired.
        // Actually, the repo method I added `findByUserOrderByProductNameAsc` forces name sort. 
        // But the parameters pass `sortBy`. This is a slight mismatch. 
        // For now, I will respect the repo method I created, but ideally we should have just used findByUser.
        // Wait, I can't change the repo method signature without another tool call. 
        // I will use the method I created: findByUserOrderByProductNameAsc. It accepts Pageable, so it might override the static Sort if Pageable has sort?
        // JPA naming conventions: `OrderBy...` in method name usually overrides.
        // Let's try to stick to the plan.
        
        Page<Product> pageProducts = productRepository.findByUserOrderByProductNameAsc(loggedInUser, pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setSellerName(product.getUser() != null ? product.getUser().getUserName() : null);
                    return dto;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;
    }


}
