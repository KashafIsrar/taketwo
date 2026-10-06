// repository/MessageRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);
    List<Message> findByConversation_IdAndCreatedAtAfterOrderByCreatedAtAsc(UUID conversationId, Instant after);
    Optional<Message> findTopByConversation_IdOrderByCreatedAtDesc(UUID conversationId);
}