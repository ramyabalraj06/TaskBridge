package com.taskbridge.TB.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications", indexes = {
		@Index(name = "idx_notification_recipient_read", columnList = "recipient_user_id, is_read")
})
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient_user_id", nullable = false, updatable = false, length = 100)
	private String recipientUserId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, updatable = false, length = 40)
	private AuditEventType eventType;

	@Column(name = "project_id", nullable = false, updatable = false)
	private Long projectId;

	@Column(name = "message", nullable = false, updatable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	protected Notification() {
	}

	public Notification(String recipientUserId, AuditEventType eventType, Long projectId, String message) {
		this.recipientUserId = recipientUserId;
		this.eventType = eventType;
		this.projectId = projectId;
		this.message = message;
		this.read = false;
		this.createdAt = Instant.now();
	}

	public void markRead() {
		if (!read) {
			read = true;
			readAt = Instant.now();
		}
	}

	public Long getId() { return id; }
	public String getRecipientUserId() { return recipientUserId; }
	public AuditEventType getEventType() { return eventType; }
	public Long getProjectId() { return projectId; }
	public String getMessage() { return message; }
	public boolean isRead() { return read; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getReadAt() { return readAt; }
}