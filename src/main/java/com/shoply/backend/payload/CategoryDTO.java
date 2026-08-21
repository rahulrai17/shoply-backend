package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Auto-generated Category ID")
    private Long categoryID;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 3, message = "Category name must contain at least 3 characters")
    @Schema(description = "Name of the product category", example = "Home & Kitchen")
    private String categoryName;
}
