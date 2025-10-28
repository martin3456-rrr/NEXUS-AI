package com.nexus.payment.Controller;

import com.nexus.payment.dto.PaymentRequest;
import com.nexus.payment.event.PaymentEvent;
import com.nexus.payment.kafka.PaymentEventPublisher;
import com.nexus.payment.service.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PaymentEventPublisher eventPublisher;

    @Value("${payments.topic:payments.events}")
    private String paymentsTopic;

    @PostMapping("/charge")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {  // @Valid for DTO validation
        logger.info("Processing payment request: amount={}, currency={}, description={}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getDescription());

        try {
            // Call service for charge creation (mock or real Stripe)
            String chargeId = stripeService.createCharge(paymentRequest);

            // Publish async event to Kafka
            PaymentEvent event = PaymentEvent.builder()
                    .paymentId(chargeId)
                    .userId("anonymous")
                    .amount(paymentRequest.getAmount())
                    .currency(paymentRequest.getCurrency())
                    .timestamp(Instant.now().toEpochMilli())
                    .status("SUCCESS")
                    .description(paymentRequest.getDescription())
                    .build();
            eventPublisher.publish(event);

            logger.info("Payment successful for charge ID: {}", chargeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully!",
                    "chargeId", chargeId,
                    "amount", paymentRequest.getAmount() + " " + paymentRequest.getCurrency()
            ));
        } catch (IllegalArgumentException e) {  // From first/simple validation or legacy
            logger.error("Invalid argument in payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (StripeService.PaymentException e) {  // Custom from service (expanded)
            logger.error("Payment exception: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {  // General errors (e.g., network, Stripe API fail)
            logger.error("Unexpected error during payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred: " + e.getMessage()
            ));
        }
    }
}
