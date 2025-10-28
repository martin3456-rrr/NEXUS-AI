package com.nexus.notification.kafka;

import com.nexus.notification.NotificationServiceApplication;
import com.nexus.notification.entity.NotificationEntity;
import com.nexus.notification.event.PaymentEvent;
import com.nexus.notification.repository.NotificationRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test")
public class PaymentKafkaIT {

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.4");

    @Container
    static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("payments.topic", () -> "payments.events.test");
    }

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    void shouldConsumePaymentEventAndPersistNotification() {
        // given
        String topic = "payments.events.test";
        PaymentEvent event = PaymentEvent.builder()
                .paymentId("ch_it_123")
                .userId("anonymous")
                .amount(new BigDecimal("42.50"))
                .currency("USD")
                .timestamp(Instant.now().toEpochMilli())
                .status("SUCCESS")
                .description("IT payment")
                .build();

        // when: publish to Kafka (JSON)
        publishEvent(topic, event);

        // then: await persistence via Kafka listener
        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    List<NotificationEntity> list = notificationRepository.findByUserIdOrderByCreatedAtDesc("anonymous");
                    assertThat(list).isNotEmpty();
                    assertThat(list.get(0).getContent()).contains("IT payment");
                    assertThat(list.get(0).getType()).isEqualTo("PAYMENT");
                    assertThat(list.get(0).isReadFlag()).isFalse();
                });
    }

    private void publishEvent(String topic, PaymentEvent event) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class.getName());
        // Avoid adding type headers to match consumer's default.type config
        props.put("spring.json.add.type.headers", false);

        try (KafkaProducer<String, PaymentEvent> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(topic, event));
            producer.flush();
        }
    }
}
