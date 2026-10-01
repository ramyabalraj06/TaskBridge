TASKBRIDGE DOMAIN REPAIR REVIEW
===============================

BASELINE FINDINGS
-----------------
1. ProjectStatus mixes project lifecycle values with work-item states.
2. Project stores a scalar team ID, so database relationships and Team existence are not enforced.
3. Team and Story domain layers do not exist.
4. A Team can have multiple ACTIVE Projects.
5. Story lifecycle routines do not exist.
6. H2 console, MVC, CORS, and Spring Security are not configured.
7. Service milestones are not logged through parameterized SLF4J calls.
8. Tests cover only the original Project persistence slice.

APPROVED DECISIONS
------------------
1. Use Story as the work-item entity.
2. A Team can own many Projects, but only one can be ACTIVE.
3. Project states are PLANNING, ACTIVE, and ARCHIVED.
4. Story states are TO_DO, IN_PROGRESS, DONE, and CANCELLED.
5. Story transitions move forward; DONE and CANCELLED cannot reopen.
6. Stories use LOW, MEDIUM, HIGH, and CRITICAL priorities and optional non-negative story points.
7. Team deletion is blocked while Projects exist; Project deletion is blocked while Stories exist.
8. Anonymous users can read pages. Mutations require an in-memory Spring Security login and CSRF token.
9. H2 console and permissive CORS are sandbox features and are not production defaults.

ACCEPTANCE CRITERIA
-------------------
1. Team, Project, and Story are connected by required JPA relationships.
2. Activating a second Project for one Team fails without changing either Project.
3. Stories can be created and changed only while their Project is ACTIVE.
4. Story status transitions follow the approved lifecycle.
5. Parent deletes cannot silently remove or orphan child records.
6. Services use constructor injection, transaction boundaries, validation, and parameterized logging.
7. MVC controllers call services only and render Thymeleaf views.
8. Global sandbox CORS supports cross-origin MVC requests without credentials.
9. Anonymous mutations are rejected; authenticated mutations with CSRF are accepted.
10. Unit, H2 integration, MVC, security, CORS, and context tests pass.

SCOPE EXCLUSIONS
----------------
TeamMember persistence, individual Story assignees, epics, production PostgreSQL credentials
and migrations, and a custom login page are excluded.

POST-REVIEW CORRECTIONS
-----------------------
1. Split ProjectStatus into project-only values and introduced StoryStatus for work flow states.
2. Replaced Project.teamId with a required lazy Team relationship mapped through team_id.
3. Added Team and Story entities, repositories, services, timestamps, validation, and explicit columns.
4. Added the one-ACTIVE-Project-per-Team service invariant.
5. Added forward-only Story transitions and ACTIVE-Project checks for Story mutations.
6. Added child existence checks so Team and Project deletion cannot orphan or erase children.
7. Added parameterized SLF4J milestone and rejected-transition logging to all services.
8. Added Boot 4 MVC, Thymeleaf, Security, H2 console, MVC-test, and security-test modules.
9. Added local and test configuration profiles; H2 console is enabled only by the local profile
	or an explicit test override.
10. Added global CORS without credentials and a SecurityFilterChain that permits reads while
	 requiring authentication and CSRF for mutations.
11. Added Team, Project, and Story MVC controllers and Bootstrap/Thymeleaf views.
12. Replaced tests for the retired scalar-team API with Mockito, H2, MVC, security, CORS,
	 template, and real H2 console coverage.
13. Corrected initial web tests to use Boot 4's explicit mock-user request processor and a real
	 random-port request for the separately registered H2 servlet.

NOTIFICATION AND AUDIT EXTENSION
--------------------------------
1. Added Team-owned username assignments and authenticated assign/remove forms.
2. Added immutable Project audit entries with actor user ID, Team organisation ID, before/after
	JSON snapshots, event classification, scalar entity IDs, and occurrence timestamps.
3. Added persistent notifications with unread lookup, recipient ownership checks, and idempotent
	mark-as-read behavior.
4. Integrated Project create, real status update, and delete milestones with synchronous audit
	and notification writes in the existing transaction.
5. Added authenticated JSON endpoints for internal audit recording, filtered Project history,
	unread user notifications, and marking a notification read.
6. Protected audit and notification reads before the global anonymous GET rule while retaining
	CSRF protection for POST and PATCH.
7. Created spec.md as the feature specification and progress ledger; spec.m was not created.

VERIFICATION RESULTS
--------------------
Command: mvnw.cmd clean package
Result: BUILD SUCCESS
Tests: 33 run, 0 failures, 0 errors, 0 skipped
Package: target/TB-0.0.1-SNAPSHOT.jar
Editor diagnostics: no Java errors found

Notification and Audit extension verification:
- Production-code editor diagnostics reported no errors during implementation.
- Existing ProjectService test constructor wiring was updated for source compatibility.
- Maven, test, and packaging commands were not run for this extension by explicit user request.

Coverage includes:
- Team create/query/delete and child-delete protection.
- Project creation, Team mapping, status updates, active-project uniqueness, and delete protection.
- Story validation, active-parent rules, valid and invalid transitions, queries, and deletion.
- Team -> Project -> Story H2 persistence and timestamps.
- Anonymous Thymeleaf reads, authenticated CSRF-protected mutations, and login redirection.
- Team, Project, and Story template resolution.
- Permissive CORS preflight behavior.
- Real H2 console availability and SAMEORIGIN frame protection.

KNOWN SANDBOX LIMITATIONS
-------------------------
1. Spring Boot generates and logs a development-only in-memory password at startup. Production
	must replace this with persistent identity configuration.
2. JPA open-in-view remains enabled so lazy Team/Project relationships can be rendered by the
	server-side views. A DTO/view-model boundary should replace it if the read layer expands.
3. The one-active-project rule is enforced transactionally in the service layer. A production
	PostgreSQL deployment should add a database-level partial unique constraint through migrations.