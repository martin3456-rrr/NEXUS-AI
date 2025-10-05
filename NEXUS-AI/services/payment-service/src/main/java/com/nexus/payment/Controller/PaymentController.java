package com.nexus.payment.Controller;

import com.nexus.payment.dto.PaymentRequest;
import com.nexus.payment.service.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private StripeService stripeService;

    @PostMapping("/charge")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {  // @Valid for DTO validation
        logger.info("Processing payment request: amount={}, currency={}, description={}",
                paymentRequest.getAmount(), paymentRequest.getCurrency(), paymentRequest.getDescription());

        try {
            // Call service for charge creation (mock or real Stripe)
            String chargeId = stripeService.createCharge(paymentRequest);

            logger.info("Payment successful for charge ID: {}", chargeId);
            return ResponseEntity.ok(Map.of(  // JSON response (połączenie z oboma: structured + message)
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
