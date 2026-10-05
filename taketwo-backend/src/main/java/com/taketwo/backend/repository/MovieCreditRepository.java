// repository/MovieCreditRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.dto.DirectorAffinity;
import com.taketwo.backend.entity.MovieCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MovieCreditRepository extends JpaRepository<MovieCredit, UUID> {
    List<MovieCredit> findByMovie_Id(UUID movieId);
    long countByMovie_Id(UUID movieId);

    // Powers director-momentum detection: how many distinct films by this
    // director has the user logged within a given window (recencyFrom).
    @Query("""
            select count(distinct ml) from MovieLog ml
            join ml.movie m
            join MovieCredit mc on mc.movie = m
            where ml.user.id = :userId
            and mc.person.id = :directorId
            and mc.role = com.taketwo.backend.entity.MovieCredit.Role.DIRECTOR
            and ml.watchedDate >= :recencyFrom
            """)
    long countRecentLogsByDirector(
            @org.springframework.data.repository.query.Param("userId") UUID userId,
            @org.springframework.data.repository.query.Param("directorId") UUID directorId,
            @org.springframework.data.repository.query.Param("recencyFrom") java.time.LocalDate recencyFrom
    );

    // Add to MovieCreditRepository.java

@Query("""
        select new com.taketwo.backend.dto.DirectorAffinity(p.id, p.tmdbPersonId, p.name, count(distinct ml), avg(ml.rating))
        from MovieLog ml join ml.movie m join MovieCredit mc on mc.movie = m join mc.person p
        where ml.user.id = :userId and mc.role = com.taketwo.backend.entity.MovieCredit.Role.DIRECTOR
        group by p.id, p.tmdbPersonId, p.name
        order by count(distinct ml) desc
        """)
List<DirectorAffinity> findDirectorAffinityByUserId(@Param("userId") UUID userId);

@Query("""
        select new com.taketwo.backend.dto.DirectorAffinity(p.id, p.tmdbPersonId, p.name, count(distinct ml), avg(ml.rating))
        from MovieLog ml join ml.movie m join MovieCredit mc on mc.movie = m join mc.person p
        where ml.user.id = :userId and mc.role = com.taketwo.backend.entity.MovieCredit.Role.DIRECTOR
        and ml.watchedDate >= :recencyFrom
        group by p.id, p.tmdbPersonId, p.name
        having count(distinct ml) >= 2
        order by count(distinct ml) desc
        """)
List<DirectorAffinity> findDirectorMomentumByUserId(@Param("userId") UUID userId, @Param("recencyFrom") LocalDate recencyFrom);
}