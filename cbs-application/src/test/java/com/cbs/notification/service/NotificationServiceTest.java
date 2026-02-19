package com.cbs.notification.service;

import com.cbs.common.exception.ApiException;
import com.cbs.notification.dto.CreateNotificationRequest;
import com.cbs.notification.dto.NotificationResponse;
import com.cbs.notification.dto.NotificationStatusReasonRequest;
import com.cbs.notification.model.Notification;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import com.cbs.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void createNotification_normalizesReferenceAndTextFields() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                1L,
                "  user@example.com  ",
                NotificationChannel.EMAIL,
                "  Welcome  ",
                "  Hello user  ",
                "  ref-1  "
        );
        when(notificationRepository.existsByReference("REF-1")).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.createNotification(request);

        assertEquals("REF-1", response.reference());
        assertEquals("user@example.com", response.recipient());
        assertEquals("Welcome", response.subject());
    }

    @Test
    void createNotification_throwsWhenReferenceExists() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                1L,
                "user@example.com",
                NotificationChannel.EMAIL,
                "Subject",
                "Message",
                "REF-1"
        );
        when(notificationRepository.existsByReference("REF-1")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> notificationService.createNotification(request));

        assertEquals("NOTIFICATION_REFERENCE_EXISTS", exception.getErrorCode());
    }

    @Test
    void markSent_throwsWhenCancelled() {
        Notification notification = createNotificationWithStatus(NotificationStatus.CANCELLED);
        when(notificationRepository.findById(11L)).thenReturn(Optional.of(notification));

        ApiException exception = assertThrows(ApiException.class, () -> notificationService.markSent(11L));

        assertEquals("NOTIFICATION_CANCELLED", exception.getErrorCode());
    }

    @Test
    void cancelNotification_throwsWhenAlreadySent() {
        Notification notification = createNotificationWithStatus(NotificationStatus.SENT);
        when(notificationRepository.findById(12L)).thenReturn(Optional.of(notification));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> notificationService.cancelNotification(12L, new NotificationStatusReasonRequest("duplicate"))
        );

        assertEquals("NOTIFICATION_ALREADY_SENT", exception.getErrorCode());
    }

    @Test
    void markFailed_setsFailedStatusAndReason() {
        Notification notification = createNotificationWithStatus(NotificationStatus.CREATED);
        when(notificationRepository.findById(13L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markFailed(13L, new NotificationStatusReasonRequest("  provider error  "));

        assertEquals(NotificationStatus.FAILED, response.status());
        assertEquals("provider error", response.statusReason());
    }

    private Notification createNotificationWithStatus(NotificationStatus status) {
        Notification notification = new Notification(
                1L,
                "user@example.com",
                NotificationChannel.EMAIL,
                "Subject",
                "Message",
                "REF-1"
        );
        notification.setStatus(status);
        return notification;
    }
}
