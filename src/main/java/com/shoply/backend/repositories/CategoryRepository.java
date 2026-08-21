package com.shoply.backend.repositories;

import com.shoply.backend.model.Cart;
import com.shoply.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);

    // The above findByCategoryName will be taken care by JPA as we have already followed the rules
    // rule : findBy - implies to JPA tha this something where findBy type queries will be needed.
    // CategoryName is a field from the Category Entity. Remember this is important.
    // These are also known as Repository Method

}
