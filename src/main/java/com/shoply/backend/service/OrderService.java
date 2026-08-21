package com.shoply.backend.service;

import com.shoply.backend.payload.OrderDTO;
import org.springframework.stereotype.Service;

public interface OrderService {
    OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
