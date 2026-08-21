package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Auto-generated Product ID")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    @Schema(description = "Name of the product", example = "4K Ultra HD Smart TV")
    private String productName;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Product image filename", example = "default.png")
    private String image;

    @NotBlank(message = "Product description is required")
    @Size(min = 6, message = "Product description must contain at least 6 characters")
    @Schema(description = "Detailed product description", example = "55-inch OLED display with HDR10+")
    private String description;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(description = "Stock inventory quantity", example = "15")
    private Integer quantity;

    @Min(value = 0, message = "Price cannot be negative")
    @Schema(description = "Retail price before discount", example = "500.00")
    private double price;

    @Schema(description = "Percentage discount (0 - 100%)", example = "10.00")
    private double discount;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Calculated final price after discount", example = "450.00")
    private double specialPrice;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Username of merchant/seller who created the product", example = "seller1")
    private String sellerName;
}
