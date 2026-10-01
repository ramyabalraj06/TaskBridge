# Notification & Audit Service Specification

## Status

| Phase | Progress |
| --- | --- |
| Specification and domain model | Completed |
| Team recipient assignments | Completed |
| Audit and notification services | Completed |
| Project milestone integration | Completed |
| JSON API and security | Completed |
| Verification | Not run by request |

## Baseline

The remediated Team, Project, and Story application previously completed a clean package with 33 passing tests. This build extends that baseline without changing the existing Project and Story lifecycle rules.

## Identity Decisions

- Team assignments are stored as a set of username strings; no User, TeamMember, or Organisation entity is introduced.
- The authenticated Spring Security username is the audit actor user ID.
- The owning Team ID is the audit actor organisation ID.
- Project notifications are created for every assigned Team username except the actor.
- Unauthenticated internal execution uses the actor ID `system`.

## Audit Model

An immutable audit entry stores:

- Project ID
- Event type: `PROJECT_CREATED`, `PROJECT_STATUS_UPDATED`, or `PROJECT_DELETED`
- Entity type and entity ID
- Actor user ID and actor organisation ID
- Previous Project snapshot JSON
- New Project snapshot JSON
- Occurrence timestamp

Audit entries have no update or delete service operation. They use immutable columns and remain available after Project deletion because Project references are stored as scalar IDs.

## Notification Model

A notification stores recipient user ID, event type, Project ID, message, unread/read state, created timestamp, and optional read timestamp. Only the read state may change after insertion.

## Milestone Rules

- Project creation writes an audit entry with no previous snapshot and creates notifications.
- A real status change writes before/after snapshots and creates notifications.
- An idempotent same-status request writes no audit entry or notification.
- Project deletion writes an audit entry with no new snapshot and creates notifications.
- Audit and notification writes execute synchronously in the Project transaction so any failure rolls back the milestone.

## API Contract

- `POST /audit` records an internal audit event for any authenticated caller.
- `GET /audit/{projectId}` returns newest-first history with optional `eventType`, `from`, and `to` filters.
- `GET /notifications/{userId}` returns unread notifications owned by the authenticated user.
- `PATCH /notifications/{id}/read` idempotently marks the authenticated user's notification read.

Date filters use ISO-8601 instants. POST and PATCH remain CSRF-protected because the application uses session authentication.

## Scope Boundaries

This build does not add email or push delivery, notification deletion, pagination, retention rules, a message broker, a persistent user domain, Team roles, or audit coverage for Team and Story mutations.

## Progress Notes

- Specification created as `spec.md`; the planned `spec.m` name was replaced by user request.
- Team recipient username persistence and management UI are implemented.
- Immutable audit persistence, filtered history, notifications, ownership checks, and read updates are implemented.
- Project create, status-change, and delete milestones now write audit and notification records transactionally.
- All four requested JSON endpoints and authenticated API rules are implemented.
- Existing ProjectService test wiring was updated for source compatibility, but tests were not run.
- Editor diagnostics reported no production-code errors during implementation.
- Maven commands and test execution are intentionally omitted by user request.