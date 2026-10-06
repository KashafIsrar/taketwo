// entity/ConversationParticipant.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipant {

    public enum Role { ADMIN, MEMBER }
    public enum Status { ACCEPTED, PENDING }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // MEMBER for everyone in step 1 (DM has no admin concept) - meaningful once Group Chats land.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    // Always ACCEPTED in step 1 - PENDING is step 2's follower-gating.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        this.joinedAt = Instant.now();
    }
}