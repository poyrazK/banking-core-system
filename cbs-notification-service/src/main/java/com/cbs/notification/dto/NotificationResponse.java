package com.cbs.notification.dto;

import com.cbs.notification.model.Notification;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;

public record NotificationResponse(
        Long id,
        Long customerId,
        String recipient,
        NotificationChannel channel,
        NotificationStatus status,
        String subject,
        String message,
        String reference,
        String statusReason
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCustomerId(),
                notification.getRecipient(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getReference(),
                notification.getStatusReason()
        );
    }
}
