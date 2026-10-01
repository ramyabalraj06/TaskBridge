package com.taskbridge.TB.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskbridge.TB.models.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(String recipientUserId);
}