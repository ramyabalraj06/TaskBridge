package com.taskbridge.TB.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taskbridge.TB.dto.NotificationResponse;
import com.taskbridge.TB.security.CurrentActorProvider;
import com.taskbridge.TB.services.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationApiController {

	private final NotificationService notificationService;
	private final CurrentActorProvider currentActorProvider;

	public NotificationApiController(NotificationService notificationService,
			CurrentActorProvider currentActorProvider) {
		this.notificationService = notificationService;
		this.currentActorProvider = currentActorProvider;
	}

	@GetMapping("/{userId}")
	@ResponseBody
	public List<NotificationResponse> unread(@PathVariable String userId) {
		return notificationService.getUnreadForUser(userId, currentActorProvider.getCurrentUserId()).stream()
				.map(NotificationResponse::from)
				.toList();
	}

	@PatchMapping("/{notificationId}/read")
	@ResponseBody
	public NotificationResponse markRead(@PathVariable Long notificationId) {
		return NotificationResponse.from(notificationService.markRead(
				notificationId, currentActorProvider.getCurrentUserId()));
	}
}