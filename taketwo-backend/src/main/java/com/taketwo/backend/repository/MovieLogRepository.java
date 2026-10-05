package com.taketwo.backend.repository;

import com.taketwo.backend.dto.GenreAffinity;
import com.taketwo.backend.dto.projection.KeywordAffinityProjection;
import com.taketwo.backend.dto.projection.MovieRatingProjection;
import com.taketwo.backend.dto.projection.MovieYearRating;
import com.taketwo.backend.entity.MovieLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;
import java.util.List;
import java.util.UUID;

public interface MovieLogRepository extends JpaRepository<MovieLog, UUID> {
    List<MovieLog> findByUser_IdOrderByWatchedDateDesc(UUID userId);

    long countByUser_Id(UUID userId);

    // Returns null (not 0.0) when the user has zero logs - handled explicitly
    // in ProfileService rather than silently showing "0.0" for someone who
    // hasn't logged anything yet.
    @Query("select avg(m.rating) from MovieLog m where m.user.id = :userId")
    Double findAverageRatingByUserId(@Param("userId") UUID userId);

    // Powers the activity feed: logs from any user in the given set (the
    // people the current user follows), most recent first, capped by the
    // page size passed in - avoids ever loading an unbounded feed.
    List<MovieLog> findByUser_IdInOrderByCreatedAtDesc(List<UUID> userIds, Pageable pageable);

    // Add to MovieLogRepository.java - all additive, existing methods untouched

// Real genre-based count, for badges like "Nightmare Fiend" (10 horror films)
@Query("""
        select count(distinct ml) from MovieLog ml
        join ml.movie m
        join m.genres g
        where ml.user.id = :userId and g.name = :genreName
        """)
long countByUserIdAndGenreName(@Param("userId") UUID userId, @Param("genreName") String genreName);

// Real decade-based count, for challenges like "1980s Nostalgia"
@Query("""
        select count(distinct ml) from MovieLog ml
        join ml.movie m
        where ml.user.id = :userId
        and m.releaseDate >= :startDate and m.releaseDate < :endDate
        """)
long countByUserIdAndReleaseDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
);

// Real streak calculation needs the actual watched dates, not just a count
@Query("select distinct ml.watchedDate from MovieLog ml where ml.user.id = :userId order by ml.watchedDate desc")
List<java.time.LocalDate> findDistinctWatchedDatesByUserId(@Param("userId") UUID userId);

// Add to MovieLogRepository.java

@Query("""
        select new com.taketwo.backend.dto.GenreAffinity(g.tmdbGenreId, g.name, count(distinct ml), avg(ml.rating))
        from MovieLog ml join ml.movie m join m.genres g
        where ml.user.id = :userId
        group by g.tmdbGenreId, g.name
        order by count(distinct ml) desc
        """)
List<GenreAffinity> findGenreAffinityByUserId(@Param("userId") UUID userId);

@Query("""
        select new com.taketwo.backend.dto.projection.MovieYearRating(m.releaseDate, ml.rating)
        from MovieLog ml join ml.movie m
        where ml.user.id = :userId and m.releaseDate is not null
        """)
List<MovieYearRating> findReleaseDateAndRatingByUserId(@Param("userId") UUID userId);

// Excludes already-logged movies from TAKE TWO candidate pools
@Query("select m.tmdbId from MovieLog ml join ml.movie m where ml.user.id = :userId")
Set<Long> findLoggedTmdbIdsByUserId(@Param("userId") UUID userId);

@Query("select new com.taketwo.backend.dto.projection.MovieRatingProjection(ml.movie.tmdbId, ml.rating) from MovieLog ml where ml.user.id = :userId")
List<MovieRatingProjection> findTmdbIdAndRatingByUserId(@Param("userId") UUID userId);

@Query("""
        select ml from MovieLog ml
        where ml.user.id = :userId and ml.rating >= :minRating
        order by ml.rating desc, ml.createdAt desc
        """)
List<MovieLog> findHighlyRatedLogsByUserId(@Param("userId") UUID userId, @Param("minRating") Double minRating, Pageable pageable);

// Add to MovieLogRepository.java

@Query("""
        select new com.taketwo.backend.dto.projection.KeywordAffinityProjection(k.name, count(distinct ml), avg(ml.rating))
        from MovieLog ml join ml.movie m join m.keywords k
        where ml.user.id = :userId
        group by k.name
        """)
List<KeywordAffinityProjection> findKeywordAffinityByUserId(@Param("userId") UUID userId);
}
