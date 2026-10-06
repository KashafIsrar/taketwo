package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.entity.*;
import com.taketwo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConversationSummaryResponse getOrCreateDm(User currentUser, UUID targetUserId) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't message yourself");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation conversation = conversationRepository.findDmBetween(currentUser.getId(), targetUserId)
                .orElseGet(() -> createDm(currentUser, target));

        return toSummary(conversation, currentUser.getId());
    }

    private Conversation createDm(User currentUser, User target) {
        Conversation conversation = conversationRepository.save(
                Conversation.builder().type(Conversation.Type.DM).createdBy(currentUser).build());

        // Step 1: both participants start ACCEPTED - the PENDING/follower-gating
        // logic is step 2, not built yet.
        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(currentUser)
                .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());
        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(target)
                .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());

        return conversation;
    }

    public List<ConversationSummaryResponse> getInbox(User currentUser) {
        return participantRepository.findByUser_IdAndStatus(currentUser.getId(), ConversationParticipant.Status.ACCEPTED)
                .stream()
                .map(cp -> toSummary(cp.getConversation(), currentUser.getId()))
                .sorted((a, b) -> {
                    if (a.lastMessageAt() == null) return 1;
                    if (b.lastMessageAt() == null) return -1;
                    return b.lastMessageAt().compareTo(a.lastMessageAt());
                })
                .toList();
    }

    public List<MessageResponse> getMessages(User currentUser, UUID conversationId, Instant after) {
        requireParticipant(currentUser.getId(), conversationId);

        List<Message> messages = after == null
                ? messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId)
                : messageRepository.findByConversation_IdAndCreatedAtAfterOrderByCreatedAtAsc(conversationId, after);

        return messages.stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public MessageResponse sendMessage(User currentUser, UUID conversationId, String content) {
        ConversationParticipant participant = requireParticipant(currentUser.getId(), conversationId);

        Message message = messageRepository.save(Message.builder()
                .conversation(participant.getConversation()).sender(currentUser).content(content).build());

        // Sending a message implies you've seen everything up to now -
        // marks the sender's own lastReadAt, so you don't see your own
        // conversation as unread immediately after sending into it.
        participant.setLastReadAt(message.getCreatedAt());
        participantRepository.save(participant);

        return toMessageResponse(message);
    }

    @Transactional
    public void markRead(User currentUser, UUID conversationId) {
        ConversationParticipant participant = requireParticipant(currentUser.getId(), conversationId);
        participant.setLastReadAt(Instant.now());
        participantRepository.save(participant);
    }

    private ConversationParticipant requireParticipant(UUID userId, UUID conversationId) {
        return participantRepository.findByConversation_IdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant in this conversation"));
    }

    private ConversationSummaryResponse toSummary(Conversation conversation, UUID currentUserId) {
        ConversationParticipant me = participantRepository
                .findByConversation_IdAndUser_Id(conversation.getId(), currentUserId)
                .orElseThrow();

        // Step 1 is DM-only (exactly 2 participants) - "the other user" is
        // well-defined. Group Chats (step 3) will need a different summary shape.
        FollowUserSummary otherUser = participantRepository.findByConversation_Id(conversation.getId()).stream()
                .filter(p -> !p.getUser().getId().equals(currentUserId))
                .findFirst()
                .map(p -> new FollowUserSummary(p.getUser().getId(), p.getUser().getUsername(), p.getUser().getDisplayName()))
                .orElse(null);

        var latest = messageRepository.findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        boolean unread = latest != null && (me.getLastReadAt() == null || latest.getCreatedAt().isAfter(me.getLastReadAt()));

        return new ConversationSummaryResponse(
                conversation.getId(), otherUser,
                latest != null ? latest.getContent() : null,
                latest != null ? latest.getCreatedAt() : null,
                unread
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        User sender = message.getSender();
        return new MessageResponse(
                message.getId(), message.getConversation().getId(),
                new FollowUserSummary(sender.getId(), sender.getUsername(), sender.getDisplayName()),
                message.getContent(), message.getCreatedAt()
        );
    }
}