package com.taskbridge.TB.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	@Mock
	private TeamRepository teamRepository;
	@Mock
	private ProjectRepository projectRepository;

	private TeamService teamService;

	@BeforeEach
	void setUp() {
		teamService = new TeamService(teamRepository, projectRepository);
	}

	@Test
	void createNormalizesAndSavesTeam() {
		Team team = new Team("  Delivery  ", null);
		when(teamRepository.save(team)).thenReturn(team);

		assertSame(team, teamService.create(team));
		assertEquals("Delivery", team.getName());
	}

	@Test
	void createRejectsInvalidAndDuplicateNames() {
		when(teamRepository.existsByNameIgnoreCase("Delivery")).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> teamService.create(null));
		assertThrows(IllegalArgumentException.class, () -> teamService.create(new Team(" ", null)));
		assertThrows(IllegalStateException.class, () -> teamService.create(new Team("Delivery", null)));
	}

	@Test
	void getAllAndGetByIdDelegateToRepository() {
		Team team = team(1L);
		List<Team> teams = List.of(team);
		when(teamRepository.findAll()).thenReturn(teams);
		when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

		assertSame(teams, teamService.getAll());
		assertSame(team, teamService.getById(1L));
		assertThrows(IllegalArgumentException.class, () -> teamService.getById(null));
		assertThrows(NoSuchElementException.class, () -> teamService.getById(99L));
	}

	@Test
	void deleteBlocksTeamsWithProjects() {
		Team team = team(1L);
		when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
		when(projectRepository.existsByTeamId(1L)).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> teamService.delete(1L));
		verify(teamRepository, never()).delete(team);
	}

	@Test
	void deleteRemovesEmptyTeam() {
		Team team = team(1L);
		when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

		teamService.delete(1L);

		verify(teamRepository).delete(team);
	}

	private Team team(Long id) {
		Team team = new Team("Delivery", null);
		team.setId(id);
		return team;
	}
}