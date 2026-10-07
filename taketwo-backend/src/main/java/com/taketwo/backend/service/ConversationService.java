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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

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

        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(currentUser)
                .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());
        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(target)
                .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());

        return conversation;
    }

    @Transactional
    public ConversationSummaryResponse createGroup(User creator, String name, List<UUID> memberUserIds) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }

        Conversation conversation = conversationRepository.save(
                Conversation.builder().type(Conversation.Type.GROUP).name(name).createdBy(creator).build());

        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(creator)
                .role(ConversationParticipant.Role.ADMIN).status(ConversationParticipant.Status.ACCEPTED).build());

        if (memberUserIds != null) {
            for (UUID memberId : new LinkedHashSet<>(memberUserIds)) {
                if (memberId.equals(creator.getId())) continue;
                User member = userRepository.findById(memberId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + memberId));
                requireMutualFollow(creator.getId(), memberId);
                participantRepository.save(ConversationParticipant.builder()
                        .conversation(conversation).user(member)
                        .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());
            }
        }

        return toSummary(conversation, creator.getId());
    }

    @Transactional
    public void inviteToGroup(User currentUser, UUID conversationId, UUID targetUserId) {
        ConversationParticipant requester = requireAdmin(currentUser.getId(), conversationId);
        Conversation conversation = requester.getConversation();
        if (conversation.getType() != Conversation.Type.GROUP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only invite into group conversations");
        }
        if (participantRepository.findByConversation_IdAndUser_Id(conversationId, targetUserId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already in this group");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        requireMutualFollow(currentUser.getId(), targetUserId);

        participantRepository.save(ConversationParticipant.builder()
                .conversation(conversation).user(target)
                .role(ConversationParticipant.Role.MEMBER).status(ConversationParticipant.Status.ACCEPTED).build());
    }

    @Transactional
    public void kickFromGroup(User currentUser, UUID conversationId, UUID targetUserId) {
        ConversationParticipant requester = requireAdmin(currentUser.getId(), conversationId);
        if (requester.getConversation().getType() != Conversation.Type.GROUP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only remove members from group conversations");
        }
        ConversationParticipant target = participantRepository.findByConversation_IdAndUser_Id(conversationId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "That user isn't in this group"));
        if (target.getRole() == ConversationParticipant.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins can't be removed");
        }
        participantRepository.delete(target);
    }

    @Transactional
    public void renameGroup(User currentUser, UUID conversationId, String newName) {
        ConversationParticipant requester = requireAdmin(currentUser.getId(), conversationId);
        Conversation conversation = requester.getConversation();
        if (conversation.getType() != Conversation.Type.GROUP) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only rename group conversations");
        }
        conversation.setName(newName);
        conversationRepository.save(conversation);
    }

    public List<ParticipantResponse> getParticipants(User currentUser, UUID conversationId) {
        requireParticipant(currentUser.getId(), conversationId);
        return participantRepository.findByConversation_Id(conversationId).stream()
                .map(p -> new ParticipantResponse(p.getUser().getId(), p.getUser().getUsername(), p.getUser().getDisplayName(), p.getRole().name()))
                .toList();
    }

    public List<FollowUserSummary> getMutualFollowers(User currentUser) {
        return followRepository.findMutualFollowers(currentUser.getId());
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

    private void requireMutualFollow(UUID requesterId, UUID targetId) {
        boolean requesterFollowsTarget = followRepository.existsByFollower_IdAndFollowee_Id(requesterId, targetId);
        boolean targetFollowsRequester = followRepository.existsByFollower_IdAndFollowee_Id(targetId, requesterId);

        if (!requesterFollowsTarget || !targetFollowsRequester) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only add mutual followers to a group chat.");
        }
    }

    private ConversationParticipant requireAdmin(UUID userId, UUID conversationId) {
        ConversationParticipant participant = requireParticipant(userId, conversationId);
        if (participant.getRole() != ConversationParticipant.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group admin can do this");
        }
        return participant;
    }

    private ConversationParticipant requireParticipant(UUID userId, UUID conversationId) {
        return participantRepository.findByConversation_IdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant in this conversation"));
    }

    private ConversationSummaryResponse toSummary(Conversation conversation, UUID currentUserId) {
        ConversationParticipant me = participantRepository
                .findByConversation_IdAndUser_Id(conversation.getId(), currentUserId)
                .orElseThrow();

        List<ConversationParticipant> allParticipants = participantRepository.findByConversation_Id(conversation.getId());

        FollowUserSummary otherUser = null;
        if (conversation.getType() == Conversation.Type.DM) {
            otherUser = allParticipants.stream()
                    .filter(p -> !p.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .map(p -> new FollowUserSummary(p.getUser().getId(), p.getUser().getUsername(), p.getUser().getDisplayName()))
                    .orElse(null);
        }

        var latest = messageRepository.findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId()).orElse(null);
        boolean unread = latest != null && (me.getLastReadAt() == null || latest.getCreatedAt().isAfter(me.getLastReadAt()));

        return new ConversationSummaryResponse(
                conversation.getId(),
                conversation.getType().name(),
                otherUser,
                conversation.getType() == Conversation.Type.GROUP ? conversation.getName() : null,
                allParticipants.size(),
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