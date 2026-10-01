package com.taskbridge.TB.services;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.AuditEntry;
import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.AuditedEntityType;
import com.taskbridge.TB.repositories.AuditEntryRepository;

@Service
@Transactional
public class AuditService {

	private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

	private final AuditEntryRepository auditEntryRepository;

	public AuditService(AuditEntryRepository auditEntryRepository) {
		this.auditEntryRepository = auditEntryRepository;
	}

	public AuditEntry record(Long projectId, AuditEventType eventType, AuditedEntityType entityType,
			Long entityId, String actorUserId, Long actorOrganisationId,
			String previousStateSnapshot, String newStateSnapshot) {
		validate(projectId, eventType, entityType, entityId, actorUserId, actorOrganisationId,
				previousStateSnapshot, newStateSnapshot);

		AuditEntry entry = new AuditEntry(projectId, eventType, entityType, entityId,
				actorUserId.trim(), actorOrganisationId, previousStateSnapshot, newStateSnapshot);
		AuditEntry saved = auditEntryRepository.save(entry);
		logger.info("Recorded audit event={} projectId={} actorUserId={}",
				eventType, projectId, actorUserId);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<AuditEntry> getHistory(Long projectId, AuditEventType eventType, Instant from, Instant to) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project ID is required");
		}
		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException("Audit date range is invalid");
		}
		return auditEntryRepository.findHistory(projectId, eventType, from, to);
	}

	private void validate(Long projectId, AuditEventType eventType, AuditedEntityType entityType,
			Long entityId, String actorUserId, Long actorOrganisationId,
			String previousStateSnapshot, String newStateSnapshot) {
		if (projectId == null || eventType == null || entityType == null || entityId == null
				|| actorUserId == null || actorUserId.isBlank() || actorOrganisationId == null) {
			throw new IllegalArgumentException("Complete audit identity and event data are required");
		}
		if (eventType == AuditEventType.PROJECT_CREATED
				&& (previousStateSnapshot != null || newStateSnapshot == null)) {
			throw new IllegalArgumentException("Project creation requires only a new snapshot");
		}
		if (eventType == AuditEventType.PROJECT_STATUS_UPDATED
				&& (previousStateSnapshot == null || newStateSnapshot == null)) {
			throw new IllegalArgumentException("Project status updates require before and after snapshots");
		}
		if (eventType == AuditEventType.PROJECT_DELETED
				&& (previousStateSnapshot == null || newStateSnapshot != null)) {
			throw new IllegalArgumentException("Project deletion requires only a previous snapshot");
		}
	}
}