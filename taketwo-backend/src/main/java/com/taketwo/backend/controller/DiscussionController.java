package com.taketwo.backend.controller;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;
    private final CurrentUserService currentUserService;

    private User resolveOptionalUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return currentUserService.getCurrentUser(authentication);
        }
        return null;
    }

    @GetMapping("/movies/{tmdbId}/posts")
    public List<DiscussionPostResponse> getPosts(
            @PathVariable Long tmdbId,
            @RequestParam(required = false, defaultValue = "new") String sort,
            Authentication authentication
    ) {
        User user = resolveOptionalUser(authentication);
        return discussionService.getPosts(tmdbId, sort, user != null ? user.getId() : null);
    }

    @PostMapping("/movies/{tmdbId}/posts")
    public DiscussionPostResponse createPost(
            @PathVariable Long tmdbId,
            @Valid @RequestBody DiscussionPostRequest request,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return discussionService.createPost(user, tmdbId, request);
    }

    @GetMapping("/posts/{postId}")
    public DiscussionPostResponse getPost(@PathVariable UUID postId, Authentication authentication) {
        User user = resolveOptionalUser(authentication);
        return discussionService.getPost(postId, user != null ? user.getId() : null);
    }

    @PostMapping("/posts/{postId}/vote")
    public VoteResponse votePost(@PathVariable UUID postId, @Valid @RequestBody VoteRequest request, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return discussionService.votePost(user, postId, request.voteType());
    }

    @GetMapping("/posts/{postId}/comments")
    public List<DiscussionCommentResponse> getTopLevelComments(@PathVariable UUID postId, Authentication authentication) {
        User user = resolveOptionalUser(authentication);
        return discussionService.getTopLevelComments(postId, user != null ? user.getId() : null);
    }

    @PostMapping("/posts/{postId}/comments")
    public DiscussionCommentResponse addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody DiscussionCommentRequest request,
            Authentication authentication
    ) {
        var user = currentUserService.getCurrentUser(authentication);
        return discussionService.addComment(user, postId, request);
    }

    @GetMapping("/comments/{commentId}/replies")
    public List<DiscussionCommentResponse> getReplies(
            @PathVariable UUID commentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        User user = resolveOptionalUser(authentication);
        return discussionService.getReplies(commentId, page, limit, user != null ? user.getId() : null);
    }

    @PostMapping("/comments/{commentId}/vote")
    public VoteResponse voteComment(@PathVariable UUID commentId, @Valid @RequestBody VoteRequest request, Authentication authentication) {
        var user = currentUserService.getCurrentUser(authentication);
        return discussionService.voteComment(user, commentId, request.voteType());
    }
}