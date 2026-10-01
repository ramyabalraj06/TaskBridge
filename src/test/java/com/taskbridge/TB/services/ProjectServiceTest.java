package com.taskbridge.TB.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.StoryRepository;
import com.taskbridge.TB.repositories.TeamRepository;
import com.taskbridge.TB.security.CurrentActorProvider;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private TeamRepository teamRepository;
	@Mock
	private StoryRepository storyRepository;
	@Mock
	private AuditService auditService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private ProjectSnapshotSerializer projectSnapshotSerializer;
	@Mock
	private CurrentActorProvider currentActorProvider;

	private ProjectService projectService;

	@BeforeEach
	void setUp() {
		projectService = new ProjectService(projectRepository, teamRepository, storyRepository,
				auditService, notificationService, projectSnapshotSerializer, currentActorProvider);
	}

	@Test
	void createAssignsTeamAndPlanningStatus() {
		Team team = team(10L);
		Project project = new Project("  Platform  ", "Description", null);
		when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
		when(projectRepository.save(project)).thenReturn(project);

		Project created = projectService.create(project, 10L);

		assertSame(team, created.getTeam());
		assertEquals("Platform", created.getName());
		assertEquals(ProjectStatus.PLANNING, created.getStatus());
	}

	@Test
	void createRejectsInvalidInputAndMissingTeam() {
		when(teamRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> projectService.create(null, 1L));
		assertThrows(IllegalArgumentException.class,
				() -> projectService.create(new Project(" ", null, null), 1L));
		assertThrows(IllegalArgumentException.class,
				() -> projectService.create(new Project("Project", null, null), null));
		assertThrows(NoSuchElementException.class,
				() -> projectService.create(new Project("Project", null, null), 99L));
	}

	@Test
	void activatingSecondProjectForTeamIsRejected() {
		Project project = project(1L, team(10L), ProjectStatus.PLANNING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.existsByTeamIdAndStatusAndIdNot(10L, ProjectStatus.ACTIVE, 1L))
				.thenReturn(true);

		assertThrows(IllegalStateException.class,
				() -> projectService.updateStatus(1L, ProjectStatus.ACTIVE));
		verify(projectRepository, never()).save(project);
	}

	@Test
	void updateStatusPersistsAllowedChange() {
		Project project = project(1L, team(10L), ProjectStatus.PLANNING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(project)).thenReturn(project);

		assertEquals(ProjectStatus.ARCHIVED,
				projectService.updateStatus(1L, ProjectStatus.ARCHIVED).getStatus());
	}

	@Test
	void getByTeamReturnsRepositoryResults() {
		List<Project> projects = List.of(project(1L, team(10L), ProjectStatus.PLANNING));
		when(projectRepository.findByTeamId(10L)).thenReturn(projects);

		assertSame(projects, projectService.getByTeam(10L));
		assertThrows(IllegalArgumentException.class, () -> projectService.getByTeam(null));
	}

	@Test
	void deleteIsBlockedWhileStoriesExist() {
		Project project = project(1L, team(10L), ProjectStatus.ACTIVE);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(storyRepository.existsByProjectId(1L)).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> projectService.delete(1L));
		verify(projectRepository, never()).delete(project);
	}

	@Test
	void getByIdAndDeleteEmptyProject() {
		Project project = project(1L, team(10L), ProjectStatus.PLANNING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		assertSame(project, projectService.getById(1L));
		projectService.delete(1L);

		verify(projectRepository).delete(project);
	}

	private Team team(Long id) {
		Team team = new Team("Team", null);
		team.setId(id);
		return team;
	}

	private Project project(Long id, Team team, ProjectStatus status) {
		Project project = new Project("Project", null, team);
		project.setId(id);
		project.setStatus(status);
		return project;
	}
}