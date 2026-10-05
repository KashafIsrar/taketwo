// entity/ChallengeProgress.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "challenge_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_key"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Matches a key in the server-side ChallengeCatalog - not a DB foreign key,
    // since challenge definitions are app content, not user data.
    @Column(name = "challenge_key", nullable = false, length = 50)
    private String challengeKey;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    // Null while in progress. Set once, never cleared - completed challenges
    // stay completed and are never reassigned to the same user.
    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        this.assignedAt = Instant.now();
    }
}