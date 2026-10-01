package com.taskbridge.TB.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.taskbridge.TB.models.AuditEntry;
import com.taskbridge.TB.models.AuditEventType;

public interface AuditEntryRepository extends Repository<AuditEntry, Long> {

	AuditEntry save(AuditEntry auditEntry);

	@Query("""
			SELECT entry FROM AuditEntry entry
			WHERE entry.projectId = :projectId
			AND (:eventType IS NULL OR entry.eventType = :eventType)
			AND (:from IS NULL OR entry.occurredAt >= :from)
			AND (:to IS NULL OR entry.occurredAt <= :to)
			ORDER BY entry.occurredAt DESC
			""")
	List<AuditEntry> findHistory(@Param("projectId") Long projectId,
			@Param("eventType") AuditEventType eventType,
			@Param("from") Instant from, @Param("to") Instant to);
}