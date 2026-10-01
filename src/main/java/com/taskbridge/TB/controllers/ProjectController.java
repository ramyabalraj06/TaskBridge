package com.taskbridge.TB.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.services.ProjectService;
import com.taskbridge.TB.services.TeamService;

@Controller
public class ProjectController {

	private final ProjectService projectService;
	private final TeamService teamService;

	public ProjectController(ProjectService projectService, TeamService teamService) {
		this.projectService = projectService;
		this.teamService = teamService;
	}

	@GetMapping("/teams/{teamId}/projects")
	public String list(@PathVariable Long teamId, Model model) {
		model.addAttribute("team", teamService.getById(teamId));
		model.addAttribute("projects", projectService.getByTeam(teamId));
		model.addAttribute("project", new Project());
		model.addAttribute("statuses", ProjectStatus.values());
		return "projects/list";
	}

	@PostMapping("/teams/{teamId}/projects")
	public String create(@PathVariable Long teamId, @ModelAttribute Project project,
			RedirectAttributes redirectAttributes) {
		try {
			projectService.create(project, teamId);
			redirectAttributes.addFlashAttribute("message", "Project created");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToTeam(teamId);
	}

	@PostMapping("/projects/{projectId}/status")
	public String updateStatus(@PathVariable Long projectId, @RequestParam ProjectStatus status,
			RedirectAttributes redirectAttributes) {
		Project project = projectService.getById(projectId);
		try {
			projectService.updateStatus(projectId, status);
			redirectAttributes.addFlashAttribute("message", "Project status updated");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToTeam(project.getTeam().getId());
	}

	@PostMapping("/projects/{projectId}/delete")
	public String delete(@PathVariable Long projectId, RedirectAttributes redirectAttributes) {
		Project project = projectService.getById(projectId);
		Long teamId = project.getTeam().getId();
		try {
			projectService.delete(projectId);
			redirectAttributes.addFlashAttribute("message", "Project deleted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return redirectToTeam(teamId);
	}

	private String redirectToTeam(Long teamId) {
		return "redirect:/teams/" + teamId + "/projects";
	}
}