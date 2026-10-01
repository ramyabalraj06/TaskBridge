package com.taskbridge.TB.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskbridge.TB.models.AuditEventType;
import com.taskbridge.TB.models.Notification;
import com.taskbridge.TB.models.Team;
import com.taskbridge.TB.repositories.NotificationRepository;

@Service
@Transactional
public class NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public List<Notification> notifyTeam(Team team, String actorUserId, AuditEventType eventType,
			Long projectId, String message) {
		if (team == null || actorUserId == null || eventType == null || projectId == null
				|| message == null || message.isBlank()) {
			throw new IllegalArgumentException("Complete notification event data are required");
		}

		List<Notification> notifications = team.getMemberUserIds().stream()
				.map(String::trim)
				.filter(userId -> !userId.isBlank())
				.filter(userId -> !userId.equals(actorUserId))
				.distinct()
				.map(userId -> new Notification(userId, eventType, projectId, message))
				.toList();
		List<Notification> saved = notificationRepository.saveAll(notifications);
		logger.info("Created notifications event={} projectId={} recipientCount={}",
				eventType, projectId, saved.size());
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Notification> getUnreadForUser(String userId, String requesterUserId) {
		validateOwnership(userId, requesterUserId);
		return notificationRepository.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
	}

	public Notification markRead(Long notificationId, String requesterUserId) {
		if (notificationId == null) {
			throw new IllegalArgumentException("Notification ID is required");
		}
		Notification notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new NoSuchElementException(
						"Notification " + notificationId + " was not found"));
		validateOwnership(notification.getRecipientUserId(), requesterUserId);
		notification.markRead();
		Notification saved = notificationRepository.save(notification);
		logger.info("Marked notification id={} read for userId={}", notificationId, requesterUserId);
		return saved;
	}

	private void validateOwnership(String ownerUserId, String requesterUserId) {
		if (ownerUserId == null || ownerUserId.isBlank()
				|| requesterUserId == null || !ownerUserId.equals(requesterUserId)) {
			throw new AccessDeniedException("Notification access is restricted to its recipient");
		}
	}
}