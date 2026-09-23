package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "genres", uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_genre_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_genre_id", nullable = false, unique = true)
    private Integer tmdbGenreId;

    @Column(nullable = false)
    private String name;
}
