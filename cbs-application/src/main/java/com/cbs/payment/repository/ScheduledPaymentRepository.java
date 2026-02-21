package com.cbs.payment.repository;

import com.cbs.payment.model.ScheduledPayment;
import com.cbs.payment.model.ScheduledPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, Long> {

    boolean existsByReference(String reference);

    List<ScheduledPayment> findByCustomerIdOrderByIdDesc(Long customerId);

    List<ScheduledPayment> findByStatusOrderByIdDesc(ScheduledPaymentStatus status);

    List<ScheduledPayment> findByNextExecutionDateLessThanEqualAndStatus(LocalDate date, ScheduledPaymentStatus status);
}
