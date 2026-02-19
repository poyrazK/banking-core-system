package com.cbs.notification.service;

import com.cbs.notification.dto.CreateNotificationRequest;
import com.cbs.notification.dto.NotificationResponse;
import com.cbs.notification.dto.NotificationStatusReasonRequest;
import com.cbs.notification.model.Notification;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import com.cbs.notification.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class NotificationServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55442/cbs_notification_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void createNotificationPersistsNormalizedReferenceInPostgres() {
        NotificationResponse response = notificationService.createNotification(new CreateNotificationRequest(
                1L,
                " user@example.com ",
                NotificationChannel.EMAIL,
                " Welcome ",
                " Hello user ",
                " ref-1 "
        ));

        assertEquals("REF-1", response.reference());
        assertTrue(notificationRepository.existsByReference("REF-1"));
    }

    @Test
    void markFailedPersistsStatusAndReason() {
        NotificationResponse created = notificationService.createNotification(new CreateNotificationRequest(
                2L,
                "+905550000000",
                NotificationChannel.SMS,
                "Alert",
                "Balance alert",
                "REF-2"
        ));

        NotificationResponse failed = notificationService.markFailed(created.id(), new NotificationStatusReasonRequest(" provider error "));
        assertEquals(NotificationStatus.FAILED, failed.status());
        assertEquals("provider error", failed.statusReason());

        Notification persisted = notificationRepository.findById(created.id()).orElseThrow();
        assertEquals(NotificationStatus.FAILED, persisted.getStatus());
    }
}