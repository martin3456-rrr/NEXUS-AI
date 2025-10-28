package com.nexus.payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexus.payment.service.StripeService;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = com.nexus.payment.Controller.PaymentController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.stream.enabled=false"
})
class PaymentControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StripeService stripeService;

    @Test
    void charge_success_and_validation_errors() throws Exception {
        // Mock StripeService behavior based on request content
        Mockito.when(stripeService.createCharge(Mockito.any())).thenAnswer(inv -> {
            var req = (com.nexus.payment.dto.PaymentRequest) inv.getArgument(0);
            if (req.getCardNumber() != null && req.getCardNumber().startsWith("4") && req.getAmount().doubleValue() > 0) {
                return "ch_test_123";
            }
            throw new IllegalArgumentException("Invalid payment data");
        });

        // success
        mvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "amount": 99.99,
                          "currency": "USD",
                          "description": "Test order #1",
                          "cardNumber": "4111111111111111",
                          "cardCvc": "123",
                          "cardExpiry": "12/30"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chargeId").isNotEmpty());

        // invalid card should fail in service
        mvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "amount": 10.0,
                          "currency": "USD",
                          "description": "Test order #2",
                          "cardNumber": "5111111111111111",
                          "cardCvc": "123",
                          "cardExpiry": "12/30"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // negative amount -> fail
        mvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "amount": -1.0,
                          "currency": "USD",
                          "description": "Test order #3",
                          "cardNumber": "4111111111111111",
                          "cardCvc": "123",
                          "cardExpiry": "12/30"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
}
