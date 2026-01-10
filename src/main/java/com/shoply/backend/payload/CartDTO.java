package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    @Schema(example = "1")
    private Long cartId;
    
    @Schema(example = "1500.0")
    private Double totalPrice = 0.0;
    
    private List<ProductDTO> products = new ArrayList<>();
}
