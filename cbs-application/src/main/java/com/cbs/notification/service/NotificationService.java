package com.cbs.notification.service;

import com.cbs.common.exception.ApiException;
import com.cbs.notification.dto.CreateNotificationRequest;
import com.cbs.notification.dto.NotificationResponse;
import com.cbs.notification.dto.NotificationStatusReasonRequest;
import com.cbs.notification.model.Notification;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import com.cbs.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        String reference = normalizeReference(request.reference());
        if (notificationRepository.existsByReference(reference)) {
            throw new ApiException("NOTIFICATION_REFERENCE_EXISTS", "Reference already exists");
        }

        Notification notification = new Notification(
                request.customerId(),
                request.recipient().trim(),
                request.channel(),
                request.subject().trim(),
                request.message().trim(),
                reference
        );

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId) {
        return NotificationResponse.from(findNotification(notificationId));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(Long customerId,
                                                        NotificationStatus status,
                                                        NotificationChannel channel) {
        List<Notification> notifications;

        if (customerId != null && status != null && channel == null) {
            notifications = notificationRepository.findByCustomerIdAndStatusOrderByIdDesc(customerId, status);
        } else if (customerId != null && status == null && channel == null) {
            notifications = notificationRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else if (status != null && customerId == null && channel == null) {
            notifications = notificationRepository.findByStatusOrderByIdDesc(status);
        } else if (channel != null && customerId == null && status == null) {
            notifications = notificationRepository.findByChannelOrderByIdDesc(channel);
        } else {
            notifications = notificationRepository.findAll().stream()
                    .filter(item -> customerId == null || item.getCustomerId().equals(customerId))
                    .filter(item -> status == null || item.getStatus() == status)
                    .filter(item -> channel == null || item.getChannel() == channel)
                    .sorted(Comparator.comparing(Notification::getId).reversed())
                    .toList();
        }

        return notifications.stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markSent(Long notificationId) {
        Notification notification = findNotification(notificationId);
        if (notification.getStatus() == NotificationStatus.CANCELLED) {
            throw new ApiException("NOTIFICATION_CANCELLED", "Cancelled notification cannot be sent");
        }

        notification.setStatus(NotificationStatus.SENT);
        notification.setStatusReason(null);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse markFailed(Long notificationId, NotificationStatusReasonRequest request) {
        Notification notification = findNotification(notificationId);
        if (notification.getStatus() == NotificationStatus.CANCELLED) {
            throw new ApiException("NOTIFICATION_CANCELLED", "Cancelled notification cannot be failed");
        }

        notification.setStatus(NotificationStatus.FAILED);
        notification.setStatusReason(request.reason().trim());
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse cancelNotification(Long notificationId, NotificationStatusReasonRequest request) {
        Notification notification = findNotification(notificationId);
        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new ApiException("NOTIFICATION_ALREADY_SENT", "Sent notification cannot be cancelled");
        }

        notification.setStatus(NotificationStatus.CANCELLED);
        notification.setStatusReason(request.reason().trim());
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    private Notification findNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApiException("NOTIFICATION_NOT_FOUND", "Notification not found"));
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }
}
