package com.nexus.payment.events;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentCompletedEvent {
    private String orderId;
    private String status; // SUCCESS, FAILED
}