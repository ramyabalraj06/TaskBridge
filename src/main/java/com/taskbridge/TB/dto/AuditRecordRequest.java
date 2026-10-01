package com.taskbridge.TB.dto;

import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.AuditedEntityType;

public record AuditRecordRequest(Long projectId, AuditEventType eventType,
		AuditedEntityType entityType, Long entityId,
		String previousStateSnapshot, String newStateSnapshot) {
}