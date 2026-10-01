package com.taskbridge.TB.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.repositories.ProjectRepository;
import com.taskbridge.TB.repositories.TeamRepository;

@Service
@Transactional
public class TeamService {

	private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

	private final TeamRepository teamRepository;
	private final ProjectRepository projectRepository;

	public TeamService(TeamRepository teamRepository, ProjectRepository projectRepository) {
		this.teamRepository = teamRepository;
		this.projectRepository = projectRepository;
	}

	public Team create(Team team) {
		if (team == null || team.getName() == null || team.getName().isBlank()) {
			throw new IllegalArgumentException("Team name is required");
		}
		if (team.getId() != null) {
			throw new IllegalArgumentException("New teams cannot have an ID");
		}

		String name = team.getName().trim();
		if (teamRepository.existsByNameIgnoreCase(name)) {
			throw new IllegalStateException("A team named " + name + " already exists");
		}

		team.setName(name);
		Team saved = teamRepository.save(team);
		logger.info("Created team id={} name={}", saved.getId(), saved.getName());
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Team> getAll() {
		return teamRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Team getById(Long teamId) {
		if (teamId == null) {
			throw new IllegalArgumentException("Team ID is required");
		}
		return teamRepository.findById(teamId)
				.orElseThrow(() -> new NoSuchElementException("Team " + teamId + " was not found"));
	}

	public void delete(Long teamId) {
		Team team = getById(teamId);
		if (projectRepository.existsByTeamId(teamId)) {
			logger.warn("Rejected deletion of team id={} because projects exist", teamId);
			throw new IllegalStateException("Team cannot be deleted while projects exist");
		}
		teamRepository.delete(team);
		logger.info("Deleted team id={}", teamId);
	}

	public Team assignUser(Long teamId, String userId) {
		Team team = getById(teamId);
		String normalizedUserId = normalizeUserId(userId);
		if (team.addMemberUserId(normalizedUserId)) {
			teamRepository.save(team);
			logger.info("Assigned userId={} to team id={}", normalizedUserId, teamId);
		}
		return team;
	}

	public Team removeUser(Long teamId, String userId) {
		Team team = getById(teamId);
		String normalizedUserId = normalizeUserId(userId);
		if (team.removeMemberUserId(normalizedUserId)) {
			teamRepository.save(team);
			logger.info("Removed userId={} from team id={}", normalizedUserId, teamId);
		}
		return team;
	}

	private String normalizeUserId(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("User ID is required");
		}
		return userId.trim();
	}
}