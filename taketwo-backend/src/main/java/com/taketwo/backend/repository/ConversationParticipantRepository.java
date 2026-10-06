// repository/ConversationParticipantRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, UUID> {
    Optional<ConversationParticipant> findByConversation_IdAndUser_Id(UUID conversationId, UUID userId);
    List<ConversationParticipant> findByConversation_Id(UUID conversationId);
    List<ConversationParticipant> findByUser_IdAndStatus(UUID userId, ConversationParticipant.Status status);
}