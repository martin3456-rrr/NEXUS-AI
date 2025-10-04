package com.nexus.payment.processor;

import com.nexus.payment.events.OrderCreatedEvent;
import com.nexus.payment.events.PaymentCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentProcessorTest {

    private final PaymentProcessor paymentProcessor = new PaymentProcessor();

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        OrderCreatedEvent orderEvent = new OrderCreatedEvent("ORDER-123", 99.99);

        // When
        PaymentCompletedEvent result = paymentProcessor.processPayment().apply(orderEvent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORDER-123");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }
}

