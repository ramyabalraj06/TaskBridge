package com.taskbridge.TB.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Story;
import com.taskbridge.TB.models.StoryStatus;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.StoryRepository;

@Service
@Transactional
public class StoryService {

	private static final Logger logger = LoggerFactory.getLogger(StoryService.class);

	private final StoryRepository storyRepository;
	private final ProjectRepository projectRepository;

	public StoryService(StoryRepository storyRepository, ProjectRepository projectRepository) {
		this.storyRepository = storyRepository;
		this.projectRepository = projectRepository;
	}

	public Story create(Story story, Long projectId) {
		validateNewStory(story);
		Project project = getProject(projectId);
		requireActive(project);

		story.setTitle(story.getTitle().trim());
		story.setStatus(StoryStatus.TO_DO);
		story.setProject(project);
		Story saved = storyRepository.save(story);
		logger.info("Created story id={} projectId={} status={}", saved.getId(), projectId, saved.getStatus());
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Story> getByProject(Long projectId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project ID is required");
		}
		return storyRepository.findByProjectId(projectId);
	}

	@Transactional(readOnly = true)
	public Story getById(Long storyId) {
		return findById(storyId);
	}

	public Story updateStatus(Long storyId, StoryStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("Story status is required");
		}
		Story story = findById(storyId);
		requireActive(story.getProject());
		if (story.getStatus() != status && !isAllowed(story.getStatus(), status)) {
			logger.warn("Rejected story transition id={} from={} to={}", storyId, story.getStatus(), status);
			throw new IllegalStateException("Story cannot move from " + story.getStatus() + " to " + status);
		}
		story.setStatus(status);
		Story saved = storyRepository.save(story);
		logger.info("Updated story id={} status={}", storyId, status);
		return saved;
	}

	public void delete(Long storyId) {
		Story story = findById(storyId);
		requireActive(story.getProject());
		storyRepository.delete(story);
		logger.info("Deleted story id={} projectId={}", storyId, story.getProject().getId());
	}

	private void validateNewStory(Story story) {
		if (story == null) {
			throw new IllegalArgumentException("Story is required");
		}
		if (story.getId() != null) {
			throw new IllegalArgumentException("New stories cannot have an ID");
		}
		if (story.getTitle() == null || story.getTitle().isBlank()) {
			throw new IllegalArgumentException("Story title is required");
		}
		if (story.getPriority() == null) {
			throw new IllegalArgumentException("Story priority is required");
		}
		if (story.getStoryPoints() != null && story.getStoryPoints() < 0) {
			throw new IllegalArgumentException("Story points cannot be negative");
		}
	}

	private Story findById(Long storyId) {
		if (storyId == null) {
			throw new IllegalArgumentException("Story ID is required");
		}
		return storyRepository.findById(storyId)
				.orElseThrow(() -> new NoSuchElementException("Story " + storyId + " was not found"));
	}

	private Project getProject(Long projectId) {
		if (projectId == null) {
			throw new IllegalArgumentException("Project ID is required");
		}
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new NoSuchElementException("Project " + projectId + " was not found"));
	}

	private void requireActive(Project project) {
		if (project.getStatus() != ProjectStatus.ACTIVE) {
			logger.warn("Rejected story mutation because project id={} status={}", project.getId(), project.getStatus());
			throw new IllegalStateException("Stories can be changed only while the project is active");
		}
	}

	private boolean isAllowed(StoryStatus current, StoryStatus next) {
		return current == StoryStatus.TO_DO
				&& (next == StoryStatus.IN_PROGRESS || next == StoryStatus.CANCELLED)
				|| current == StoryStatus.IN_PROGRESS
				&& (next == StoryStatus.DONE || next == StoryStatus.CANCELLED);
	}
}