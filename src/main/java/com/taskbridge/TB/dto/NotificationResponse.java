package com.taskbridge.TB.dto;

import java.time.Instant;

import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.Notification;

public record NotificationResponse(Long id, String recipientUserId, AuditEventType eventType,
		Long projectId, String message, boolean read, Instant createdAt, Instant readAt) {

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(notification.getId(), notification.getRecipientUserId(),
				notification.getEventType(), notification.getProjectId(), notification.getMessage(),
				notification.isRead(), notification.getCreatedAt(), notification.getReadAt());
	}
}