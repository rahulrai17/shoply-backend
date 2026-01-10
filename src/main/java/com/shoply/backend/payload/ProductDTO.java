package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @Schema(example = "1")
    private Long productId;
    
    @Schema(example = "iPhone 15 Pro")
    private String productName;
    
    @Schema(example = "default.png")
    private String image;
    
    @Schema(example = "Latest Apple smartphone with A17 Pro chip")
    private String description;
    
    @Schema(example = "50")
    private Integer quantity;
    
    @Schema(example = "999.99")
    private double price;
    
    @Schema(example = "10.0")
    private double discount;
    
    @Schema(example = "899.99")
    private double specialPrice;

}
