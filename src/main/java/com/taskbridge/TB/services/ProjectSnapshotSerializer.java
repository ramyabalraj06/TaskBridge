package com.taskbridge.TB.services;

import org.springframework.stereotype.Component;

import com.taskbridge.TB.dto.ProjectSnapshot;
import com.taskbridge.TB.models.Project;

import tools.jackson.databind.ObjectMapper;

@Component
public class ProjectSnapshotSerializer {

	private final ObjectMapper objectMapper;

	public ProjectSnapshotSerializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String serialize(Project project) {
		try {
			return objectMapper.writeValueAsString(ProjectSnapshot.from(project));
		} catch (Exception exception) {
			throw new IllegalStateException("Project snapshot could not be serialized", exception);
		}
	}
}