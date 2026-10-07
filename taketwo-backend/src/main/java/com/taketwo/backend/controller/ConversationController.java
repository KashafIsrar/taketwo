package com.taketwo.backend.controller;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.service.ConversationService;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    @PostMapping("/dm/{targetUserId}")
    public ConversationSummaryResponse getOrCreateDm(@PathVariable UUID targetUserId, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.getOrCreateDm(user, targetUserId);
    }

    @GetMapping
    public List<ConversationSummaryResponse> getInbox(Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.getInbox(user);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) Instant after,
            Authentication authentication
    ) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.getMessages(user, conversationId, after);
    }

    @PostMapping("/{conversationId}/messages")
    public MessageResponse sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.sendMessage(user, conversationId, request.content());
    }

    @PostMapping("/{conversationId}/read")
    public void markRead(@PathVariable UUID conversationId, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        conversationService.markRead(user, conversationId);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationSummaryResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request, 
            Authentication authentication
    ) {
        User user = currentUserService.getCurrentUser(authentication);
        ConversationSummaryResponse response = conversationService.createGroup(
                user, 
                request.name(), 
                request.memberUserIds()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{conversationId}/invite/{targetUserId}")
    public void invite(@PathVariable UUID conversationId, @PathVariable UUID targetUserId, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        conversationService.inviteToGroup(user, conversationId, targetUserId);
    }

    @DeleteMapping("/{conversationId}/members/{targetUserId}")
    public void kick(@PathVariable UUID conversationId, @PathVariable UUID targetUserId, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        conversationService.kickFromGroup(user, conversationId, targetUserId);
    }

    @PutMapping("/{conversationId}/rename")
    public void rename(@PathVariable UUID conversationId, @Valid @RequestBody RenameGroupRequest request, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        conversationService.renameGroup(user, conversationId, request.name());
    }

    @GetMapping("/{conversationId}/participants")
    public List<ParticipantResponse> getParticipants(@PathVariable UUID conversationId, Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.getParticipants(user, conversationId);
    }

    @GetMapping("/mutual-followers")
    public List<FollowUserSummary> getMutualFollowers(Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return conversationService.getMutualFollowers(user);
    }
}