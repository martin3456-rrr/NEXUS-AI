package com.nexus.payment.processor;

import com.nexus.payment.events.OrderCreatedEvent;
import com.nexus.payment.events.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    // Definicja funkcji, która przetwarza zamówienie i zwraca zdarzenie o zakończeniu płatności
    @Bean
    public Function<OrderCreatedEvent, PaymentCompletedEvent> processPayment() {
        return orderEvent -> {
            log.info("Processing payment for order: {}", orderEvent.getOrderId());

            // Symulacja logiki przetwarzania płatności
            try {
                Thread.sleep(2000); // Symulacja opóźnienia
                log.info("Payment successful for order: {}", orderEvent.getOrderId());
                return new PaymentCompletedEvent(orderEvent.getOrderId(), "SUCCESS");
            } catch (InterruptedException e) {
                log.error("Payment failed for order: {}", orderEvent.getOrderId());
                Thread.currentThread().interrupt();
                return new PaymentCompletedEvent(orderEvent.getOrderId(), "FAILED");
            }
        };
    }
}