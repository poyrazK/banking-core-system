package com.cbs.notification.repository;

import com.cbs.notification.model.Notification;
import com.cbs.notification.model.NotificationChannel;
import com.cbs.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByReference(String reference);

    List<Notification> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Notification> findByStatusOrderByIdDesc(NotificationStatus status);

    List<Notification> findByChannelOrderByIdDesc(NotificationChannel channel);

    List<Notification> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, NotificationStatus status);
}
