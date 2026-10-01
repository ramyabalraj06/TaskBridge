package com.taskbridge.TB.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Story;
import com.taskbridge.TB.models.StoryPriority;
import com.taskbridge.TB.models.StoryStatus;
import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.StoryRepository;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

	@Mock
	private StoryRepository storyRepository;
	@Mock
	private ProjectRepository projectRepository;

	private StoryService storyService;

	@BeforeEach
	void setUp() {
		storyService = new StoryService(storyRepository, projectRepository);
	}

	@Test
	void createAssignsActiveProjectAndDefaultStatus() {
		Project project = project(ProjectStatus.ACTIVE);
		Story story = new Story("  Login flow  ", null, StoryPriority.HIGH, 5);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(storyRepository.save(story)).thenReturn(story);

		Story created = storyService.create(story, 1L);

		assertSame(project, created.getProject());
		assertEquals("Login flow", created.getTitle());
		assertEquals(StoryStatus.TO_DO, created.getStatus());
	}

	@Test
	void createRejectsInvalidStoryAndInactiveProject() {
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.PLANNING)));

		assertThrows(IllegalArgumentException.class, () -> storyService.create(null, 1L));
		assertThrows(IllegalArgumentException.class,
				() -> storyService.create(new Story(" ", null, StoryPriority.LOW, 1), 1L));
		assertThrows(IllegalArgumentException.class,
				() -> storyService.create(new Story("Story", null, null, 1), 1L));
		assertThrows(IllegalArgumentException.class,
				() -> storyService.create(new Story("Story", null, StoryPriority.LOW, -1), 1L));
		assertThrows(IllegalStateException.class,
				() -> storyService.create(new Story("Story", null, StoryPriority.LOW, 1), 1L));
	}

	@ParameterizedTest
	@CsvSource({"TO_DO,IN_PROGRESS", "TO_DO,CANCELLED", "IN_PROGRESS,DONE", "IN_PROGRESS,CANCELLED"})
	void updateStatusAllowsForwardTransitions(StoryStatus current, StoryStatus next) {
		Story story = story(current, project(ProjectStatus.ACTIVE));
		when(storyRepository.findById(2L)).thenReturn(Optional.of(story));
		when(storyRepository.save(story)).thenReturn(story);

		assertEquals(next, storyService.updateStatus(2L, next).getStatus());
	}

	@ParameterizedTest
	@CsvSource({"TO_DO,DONE", "IN_PROGRESS,TO_DO", "DONE,TO_DO", "CANCELLED,TO_DO"})
	void updateStatusRejectsInvalidTransitions(StoryStatus current, StoryStatus next) {
		Story story = story(current, project(ProjectStatus.ACTIVE));
		when(storyRepository.findById(2L)).thenReturn(Optional.of(story));

		assertThrows(IllegalStateException.class, () -> storyService.updateStatus(2L, next));
		verify(storyRepository, never()).save(story);
	}

	@Test
	void getByProjectReturnsRepositoryResults() {
		List<Story> stories = List.of(story(StoryStatus.TO_DO, project(ProjectStatus.ACTIVE)));
		when(storyRepository.findByProjectId(1L)).thenReturn(stories);

		assertSame(stories, storyService.getByProject(1L));
		assertThrows(IllegalArgumentException.class, () -> storyService.getByProject(null));
	}

	@Test
	void deleteRequiresActiveProject() {
		Story story = story(StoryStatus.TO_DO, project(ProjectStatus.PLANNING));
		when(storyRepository.findById(2L)).thenReturn(Optional.of(story));

		assertThrows(IllegalStateException.class, () -> storyService.delete(2L));
		verify(storyRepository, never()).delete(story);
	}

	@Test
	void getByIdAndDeleteActiveStory() {
		Story story = story(StoryStatus.TO_DO, project(ProjectStatus.ACTIVE));
		when(storyRepository.findById(2L)).thenReturn(Optional.of(story));

		assertSame(story, storyService.getById(2L));
		storyService.delete(2L);

		verify(storyRepository).delete(story);
	}

	private Project project(ProjectStatus status) {
		Team team = new Team("Team", null);
		team.setId(10L);
		Project project = new Project("Project", null, team);
		project.setId(1L);
		project.setStatus(status);
		return project;
	}

	private Story story(StoryStatus status, Project project) {
		Story story = new Story("Story", null, StoryPriority.MEDIUM, 3);
		story.setId(2L);
		story.setProject(project);
		story.setStatus(status);
		return story;
	}
}