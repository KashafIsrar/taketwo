// repository/ConversationRepository.java
package com.taketwo.backend.repository;

import com.taketwo.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Finds an existing DM between exactly these two users, if one exists -
    // intersection of "conversations userA is in" and "conversations userB
    // is in", restricted to DM type.
    @Query("""
            select c from Conversation c
            where c.type = com.taketwo.backend.entity.Conversation.Type.DM
            and c.id in (select cp.conversation.id from ConversationParticipant cp where cp.user.id = :userAId)
            and c.id in (select cp2.conversation.id from ConversationParticipant cp2 where cp2.user.id = :userBId)
            """)
    Optional<Conversation> findDmBetween(@Param("userAId") UUID userAId, @Param("userBId") UUID userBId);
}