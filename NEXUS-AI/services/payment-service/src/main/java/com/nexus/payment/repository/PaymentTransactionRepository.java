package com.nexus.payment.repository;

import com.nexus.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // Find a transaction by the unique charge ID from the payment gateway
    PaymentTransaction findByChargeId(String chargeId);

    // Find all transactions made by a specific user
    List<PaymentTransaction> findByUserId(Long userId);
}

