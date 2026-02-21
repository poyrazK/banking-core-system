package com.cbs.payment.service;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.dto.CreatePaymentRequest;
import com.cbs.payment.dto.CreateScheduledPaymentRequest;
import com.cbs.payment.dto.ScheduledPaymentResponse;
import com.cbs.payment.model.ScheduleFrequency;
import com.cbs.payment.model.ScheduledPayment;
import com.cbs.payment.model.ScheduledPaymentStatus;
import com.cbs.payment.repository.ScheduledPaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ScheduledPaymentService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPaymentService.class);
    private static final int MAX_FAILURE_COUNT = 3;

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final PaymentService paymentService;

    public ScheduledPaymentService(ScheduledPaymentRepository scheduledPaymentRepository,
            PaymentService paymentService) {
        this.scheduledPaymentRepository = scheduledPaymentRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public ScheduledPaymentResponse createScheduledPayment(CreateScheduledPaymentRequest request) {
        if (request.endDate() != null && request.startDate().isAfter(request.endDate())) {
            throw new ApiException("SCHEDULED_PAYMENT_INVALID_DATE_RANGE",
                    "Start date must be before or equal to end date");
        }

        String reference = normalizeReference(request.reference());
        if (scheduledPaymentRepository.existsByReference(reference)) {
            throw new ApiException("SCHEDULED_PAYMENT_REFERENCE_EXISTS", "Reference already exists");
        }

        ScheduledPayment scheduledPayment = new ScheduledPayment.Builder()
                .customerId(request.customerId())
                .sourceAccountId(request.sourceAccountId())
                .destinationAccountId(request.destinationAccountId())
                .amount(request.amount())
                .currency(request.currency().trim().toUpperCase())
                .method(request.method())
                .description(request.description().trim())
                .frequency(request.frequency())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reference(reference)
                .build();

        return ScheduledPaymentResponse.from(scheduledPaymentRepository.save(scheduledPayment));
    }

    @Transactional(readOnly = true)
    public ScheduledPaymentResponse getScheduledPayment(Long id) {
        return ScheduledPaymentResponse.from(findScheduledPayment(id));
    }

    @Transactional(readOnly = true)
    public List<ScheduledPaymentResponse> listScheduledPayments(Long customerId, ScheduledPaymentStatus status) {
        List<ScheduledPayment> payments;

        if (customerId != null && status != null) {
            payments = scheduledPaymentRepository.findByCustomerIdOrderByIdDesc(customerId);
            payments = payments.stream().filter(p -> p.getStatus() == status).toList();
        } else if (customerId != null) {
            payments = scheduledPaymentRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (status != null) {
            payments = scheduledPaymentRepository.findByStatusOrderByIdDesc(status);
        } else {
            payments = scheduledPaymentRepository.findAll().stream()
                    .sorted(Comparator.comparing(ScheduledPayment::getId).reversed())
                    .toList();
        }

        return payments.stream().map(ScheduledPaymentResponse::from).toList();
    }

    @Transactional
    public ScheduledPaymentResponse pauseScheduledPayment(Long id) {
        ScheduledPayment payment = findScheduledPayment(id);
        if (payment.getStatus() != ScheduledPaymentStatus.ACTIVE) {
            throw new ApiException("SCHEDULED_PAYMENT_NOT_ACTIVE", "Only active scheduled payments can be paused");
        }
        payment.setStatus(ScheduledPaymentStatus.PAUSED);
        return ScheduledPaymentResponse.from(scheduledPaymentRepository.save(payment));
    }

    @Transactional
    public ScheduledPaymentResponse resumeScheduledPayment(Long id) {
        ScheduledPayment payment = findScheduledPayment(id);
        if (payment.getStatus() != ScheduledPaymentStatus.PAUSED) {
            throw new ApiException("SCHEDULED_PAYMENT_NOT_PAUSED", "Only paused scheduled payments can be resumed");
        }

        payment.setStatus(ScheduledPaymentStatus.ACTIVE);

        // If next execution was in the past while paused, move it to today or future
        if (payment.getNextExecutionDate().isBefore(LocalDate.now())) {
            payment.setNextExecutionDate(
                    calculateNextExecutionDate(LocalDate.now().minusDays(1), payment.getFrequency()));
        }

        return ScheduledPaymentResponse.from(scheduledPaymentRepository.save(payment));
    }

    @Transactional
    public ScheduledPaymentResponse cancelScheduledPayment(Long id, String reason) {
        ScheduledPayment payment = findScheduledPayment(id);
        if (payment.getStatus() == ScheduledPaymentStatus.CANCELLED) {
            throw new ApiException("SCHEDULED_PAYMENT_ALREADY_CANCELLED", "Scheduled payment is already cancelled");
        }
        payment.setStatus(ScheduledPaymentStatus.CANCELLED);
        payment.setLastFailureReason("Cancelled: " + reason);
        return ScheduledPaymentResponse.from(scheduledPaymentRepository.save(payment));
    }

    @Scheduled(cron = "${cbs.scheduled-payments.execution-cron:0 0 1 * * *}") // Default 1 AM daily
    @Transactional
    public void executeScheduledPayments() {
        log.info("Starting execution of scheduled payments for date: {}", LocalDate.now());
        List<ScheduledPayment> duePayments = scheduledPaymentRepository
                .findByNextExecutionDateLessThanEqualAndStatus(LocalDate.now(), ScheduledPaymentStatus.ACTIVE);

        for (ScheduledPayment scheduled : duePayments) {
            try {
                processExecution(scheduled);
            } catch (Exception e) {
                log.error("Failed to execute scheduled payment ID: {}. Error: {}", scheduled.getId(), e.getMessage());
                handleExecutionFailure(scheduled, e.getMessage());
            }
        }
    }

    private void processExecution(ScheduledPayment scheduled) {
        // Create one-shot payment
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                scheduled.getCustomerId(),
                scheduled.getSourceAccountId(),
                scheduled.getDestinationAccountId(),
                scheduled.getAmount(),
                scheduled.getCurrency(),
                scheduled.getMethod(),
                generateExecutionReference(scheduled),
                scheduled.getDescription() + " (Scheduled)",
                LocalDate.now());

        paymentService.createPayment(paymentRequest);

        // Update scheduled payment state
        scheduled.setLastExecutedDate(LocalDate.now());
        scheduled.setExecutionCount(scheduled.getExecutionCount() + 1);
        scheduled.setFailureCount(0);
        scheduled.setLastFailureReason(null);

        LocalDate nextDate = calculateNextExecutionDate(scheduled.getNextExecutionDate(), scheduled.getFrequency());

        if (scheduled.getEndDate() != null && nextDate.isAfter(scheduled.getEndDate())) {
            scheduled.setStatus(ScheduledPaymentStatus.COMPLETED);
            scheduled.setNextExecutionDate(null);
        } else {
            scheduled.setNextExecutionDate(nextDate);
        }

        scheduledPaymentRepository.save(scheduled);
    }

    private void handleExecutionFailure(ScheduledPayment scheduled, String reason) {
        scheduled.setFailureCount(scheduled.getFailureCount() + 1);
        scheduled.setLastFailureReason(truncateReason(reason));

        if (scheduled.getFailureCount() >= MAX_FAILURE_COUNT) {
            log.warn("Scheduled payment ID: {} auto-paused due to {} consecutive failures", scheduled.getId(),
                    MAX_FAILURE_COUNT);
            scheduled.setStatus(ScheduledPaymentStatus.PAUSED);
        }

        scheduledPaymentRepository.save(scheduled);
    }

    private LocalDate calculateNextExecutionDate(LocalDate lastDate, ScheduleFrequency frequency) {
        return switch (frequency) {
            case DAILY -> lastDate.plusDays(1);
            case WEEKLY -> lastDate.plusWeeks(1);
            case MONTHLY -> lastDate.plusMonths(1);
            case QUARTERLY -> lastDate.plusMonths(3);
            case YEARLY -> lastDate.plusYears(1);
        };
    }

    private String generateExecutionReference(ScheduledPayment scheduled) {
        return scheduled.getReference() + "-" + (scheduled.getExecutionCount() + 1);
    }

    private ScheduledPayment findScheduledPayment(Long id) {
        return scheduledPaymentRepository.findById(id)
                .orElseThrow(() -> new ApiException("SCHEDULED_PAYMENT_NOT_FOUND", "Scheduled payment not found"));
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }

    private String truncateReason(String reason) {
        if (reason == null)
            return null;
        return reason.length() > 255 ? reason.substring(0, 255) : reason;
    }
}
