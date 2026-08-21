package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    @Schema(example = "1")
    private Long orderId;
    
    @Schema(example = "user@example.com")
    private String email;
    
    private List<OrderItemDTO> orderItems;
    
    @Schema(example = "2026-01-10")
    private LocalDate orderDate;
    
    private PaymentDTO payment;
    
    @Schema(example = "1200.50")
    private Double totalAmount;
    
    @Schema(example = "Order Placed")
    private String orderStatus;
    
    @Schema(example = "1")
    private Long addressId;
}
