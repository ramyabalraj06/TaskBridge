package com.taskbridge.TB.dto;

import java.time.LocalDateTime;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;

public record ProjectSnapshot(Long id, String name, String description, Long teamId,
		ProjectStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {

	public static ProjectSnapshot from(Project project) {
		return new ProjectSnapshot(project.getId(), project.getName(), project.getDescription(),
				project.getTeam().getId(), project.getStatus(), project.getCreatedAt(), project.getUpdatedAt());
	}
}