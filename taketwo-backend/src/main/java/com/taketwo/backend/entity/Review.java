package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // One-to-one: a review always belongs to exactly one specific rewatch/log entry,
    // not to "the movie" in the abstract - this is what lets a user log the same
    // film multiple times without being forced to write (or overwrite) a review each time.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_log_id", nullable = false, unique = true)
    private MovieLog movieLog;

    @Column(name = "review_text", columnDefinition = "text", nullable = false)
    private String reviewText;

    @Column(name = "contains_spoilers", nullable = false)
    private boolean containsSpoilers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
