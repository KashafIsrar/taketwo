package com.taketwo.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "movies", uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    // TMDB's community score (0-10) - powers TAKE TWO's "popular vs hidden gem" filter
    @Column(name = "vote_average")
    private Double voteAverage;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;
}
