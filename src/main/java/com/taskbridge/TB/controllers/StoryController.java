package com.taskbridge.TB.controllers;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taskbridge.TB.models.Story;
import com.taskbridge.TB.models.StoryPriority;
import com.taskbridge.TB.models.StoryStatus;
import com.taskbridge.TB.services.ProjectService;
import com.taskbridge.TB.services.StoryService;

@Controller
public class StoryController {

	private final StoryService storyService;
	private final ProjectService projectService;

	public StoryController(StoryService storyService, ProjectService projectService) {
		this.storyService = storyService;
		this.projectService = projectService;
	}

	@GetMapping("/projects/{projectId}/stories")
	public String board(@PathVariable Long projectId, Model model) {
		Map<StoryStatus, java.util.List<Story>> storiesByStatus = storyService.getByProject(projectId).stream()
				.collect(Collectors.groupingBy(Story::getStatus));
		model.addAttribute("project", projectService.getById(projectId));
		model.addAttribute("story", new Story());
		model.addAttribute("priorities", StoryPriority.values());
		model.addAttribute("statuses", Arrays.asList(StoryStatus.values()));
		model.addAttribute("storiesByStatus", storiesByStatus);
		return "stories/board";
	}

	@PostMapping("/projects/{projectId}/stories")
	public String create(@PathVariable Long projectId, @ModelAttribute Story story,
			RedirectAttributes redirectAttributes) {
		try {
			storyService.create(story, projectId);
			redirectAttributes.addFlashAttribute("message", "Story created");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToBoard(projectId);
	}

	@PostMapping("/stories/{storyId}/status")
	public String updateStatus(@PathVariable Long storyId, @RequestParam StoryStatus status,
			RedirectAttributes redirectAttributes) {
		Story story = storyService.getById(storyId);
		try {
			storyService.updateStatus(storyId, status);
			redirectAttributes.addFlashAttribute("message", "Story status updated");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToBoard(story.getProject().getId());
	}

	@PostMapping("/stories/{storyId}/delete")
	public String delete(@PathVariable Long storyId, RedirectAttributes redirectAttributes) {
		Story story = storyService.getById(storyId);
		Long projectId = story.getProject().getId();
		try {
			storyService.delete(storyId);
			redirectAttributes.addFlashAttribute("message", "Story deleted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToBoard(projectId);
	}

	private String redirectToBoard(Long projectId) {
		return "redirect:/projects/" + projectId + "/stories";
	}
}