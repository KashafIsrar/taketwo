// service/NotificationService.java
package com.taketwo.backend.service;

import com.taketwo.backend.dto.NotificationResponse;
import com.taketwo.backend.entity.MovieLog;
import com.taketwo.backend.entity.Notification;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_LIMIT = 30;

    private final NotificationRepository notificationRepository;
    private final MovieMapper movieMapper;

    @Transactional
    public void notifyLike(User actor, MovieLog log) {
        // Never notify yourself for liking your own log.
        if (actor.getId().equals(log.getUser().getId())) return;

        notificationRepository.save(Notification.builder()
                .recipient(log.getUser())
                .actor(actor)
                .type(Notification.Type.LIKE)
                .movieLog(log)
                .build());
    }

    @Transactional
    public void notifyComment(User actor, MovieLog log, String commentText) {
        if (actor.getId().equals(log.getUser().getId())) return;

        String preview = commentText.length() > 200 ? commentText.substring(0, 200) : commentText;

        notificationRepository.save(Notification.builder()
                .recipient(log.getUser())
                .actor(actor)
                .type(Notification.Type.COMMENT)
                .movieLog(log)
                .commentPreview(preview)
                .build());
    }

    public List<NotificationResponse> getNotifications(UUID recipientId, Integer limit) {
        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_LIMIT : Math.min(limit, 100);
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, effectiveLimit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipient_IdAndIsReadFalse(recipientId);
    }

    @Transactional
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsRead(recipientId);
    }

    private NotificationResponse toResponse(Notification n) {
        User actor = n.getActor();
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                new com.taketwo.backend.dto.FollowUserSummary(actor.getId(), actor.getUsername(), actor.getDisplayName()),
                movieMapper.toSummary(n.getMovieLog().getMovie()),
                n.getCommentPreview(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}