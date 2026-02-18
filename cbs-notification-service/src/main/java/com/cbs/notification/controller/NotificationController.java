package com.cbs.notification.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.notification.dto.CreateNotificationRequest;
import com.cbs.notification.dto.NotificationResponse;
import com.cbs.notification.dto.NotificationStatusReasonRequest;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import com.cbs.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Notification created", response));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable("notificationId") Long notificationId
    ) {
        NotificationResponse response = notificationService.getNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) NotificationStatus status,
            @RequestParam(value = "channel", required = false) NotificationChannel channel
    ) {
        List<NotificationResponse> responses = notificationService.listNotifications(customerId, status, channel);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", responses));
    }

    @PatchMapping("/{notificationId}/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> markSent(@PathVariable("notificationId") Long notificationId) {
        NotificationResponse response = notificationService.markSent(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as sent", response));
    }

    @PatchMapping("/{notificationId}/fail")
    public ResponseEntity<ApiResponse<NotificationResponse>> markFailed(
            @PathVariable("notificationId") Long notificationId,
            @Valid @RequestBody NotificationStatusReasonRequest request
    ) {
        NotificationResponse response = notificationService.markFailed(notificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as failed", response));
    }

    @PatchMapping("/{notificationId}/cancel")
    public ResponseEntity<ApiResponse<NotificationResponse>> cancelNotification(
            @PathVariable("notificationId") Long notificationId,
            @Valid @RequestBody NotificationStatusReasonRequest request
    ) {
        NotificationResponse response = notificationService.cancelNotification(notificationId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification cancelled", response));
    }
}
