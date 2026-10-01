package com.taskbridge.TB.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	List<Project> findByTeamId(Long teamId);

	boolean existsByTeamId(Long teamId);

	boolean existsByTeamIdAndStatusAndIdNot(Long teamId, ProjectStatus status, Long projectId);
}