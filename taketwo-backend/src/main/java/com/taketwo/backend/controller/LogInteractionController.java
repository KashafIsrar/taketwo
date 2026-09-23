package com.taketwo.backend.controller;

import com.taketwo.backend.dto.CommentRequest;
import com.taketwo.backend.dto.CommentResponse;
import com.taketwo.backend.dto.LikeStatusResponse;
import com.taketwo.backend.service.CommentService;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies/logs/{logId}")
@RequiredArgsConstructor
public class LogInteractionController {

    private final LikeService likeService;
    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    @PostMapping("/like")
    public LikeStatusResponse toggleLike(@PathVariable UUID logId, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return likeService.toggleLike(user, logId);
    }

    // Authenticated - needs to know who's asking, same reason as watchlist/status and follow-status.
    @GetMapping("/like-status")
    public LikeStatusResponse likeStatus(@PathVariable UUID logId, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return likeService.getStatus(user, logId);
    }

    @GetMapping("/comments")
    public List<CommentResponse> getComments(@PathVariable UUID logId) {
        return commentService.getComments(logId);
    }

    @PostMapping("/comments")
    public CommentResponse addComment(
            @PathVariable UUID logId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return commentService.addComment(user, logId, request.commentText());
    }
}
