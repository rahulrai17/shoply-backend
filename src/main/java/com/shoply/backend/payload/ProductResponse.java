package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private List<ProductDTO> content;
    
    @Schema(example = "0")
    private Integer pageNumber;
    
    @Schema(example = "10")
    private Integer pageSize;
    
    @Schema(example = "100")
    private Long TotalElements;
    
    @Schema(example = "10")
    private Integer totalPages;
    
    @Schema(example = "true")
    private boolean lastPage;
}
