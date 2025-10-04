package com.nexus.payment.events;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private double amount;
}

