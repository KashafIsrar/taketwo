package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    void onCreate() {
        this.addedAt = Instant.now();
    }
}
