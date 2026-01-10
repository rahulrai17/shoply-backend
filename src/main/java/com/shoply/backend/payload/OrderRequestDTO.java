package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    @Schema(example = "1")
    private Long addressId;
    
    @Schema(example = "STRIPE")
    private String paymentMethod;
    
    @Schema(example = "Stripe")
    private String pgName;
    
    @Schema(example = "ch_3P5...")
    private String pgPaymentId;
    
    @Schema(example = "succeeded")
    private String pgStatus;
    
    @Schema(example = "Payment Successful")
    private String pgResponseMessage;
}