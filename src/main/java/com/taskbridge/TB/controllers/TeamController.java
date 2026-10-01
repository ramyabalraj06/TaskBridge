package com.taskbridge.TB.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.services.TeamService;

@Controller
public class TeamController {

	private final TeamService teamService;

	public TeamController(TeamService teamService) {
		this.teamService = teamService;
	}

	@GetMapping({"/", "/teams"})
	public String list(Model model) {
		model.addAttribute("teams", teamService.getAll());
		model.addAttribute("team", new Team());
		return "teams/list";
	}

	@PostMapping("/teams")
	public String create(@ModelAttribute Team team, RedirectAttributes redirectAttributes) {
		try {
			teamService.create(team);
			redirectAttributes.addFlashAttribute("message", "Team created");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/teams";
	}

	@PostMapping("/teams/{teamId}/delete")
	public String delete(@PathVariable Long teamId, RedirectAttributes redirectAttributes) {
		try {
			teamService.delete(teamId);
			redirectAttributes.addFlashAttribute("message", "Team deleted");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/teams";
	}

	@PostMapping("/teams/{teamId}/members")
	public String assignUser(@PathVariable Long teamId, @RequestParam String userId,
			RedirectAttributes redirectAttributes) {
		try {
			teamService.assignUser(teamId, userId);
			redirectAttributes.addFlashAttribute("message", "Team member assigned");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/teams";
	}

	@PostMapping("/teams/{teamId}/members/remove")
	public String removeUser(@PathVariable Long teamId, @RequestParam String userId,
			RedirectAttributes redirectAttributes) {
		try {
			teamService.removeUser(teamId, userId);
			redirectAttributes.addFlashAttribute("message", "Team member removed");
		} catch (RuntimeException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/teams";
	}
}