package com.cbs.payment.service;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.dto.CreatePaymentRequest;
import com.cbs.payment.dto.PaymentResponse;
import com.cbs.payment.dto.PaymentStatusUpdateRequest;
import com.cbs.payment.integration.LedgerPostingClient;
import com.cbs.payment.model.Payment;
import com.cbs.payment.model.PaymentStatus;
import com.cbs.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerPostingClient ledgerPostingClient;

    public PaymentService(PaymentRepository paymentRepository,
                          LedgerPostingClient ledgerPostingClient) {
        this.paymentRepository = paymentRepository;
        this.ledgerPostingClient = ledgerPostingClient;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String reference = normalizeReference(request.reference());
        if (paymentRepository.existsByReference(reference)) {
            throw new ApiException("PAYMENT_REFERENCE_EXISTS", "Reference already exists");
        }

        Payment payment = new Payment(
                request.customerId(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                request.currency().trim().toUpperCase(),
                request.method(),
                reference,
                request.description().trim(),
                request.valueDate()
        );

        Payment createdPayment = paymentRepository.save(payment);
        createdPayment.setStatus(PaymentStatus.PROCESSING);
        Payment processingPayment = paymentRepository.save(createdPayment);

        try {
            ledgerPostingClient.postPayment(processingPayment);
            processingPayment.setStatus(PaymentStatus.COMPLETED);
            processingPayment.setFailureReason(null);
        } catch (ApiException exception) {
            processingPayment.setStatus(PaymentStatus.FAILED);
            processingPayment.setFailureReason(truncateReason(exception.getMessage()));
        }

        return PaymentResponse.from(paymentRepository.save(processingPayment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        return PaymentResponse.from(findPayment(paymentId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listPayments(Long customerId,
                                              Long sourceAccountId,
                                              Long destinationAccountId,
                                              PaymentStatus status) {
        List<Payment> payments;

        if (customerId != null && sourceAccountId == null && destinationAccountId == null && status == null) {
            payments = paymentRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (sourceAccountId != null && customerId == null && destinationAccountId == null && status == null) {
            payments = paymentRepository.findBySourceAccountIdOrderByIdDesc(sourceAccountId);
        } else if (destinationAccountId != null && customerId == null && sourceAccountId == null && status == null) {
            payments = paymentRepository.findByDestinationAccountIdOrderByIdDesc(destinationAccountId);
        } else if (status != null && customerId == null && sourceAccountId == null && destinationAccountId == null) {
            payments = paymentRepository.findByStatusOrderByIdDesc(status);
        } else {
            payments = paymentRepository.findAll().stream()
                    .filter(payment -> customerId == null || payment.getCustomerId().equals(customerId))
                    .filter(payment -> sourceAccountId == null || payment.getSourceAccountId().equals(sourceAccountId))
                    .filter(payment -> destinationAccountId == null || destinationAccountId.equals(payment.getDestinationAccountId()))
                    .filter(payment -> status == null || payment.getStatus() == status)
                    .sorted(Comparator.comparing(Payment::getId).reversed())
                    .toList();
        }

        return payments.stream().map(PaymentResponse::from).toList();
    }

    @Transactional
    public PaymentResponse completePayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        ensureTransitionAllowed(payment, PaymentStatus.COMPLETED);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setFailureReason(null);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse failPayment(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = findPayment(paymentId);
        ensureTransitionAllowed(payment, PaymentStatus.FAILED);

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(request.reason().trim());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = findPayment(paymentId);
        ensureTransitionAllowed(payment, PaymentStatus.CANCELLED);

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason(request.reason().trim());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse retryPosting(Long paymentId) {
        Payment payment = findPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new ApiException("PAYMENT_NOT_FAILED", "Only failed payment can be retried");
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setFailureReason(null);
        Payment processingPayment = paymentRepository.save(payment);

        try {
            ledgerPostingClient.postPayment(processingPayment);
            processingPayment.setStatus(PaymentStatus.COMPLETED);
            processingPayment.setFailureReason(null);
        } catch (ApiException exception) {
            processingPayment.setStatus(PaymentStatus.FAILED);
            processingPayment.setFailureReason(truncateReason(exception.getMessage()));
        }

        return PaymentResponse.from(paymentRepository.save(processingPayment));
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException("PAYMENT_NOT_FOUND", "Payment not found"));
    }

    private void ensureTransitionAllowed(Payment payment, PaymentStatus targetStatus) {
        PaymentStatus currentStatus = payment.getStatus();

        if (currentStatus == PaymentStatus.COMPLETED) {
            throw new ApiException("PAYMENT_ALREADY_COMPLETED", "Completed payment cannot be changed");
        }

        if (currentStatus == PaymentStatus.FAILED && targetStatus != PaymentStatus.COMPLETED) {
            throw new ApiException("PAYMENT_ALREADY_FAILED", "Failed payment cannot be changed");
        }

        if (currentStatus == PaymentStatus.CANCELLED) {
            throw new ApiException("PAYMENT_ALREADY_CANCELLED", "Cancelled payment cannot be changed");
        }

        if (targetStatus == PaymentStatus.COMPLETED && currentStatus == PaymentStatus.FAILED) {
            throw new ApiException("PAYMENT_FAILED", "Failed payment cannot be completed");
        }
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }

    private String truncateReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= 255 ? reason : reason.substring(0, 255);
    }
}
