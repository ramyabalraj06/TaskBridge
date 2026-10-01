package com.taskbridge.TB.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskbridge.TB.models.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

	boolean existsByNameIgnoreCase(String name);
}