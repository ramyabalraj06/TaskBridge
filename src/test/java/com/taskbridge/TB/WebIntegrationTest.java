package com.taskbridge.TB;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.taskbridge.TB.repositories.TeamRepository;
import com.taskbridge.TB.models.Project;
import com.taskbridge.TB.models.ProjectStatus;
import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.services.ProjectService;
import com.taskbridge.TB.services.TeamService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.h2.console.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TeamRepository teamRepository;
	@Autowired
	private TeamService teamService;
	@Autowired
	private ProjectService projectService;

	@LocalServerPort
	private int port;

	@Test
	void anonymousUserCanRenderTeamPage() throws Exception {
		mockMvc.perform(get("/teams"))
				.andExpect(status().isOk())
				.andExpect(view().name("teams/list"));
	}

	@Test
	void anonymousUserCanRenderProjectAndStoryPages() throws Exception {
		Team team = teamService.create(new Team("View Team", null));
		Project project = projectService.create(new Project("View Project", null, null), team.getId());
		projectService.updateStatus(project.getId(), ProjectStatus.ACTIVE);

		mockMvc.perform(get("/teams/{teamId}/projects", team.getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("projects/list"));
		mockMvc.perform(get("/projects/{projectId}/stories", project.getId()))
				.andExpect(status().isOk())
				.andExpect(view().name("stories/board"));
	}

	@Test
	void anonymousMutationRedirectsToLogin() throws Exception {
		mockMvc.perform(post("/teams").with(csrf()).param("name", "Delivery"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void authenticatedMutationWithCsrfCreatesTeam() throws Exception {
		mockMvc.perform(post("/teams").with(user("tester")).with(csrf()).param("name", "Web Team"))
				.andExpect(status().is3xxRedirection());

		org.junit.jupiter.api.Assertions.assertTrue(teamRepository.existsByNameIgnoreCase("Web Team"));
	}

	@Test
	void corsPreflightAllowsExternalOrigin() throws Exception {
		mockMvc.perform(options("/teams")
				.header("Origin", "https://example.test")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://example.test"));
	}

	@Test
	void h2ConsoleIsReachableWithSameOriginFrames() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/h2-console/"))
				.GET()
				.build();
		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		org.junit.jupiter.api.Assertions.assertEquals(200, response.statusCode());
		org.junit.jupiter.api.Assertions.assertEquals("SAMEORIGIN",
				response.headers().firstValue("X-Frame-Options").orElse(null));
	}
}