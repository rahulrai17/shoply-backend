package com.shoply.backend.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @Schema(description = "ID of the customer shipping address (e.g., 1)", example = "1")
    private Long addressId;

    @Schema(description = "Payment method selected", example = "CreditCard")
    private String paymentMethod;

    @Schema(description = "Payment gateway provider name", example = "Stripe")
    private String pgName;

    @Schema(description = "Transaction ID from payment gateway", example = "tx_stripe_987654")
    private String pgPaymentId;

    @Schema(description = "Payment gateway response status", example = "SUCCESS")
    private String pgStatus;

    @Schema(description = "Payment gateway response message", example = "Payment Authorized Successfully")
    private String pgResponseMessage;
}