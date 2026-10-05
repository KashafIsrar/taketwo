// repository/PersonRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByTmdbPersonId(Long tmdbPersonId);
}