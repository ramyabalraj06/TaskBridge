package com.taskbridge.TB.controllers;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taskbridge.TB.dto.AuditEntryResponse;
import com.taskbridge.TB.dto.AuditRecordRequest;
import com.taskbridge.TB.models.AuditEntry;
import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.security.CurrentActorProvider;
import com.taskbridge.TB.services.AuditService;
import com.taskbridge.TB.services.ProjectService;

@Controller
@RequestMapping("/audit")
public class AuditApiController {

	private final AuditService auditService;
	private final ProjectService projectService;
	private final CurrentActorProvider currentActorProvider;

	public AuditApiController(AuditService auditService, ProjectService projectService,
			CurrentActorProvider currentActorProvider) {
		this.auditService = auditService;
		this.projectService = projectService;
		this.currentActorProvider = currentActorProvider;
	}

	@PostMapping
	@ResponseBody
	public AuditEntryResponse record(@RequestBody AuditRecordRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Audit request is required");
		}
		Project project = projectService.getById(request.projectId());
		AuditEntry entry = auditService.record(request.projectId(), request.eventType(),
				request.entityType(), request.entityId(), currentActorProvider.getCurrentUserId(),
				project.getTeam().getId(), request.previousStateSnapshot(), request.newStateSnapshot());
		return AuditEntryResponse.from(entry);
	}

	@GetMapping("/{projectId}")
	@ResponseBody
	public List<AuditEntryResponse> history(@PathVariable Long projectId,
			@RequestParam(required = false) AuditEventType eventType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
		return auditService.getHistory(projectId, eventType, from, to).stream()
				.map(AuditEntryResponse::from)
				.toList();
	}
}