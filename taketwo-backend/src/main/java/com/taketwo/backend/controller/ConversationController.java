package com.taketwo.backend.controller;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.service.ConversationService;
import com.taketwo.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        var user = currentUserService.getCurrentUser(authentication);
        return conversationService.getOrCreateDm(user, targetUserId);
    }

    @GetMapping
    public List<ConversationSummaryResponse> getInbox(Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return conversationService.getInbox(user);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) Instant after,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return conversationService.getMessages(user, conversationId, after);
    }

    @PostMapping("/{conversationId}/messages")
    public MessageResponse sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return conversationService.sendMessage(user, conversationId, request.content());
    }

    @PostMapping("/{conversationId}/read")
    public void markRead(@PathVariable UUID conversationId, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        conversationService.markRead(user, conversationId);
    }
}