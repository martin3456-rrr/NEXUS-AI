package com.nexus.payment.Controller;

import com.nexus.payment.dto.PaymentRequest;
import com.nexus.payment.event.PaymentEvent;
import com.nexus.payment.kafka.PaymentEventPublisher;
import com.nexus.payment.service.StripeService;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.DistributionSummary;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final StripeService stripeService;
    private final PaymentEventPublisher eventPublisher;
    private final DistributionSummary paymentRevenueSummary;

    @Value("${payments.topic:payments.events}")
    private String paymentsTopic;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    private final Counter paymentSuccessCounter;
    private final Counter paymentFailedCounter;
    private final Timer paymentProcessTimer;

    @Autowired
    public PaymentController(StripeService stripeService, PaymentEventPublisher eventPublisher, MeterRegistry registry) {
        this.stripeService = stripeService;
        this.eventPublisher = eventPublisher;

        this.paymentSuccessCounter = Counter.builder("payments.processed")
                .tag("status", "success")
                .description("Number of successful payments")
                .register(registry);

        this.paymentFailedCounter = Counter.builder("payments.processed")
                .tag("status", "failed")
                .description("Number of failed payments")
                .register(registry);

        this.paymentProcessTimer = Timer.builder("payments.processing.time")
                .description("Time taken to process a payment")
                .register(registry);

        this.paymentRevenueSummary = DistributionSummary.builder("payments.revenue")
                .description("Sum of successful payment amounts")
                .baseUnit("USD")
                .register(registry);
    }

    @PostMapping("/charge")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            logger.error("Stripe webhook secret is not configured!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured");
        }

        logger.info("Processing payment request: amount={}, currency={}, description={}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getDescription());

        long startTime = System.nanoTime();



        Event event;
        try {
            String chargeId = stripeService.createCharge(paymentRequest);

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

            paymentSuccessCounter.increment();

            if (paymentRequest.getAmount() != null) {
                paymentRevenueSummary.record(paymentRequest.getAmount().doubleValue());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully!",
                    "chargeId", chargeId,
                    "amount", paymentRequest.getAmount() + " " + paymentRequest.getCurrency()
            ));
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

        } catch (IllegalArgumentException | StripeService.PaymentException e) {
            logger.error("Payment exception: {}", e.getMessage());
            paymentFailedCounter.increment();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during payment: {}", e.getMessage(), e);
            paymentFailedCounter.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred: " + e.getMessage(),
                    "message", "An unexpected error occurred: " + e.getMessage()
            ));
        } finally {
            paymentProcessTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        String endpointSecret = "whsec_....";

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
        }

        if ("charge.succeeded".equals(event.getType())) {
            Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
            paymentService.confirmPayment(charge.getId());
            System.out.println("Payment succeeded for: " + charge.getId());
        }

        return ResponseEntity.ok().build();
    }
}
