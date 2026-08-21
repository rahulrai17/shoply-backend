package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    @Schema(example = "1")
    private Long cartItemId;
    
    private CartDTO cart;
    private ProductDTO productDTO;
    
    @Schema(example = "2")
    private Integer quantity;
    
    @Schema(example = "10.0")
    private Double discount;
    
    @Schema(example = "500.0")
    private Double productPrice;

}
