package com.nexus.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String cardNumber;
    private String cardExpiry; // "MM/YY"
    private String cardCvc;
    private BigDecimal amount;
    private String currency; // e.g., "USD"
    private String description;
}