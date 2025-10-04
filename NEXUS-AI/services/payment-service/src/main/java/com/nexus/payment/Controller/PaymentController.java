package com.nexus.payment.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @PostMapping("/charge")
    public String processPayment(@RequestBody String paymentDetails) {
        System.out.println("Processing payment for: " + paymentDetails);
        return "Payment processed successfully!";
    }
}