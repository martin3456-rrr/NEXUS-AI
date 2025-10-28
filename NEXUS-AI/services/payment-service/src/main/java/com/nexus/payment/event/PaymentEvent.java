package com.nexus.payment.event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentEvent {
    private String paymentId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private long timestamp;
    private String status; // e.g., SUCCESS, FAILED
    private String description;
}
