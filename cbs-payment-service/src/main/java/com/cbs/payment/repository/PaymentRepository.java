package com.cbs.payment.repository;

import com.cbs.payment.model.Payment;
import com.cbs.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByReference(String reference);

    List<Payment> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Payment> findBySourceAccountIdOrderByIdDesc(Long sourceAccountId);

    List<Payment> findByDestinationAccountIdOrderByIdDesc(Long destinationAccountId);

    List<Payment> findByStatusOrderByIdDesc(PaymentStatus status);
}
