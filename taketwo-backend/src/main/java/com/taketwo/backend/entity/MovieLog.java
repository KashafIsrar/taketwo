package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "movie_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "watched_date", nullable = false)
    private LocalDate watchedDate;

    // 0.5 - 5.0 in half-star steps
    @Column(nullable = false)
    private Double rating;

    @Column(name = "is_rewatch", nullable = false)
    private boolean rewatch;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
