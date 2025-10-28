package com.nexus.notification;

import com.nexus.notification.entity.NotificationEntity;
import com.nexus.notification.repository.NotificationRepository;
import com.nexus.payment.PaymentServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        classes = {PaymentServiceApplication.class, NotificationServiceApplication.class}, // Załaduj obie aplikacje
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentToNotificationE2EIT {

    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.4");
    private static final String TEST_TOPIC = "payments.events.e2e";
    private static final String TEST_USER_ID = "e2e-user-123";

    @Container
    static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("payments.topic", () -> TEST_TOPIC); // Ustaw temat dla obu

        // Wyłącz zbędne usługi w teście E2E
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");

        // Użyj H2 dla tego testu (lub skonfiguruj Testcontainers-Postgres)
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:e2e_db");
    }

    @Autowired
    private MockMvc mockMvc; // Z PaymentService

    @Autowired
    private NotificationRepository notificationRepository; // Z NotificationService

    @BeforeEach
    void setup() {
        notificationRepository.deleteAll();
    }

    @Test
    void shouldCreatePaymentAndTriggerNotification() throws Exception {
        // Given
        String paymentJsonRequest = """
        {
           "amount": 123.45,
           "currency": "USD",
           "description": "E2E Test Payment",
           "cardNumber": "4111111111111111",
           "cardCvc": "123",
           "cardExpiry": "12/30"
        }
        """;

        // Zmień w PaymentEvent, aby uwzględniał userId (dla uproszczenia, zakładam, że jest w requeście)
        // W bieżącym kodzie userId jest hardkodowany na "anonymous"[cite: 227].
        // Na potrzeby testu, zakładamy, że listener [cite: 444] poprawnie obsłuży "anonymous".
        String expectedUserId = "anonymous";
        String expectedContentFragment = "123.45 USD - E2E Test Payment";

        // When
        // 1. Wywołaj endpoint płatności
        mockMvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJsonRequest))
                .andExpect(status().isOk());

        // Then
        // 2. Użyj Awaitility, aby poczekać na przetworzenie eventu przez Kafkę i zapis w DB
        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(expectedUserId);
                    assertThat(notifications).isNotEmpty();
                    assertThat(notifications.get(0).getType()).isEqualTo("PAYMENT");
                    assertThat(notifications.get(0).isReadFlag()).isFalse();
                    assertThat(notifications.get(0).getContent()).contains(expectedContentFragment);
                });
    }
}