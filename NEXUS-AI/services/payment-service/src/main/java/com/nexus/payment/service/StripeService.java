package com.nexus.payment.service;

import com.nexus.payment.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public static class PaymentException extends RuntimeException {
        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String createCharge(PaymentRequest paymentRequest) {
        logger.info("Processing REAL charge for amount {} {}...", paymentRequest.getAmount(), paymentRequest.getCurrency());

        if (paymentRequest.getAmount().doubleValue() <= 0) {
            logger.error("Payment amount must be positive.");
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        try {
            Map<String, Object> cardParams = new HashMap<>();
            cardParams.put("number", paymentRequest.getCardNumber());
            cardParams.put("exp_month", paymentRequest.getCardExpiry().split("/")[0]);
            cardParams.put("exp_year", "20" + paymentRequest.getCardExpiry().split("/")[1]);
            cardParams.put("cvc", paymentRequest.getCardCvc());

            Map<String, Object> tokenParams = new HashMap<>();
            tokenParams.put("card", cardParams);

            Token token = Token.create(tokenParams);

            Map<String, Object> chargeParams = new HashMap<>();

            long amountInCents = paymentRequest.getAmount().multiply(new BigDecimal(100)).longValue();

            chargeParams.put("amount", amountInCents);
            chargeParams.put("currency", paymentRequest.getCurrency().toLowerCase());
            chargeParams.put("description", paymentRequest.getDescription());
            chargeParams.put("source", token.getId());

            Charge charge = Charge.create(chargeParams);

            String chargeId = charge.getId();
            logger.info("Successfully created REAL charge with ID: {}", chargeId);
            return chargeId;

        } catch (StripeException e) {
            logger.error("Stripe payment failed: {}", e.getMessage());
            throw new PaymentException("Payment failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error during payment processing: {}", e.getMessage(), e);
            throw new PaymentException("Internal payment processing error.", e);
        }
    }
}
