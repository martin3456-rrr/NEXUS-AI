package com.nexus.payment.service;

import com.nexus.payment.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    /**
     * Custom exception representing payment-related errors that should be surfaced to clients.
     */
    public static class PaymentException extends RuntimeException {
        public PaymentException(String message) {
            super(message);
        }
        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String createCharge(PaymentRequest paymentRequest) {
        logger.info("Simulating charge for amount {} {}...", paymentRequest.getAmount(), paymentRequest.getCurrency());

        // Basic validation
        if (paymentRequest.getCardNumber() == null || !paymentRequest.getCardNumber().startsWith("4")) {
            logger.error("Invalid card number provided.");
            throw new IllegalArgumentException("Invalid card number.");
        }
        if (paymentRequest.getAmount().doubleValue() <= 0) {
            logger.error("Payment amount must be positive.");
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // Simulate a successful charge
        String chargeId = "ch_" + UUID.randomUUID().toString().replace("-", "");
        logger.info("Successfully created mock charge with ID: {}", chargeId);

        return chargeId;
    }
}
