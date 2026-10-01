// entity/Notification.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public enum Type { LIKE, COMMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Who gets notified - the person who owns the movie log that got liked/commented on
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Who did the liking/commenting
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_log_id", nullable = false)
    private MovieLog movieLog;

    // Only set for COMMENT notifications - a short copy of the comment text,
    // denormalized on purpose so the notification still makes sense even if
    // the comment is later deleted.
    @Column(name = "comment_preview", length = 200)
    private String commentPreview;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}