package com.taskbridge.TB.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Story;
import com.taskbridge.TB.models.StoryPriority;
import com.taskbridge.TB.models.StoryStatus;
import com.taskbridge.TB.models.Team;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceIntegrationTest {

	@Autowired
	private TeamService teamService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private StoryService storyService;

	@Test
	void teamProjectStoryLifecyclePersistsAndEnforcesRules() {
		Team team = teamService.create(new Team("Delivery", "Core team"));
		Project first = projectService.create(new Project("First", null, null), team.getId());
		Project second = projectService.create(new Project("Second", null, null), team.getId());

		projectService.updateStatus(first.getId(), ProjectStatus.ACTIVE);
		assertThrows(IllegalStateException.class,
				() -> projectService.updateStatus(second.getId(), ProjectStatus.ACTIVE));

		Story story = storyService.create(
				new Story("Secure writes", "Add CSRF coverage", StoryPriority.HIGH, 5), first.getId());
		assertNotNull(team.getCreatedAt());
		assertNotNull(first.getCreatedAt());
		assertNotNull(story.getCreatedAt());
		assertEquals(StoryStatus.TO_DO, story.getStatus());
		assertEquals(team.getId(), first.getTeam().getId());
		assertEquals(first.getId(), story.getProject().getId());

		storyService.updateStatus(story.getId(), StoryStatus.IN_PROGRESS);
		storyService.updateStatus(story.getId(), StoryStatus.DONE);
		assertThrows(IllegalStateException.class,
				() -> storyService.updateStatus(story.getId(), StoryStatus.TO_DO));
		assertThrows(IllegalStateException.class, () -> projectService.delete(first.getId()));
		assertThrows(IllegalStateException.class, () -> teamService.delete(team.getId()));
	}
}