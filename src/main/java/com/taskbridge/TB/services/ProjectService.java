package com.taskbridge.TB.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.AuditedEntityType;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.StoryRepository;
import com.taskbridge.TB.repositories.TeamRepository;
import com.taskbridge.TB.security.CurrentActorProvider;

@Service
@Transactional
public class ProjectService {

	private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

	private final ProjectRepository projectRepository;
	private final TeamRepository teamRepository;
	private final StoryRepository storyRepository;
	private final AuditService auditService;
	private final NotificationService notificationService;
	private final ProjectSnapshotSerializer projectSnapshotSerializer;
	private final CurrentActorProvider currentActorProvider;

	public ProjectService(ProjectRepository projectRepository, TeamRepository teamRepository,
			StoryRepository storyRepository, AuditService auditService,
			NotificationService notificationService, ProjectSnapshotSerializer projectSnapshotSerializer,
			CurrentActorProvider currentActorProvider) {
		this.projectRepository = projectRepository;
		this.teamRepository = teamRepository;
		this.storyRepository = storyRepository;
		this.auditService = auditService;
		this.notificationService = notificationService;
		this.projectSnapshotSerializer = projectSnapshotSerializer;
		this.currentActorProvider = currentActorProvider;
	}

	public Project create(Project project, Long teamId) {
		if (project == null) {
			throw new IllegalArgumentException("Project is required");
		}
		if (project.getId() != null) {
			throw new IllegalArgumentException("New projects cannot have an ID");
		}
		if (project.getName() == null || project.getName().isBlank()) {
			throw new IllegalArgumentException("Project name is required");
		}
		if (teamId == null) {
			throw new IllegalArgumentException("Team ID is required");
		}

		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new NoSuchElementException("Team " + teamId + " was not found"));
		project.setName(project.getName().trim());
		project.setTeam(team);
		project.setStatus(ProjectStatus.PLANNING);
		Project saved = projectRepository.save(project);
		String actorUserId = currentActorProvider.getCurrentUserId();
		auditService.record(saved.getId(), AuditEventType.PROJECT_CREATED, AuditedEntityType.PROJECT,
				saved.getId(), actorUserId, teamId, null, projectSnapshotSerializer.serialize(saved));
		notificationService.notifyTeam(team, actorUserId, AuditEventType.PROJECT_CREATED, saved.getId(),
				"Project '" + saved.getName() + "' was created");
		logger.info("Created project id={} teamId={} status={}", saved.getId(), teamId, saved.getStatus());
		return saved;
	}

	public Project updateStatus(Long projectId, ProjectStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("Project status is required");
		}

		Project project = findById(projectId);
		if (project.getStatus() == status) {
			return project;
		}
		if (status == ProjectStatus.ACTIVE
				&& projectRepository.existsByTeamIdAndStatusAndIdNot(
						project.getTeam().getId(), ProjectStatus.ACTIVE, projectId)) {
			logger.warn("Rejected activation of project id={} because teamId={} already has an active project",
					projectId, project.getTeam().getId());
			throw new IllegalStateException("Team already has an active project");
		}
		ProjectStatus previousStatus = project.getStatus();
		String previousSnapshot = projectSnapshotSerializer.serialize(project);
		project.setStatus(status);
		Project saved = projectRepository.save(project);
		String actorUserId = currentActorProvider.getCurrentUserId();
		auditService.record(projectId, AuditEventType.PROJECT_STATUS_UPDATED, AuditedEntityType.PROJECT,
				projectId, actorUserId, project.getTeam().getId(), previousSnapshot,
				projectSnapshotSerializer.serialize(saved));
		notificationService.notifyTeam(project.getTeam(), actorUserId,
				AuditEventType.PROJECT_STATUS_UPDATED, projectId,
				"Project '" + project.getName() + "' changed from " + previousStatus + " to " + status);
		logger.info("Updated project id={} status={}", projectId, status);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Project> getByTeam(Long teamId) {
		if (teamId == null) {
			throw new IllegalArgumentException("Team ID is required");
		}
		return projectRepository.findByTeamId(teamId);
	}

	public void delete(Long projectId) {
		Project project = findById(projectId);
		if (storyRepository.existsByProjectId(projectId)) {
			logger.warn("Rejected deletion of project id={} because stories exist", projectId);
			throw new IllegalStateException("Project cannot be deleted while stories exist");
		}
		String previousSnapshot = projectSnapshotSerializer.serialize(project);
		String actorUserId = currentActorProvider.getCurrentUserId();
		Team team = project.getTeam();
		projectRepository.delete(project);
		auditService.record(projectId, AuditEventType.PROJECT_DELETED, AuditedEntityType.PROJECT,
				projectId, actorUserId, team.getId(), previousSnapshot, null);
		notificationService.notifyTeam(team, actorUserId, AuditEventType.PROJECT_DELETED, projectId,
				"Project '" + project.getName() + "' was deleted");
		logger.info("Deleted project id={}", projectId);
	}

	@Transactional(readOnly = true)
	public Project getById(Long projectId) {
		return findById(projectId);
	}

	private Project findById(Long projectId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project ID is required");
		}
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new NoSuchElementException("Project " + projectId + " was not found"));
	}
}