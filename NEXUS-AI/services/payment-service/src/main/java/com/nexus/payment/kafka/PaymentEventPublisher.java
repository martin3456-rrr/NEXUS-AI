package com.nexus.payment.kafka;

import com.nexus.payment.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${payments.topic:payments.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PaymentEvent event) {
        log.info("Publishing PaymentEvent to topic {}: {}", topic, event);
        kafkaTemplate.send(topic, event.getPaymentId(), event);
    }
}
