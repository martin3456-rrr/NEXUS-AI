package com.nexus.notification.kafka;

import com.nexus.notification.event.PaymentEvent;
import com.nexus.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final NotificationService notificationService;

    public PaymentEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${payments.topic:payments.events}", groupId = "notification-service")
    public void onPaymentEvent(@Payload PaymentEvent event) {
        try {
            log.info("Received PaymentEvent: {}", event);
            String content = String.format("Payment %s: %s %s - %s",
                    event.getStatus(), event.getAmount(), event.getCurrency(), event.getDescription());
            // Fallback for userId if not provided
            String userId = event.getUserId() != null ? event.getUserId() : "anonymous";
            notificationService.create(userId, content, "PAYMENT");
        } catch (Exception e) {
            log.error("Error processing PaymentEvent: {}", e.getMessage(), e);
        }
    }
}
