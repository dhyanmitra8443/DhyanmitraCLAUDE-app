package com.lms.notification.repository;

import com.lms.notification.entity.Notification;
import com.lms.notification.entity.ReadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByRecipientUserId(UUID recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientUserIdAndReadStatus(UUID recipientUserId, ReadStatus readStatus, Pageable pageable);

    List<Notification> findByRecipientUserIdAndReadStatus(UUID recipientUserId, ReadStatus readStatus);
}
