package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_log_id", nullable = false)
    private MovieLog movieLog;

    @Column(name = "comment_text", columnDefinition = "text", nullable = false)
    private String commentText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
