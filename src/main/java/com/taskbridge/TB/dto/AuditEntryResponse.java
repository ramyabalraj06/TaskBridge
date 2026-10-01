package com.taskbridge.TB.dto;

import java.time.Instant;

import com.taskbridge.TB.models.AuditEntry;
import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.AuditedEntityType;

public record AuditEntryResponse(Long id, Long projectId, AuditEventType eventType,
		AuditedEntityType entityType, Long entityId, String actorUserId,
		Long actorOrganisationId, String previousStateSnapshot,
		String newStateSnapshot, Instant occurredAt) {

	public static AuditEntryResponse from(AuditEntry entry) {
		return new AuditEntryResponse(entry.getId(), entry.getProjectId(), entry.getEventType(),
				entry.getEntityType(), entry.getEntityId(), entry.getActorUserId(),
				entry.getActorOrganisationId(), entry.getPreviousStateSnapshot(),
				entry.getNewStateSnapshot(), entry.getOccurredAt());
	}
}