package com.taskbridge.TB.models;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

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
@Immutable
@Table(name = "audit_entries", indexes = {
		@Index(name = "idx_audit_project_time", columnList = "project_id, occurred_at")
})
public class AuditEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "project_id", nullable = false, updatable = false)
	private Long projectId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, updatable = false, length = 40)
	private AuditEventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(name = "entity_type", nullable = false, updatable = false, length = 40)
	private AuditedEntityType entityType;

	@Column(name = "entity_id", nullable = false, updatable = false)
	private Long entityId;

	@Column(name = "actor_user_id", nullable = false, updatable = false, length = 100)
	private String actorUserId;

	@Column(name = "actor_organisation_id", nullable = false, updatable = false)
	private Long actorOrganisationId;

	@Column(name = "previous_state_snapshot", updatable = false, columnDefinition = "TEXT")
	private String previousStateSnapshot;

	@Column(name = "new_state_snapshot", updatable = false, columnDefinition = "TEXT")
	private String newStateSnapshot;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	protected AuditEntry() {
	}

	public AuditEntry(Long projectId, AuditEventType eventType, AuditedEntityType entityType,
			Long entityId, String actorUserId, Long actorOrganisationId,
			String previousStateSnapshot, String newStateSnapshot) {
		this.projectId = projectId;
		this.eventType = eventType;
		this.entityType = entityType;
		this.entityId = entityId;
		this.actorUserId = actorUserId;
		this.actorOrganisationId = actorOrganisationId;
		this.previousStateSnapshot = previousStateSnapshot;
		this.newStateSnapshot = newStateSnapshot;
		this.occurredAt = Instant.now();
	}

	public Long getId() { return id; }
	public Long getProjectId() { return projectId; }
	public AuditEventType getEventType() { return eventType; }
	public AuditedEntityType getEntityType() { return entityType; }
	public Long getEntityId() { return entityId; }
	public String getActorUserId() { return actorUserId; }
	public Long getActorOrganisationId() { return actorOrganisationId; }
	public String getPreviousStateSnapshot() { return previousStateSnapshot; }
	public String getNewStateSnapshot() { return newStateSnapshot; }
	public Instant getOccurredAt() { return occurredAt; }
}