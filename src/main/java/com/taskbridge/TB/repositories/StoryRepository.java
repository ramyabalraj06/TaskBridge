package com.taskbridge.TB.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskbridge.TB.models.Story;

public interface StoryRepository extends JpaRepository<Story, Long> {

	List<Story> findByProjectId(Long projectId);

	boolean existsByProjectId(Long projectId);
}