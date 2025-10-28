package com.nexus.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import java.io.File;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

@DisabledIfSystemProperty(named = "skipE2ETests", matches = "true")
public class FullStackE2EIT {

    private static DockerComposeContainer<?> environment;
    private static RestTemplate restTemplate;
    private static String gatewayUrl;

    @BeforeAll
    static void setUp() {
        // Ścieżka do docker-compose.yml — lokalizacja względem testu e2e
        environment = new DockerComposeContainer<>(new File("../../docker-compose.yml"))
                .withExposedService("api-gateway", 8080,
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                .withExposedService("notification-service", 8085)
                .withLocalCompose(true);

        environment.start();

        String gatewayHost = environment.getServiceHost("api-gateway", 8080);
        Integer gatewayPort = environment.getServicePort("api-gateway", 8080);
        gatewayUrl = "http://" + gatewayHost + ":" + gatewayPort;

        restTemplate = new RestTemplateBuilder().build();
    }

    @Test
    void testFullUserJourney() throws Exception {
        // Krok 1: rejestracja
        String username = "e2e_user_" + System.currentTimeMillis();
        String email = username + "@example.com";
        Map<String, String> registerRequest = Map.of(
                "username", username,
                "password", "Password123!",
                "email", email
        );
        ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                gatewayUrl + "/api/auth/register", registerRequest, String.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Krok 2: logowanie
        Map<String, String> loginRequest = Map.of("username", username, "password", "Password123!");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                gatewayUrl + "/api/auth/login", loginRequest, Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String jwtToken = (String) loginResponse.getBody().get("token");
        assertThat(jwtToken).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> authedEntity = new HttpEntity<>(headers);

        // Krok 3: predykcja AI
        Map<String, ?> aiRequest = Map.of("input", new double[]{1.0, 2.0, 3.0});
        HttpEntity<Map<String, ?>> aiHttpEntity = new HttpEntity<>(aiRequest, headers);

        ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                gatewayUrl + "/api/analytics/predict", aiHttpEntity, Map.class);
        assertThat(aiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aiResponse.getBody().get("prediction")).isNotNull();

        // Krok 4: płatność
        Map<String, Object> paymentRequest = Map.of(
                "amount", 99.99,
                "currency", "USD",
                "description", "E2E Test Payment",
                "cardNumber", "4111111111111111"
        );
        HttpEntity<Map<String, Object>> paymentHttpEntity = new HttpEntity<>(paymentRequest, headers);
        ResponseEntity<Map> paymentResponse = restTemplate.postForEntity(
                gatewayUrl + "/api/payments/charge", paymentHttpEntity, Map.class);
        assertThat(paymentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paymentResponse.getBody().get("success")).isEqualTo(true);

        // Krok 5: weryfikacja notyfikacji
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            ResponseEntity<List> notifResponse = restTemplate.exchange(
                    gatewayUrl + "/api/notifications/anonymous",
                    HttpMethod.GET, authedEntity, List.class);
            assertThat(notifResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(notifResponse.getBody()).anyMatch(item ->
                    ((Map) item).get("content").toString().contains("E2E Test Payment"));
        });

        // Krok 6: zapis do Blockchain
        HttpEntity<String> blockchainHttpEntity = new HttpEntity<>("E2E Block Data", headers);
        ResponseEntity<Map> blockResponse = restTemplate.postForEntity(
                gatewayUrl + "/api/blockchain/add", blockchainHttpEntity, Map.class);
        assertThat(blockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(blockResponse.getBody().get("data")).isEqualTo("E2E Block Data");

        // Krok 7: test rate limiting
        ResponseEntity<String> rateLimitResponse = null;
        for (int i = 0; i < 20; i++) {
            rateLimitResponse = restTemplate.exchange(
                    gatewayUrl + "/api/analytics/predict", HttpMethod.POST, aiHttpEntity, String.class);
            if (rateLimitResponse.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                break;
            }
        }
        assertThat(rateLimitResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
