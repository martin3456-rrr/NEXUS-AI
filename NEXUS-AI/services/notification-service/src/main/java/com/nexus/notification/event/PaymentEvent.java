package com.nexus.notification.event;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentEvent {
    private String paymentId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private long timestamp;
    private String status;
    private String description;
}
