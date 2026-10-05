// entity/Person.java
package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "people", uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_person_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_person_id", nullable = false, unique = true)
    private Long tmdbPersonId;

    @Column(nullable = false)
    private String name;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @PrePersist
    void onCreate() {
        this.syncedAt = Instant.now();
    }
}