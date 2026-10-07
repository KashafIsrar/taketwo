package com.taketwo.backend.service;

import com.taketwo.backend.dto.*;
import com.taketwo.backend.entity.*;
import com.taketwo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private static final int DEFAULT_REPLY_BATCH_SIZE = 5;

    private final DiscussionPostRepository postRepository;
    private final DiscussionCommentRepository commentRepository;
    private final DiscussionPostVoteRepository postVoteRepository;
    private final DiscussionCommentVoteRepository commentVoteRepository;
    private final MovieCacheService movieCacheService;
    private final MovieMapper movieMapper;

    @Transactional
    public DiscussionPostResponse createPost(User user, Long tmdbId, DiscussionPostRequest request) {
        Movie movie = movieCacheService.findOrCacheMovie(tmdbId);

        DiscussionPost post = postRepository.save(DiscussionPost.builder()
                .movie(movie).user(user).title(request.title()).body(request.body())
                .isSpoiler(request.isSpoiler()).build());

        return toPostResponse(post, user.getId());
    }

    public List<DiscussionPostResponse> getPosts(Long tmdbId, String sort, UUID currentUserId) {
        Movie movie = movieCacheService.findOrCacheMovie(tmdbId);

        List<DiscussionPost> posts = postRepository.findByMovie_IdOrderByCreatedAtDesc(movie.getId(), PageRequest.of(0, 50));

        List<DiscussionPostResponse> responses = posts.stream()
                .map(p -> toPostResponse(p, currentUserId))
                .collect(Collectors.toCollection(ArrayList::new));

        if ("top".equals(sort)) {
            responses.sort(Comparator.comparingLong(DiscussionPostResponse::score).reversed());
        }
        return responses;
    }

    public DiscussionPostResponse getPost(UUID postId, UUID currentUserId) {
        return toPostResponse(requirePost(postId), currentUserId);
    }

    @Transactional
    public VoteResponse votePost(User user, UUID postId, VoteRequest.VoteType voteType) {
        requirePost(postId);
        var existing = postVoteRepository.findByPost_IdAndUser_Id(postId, user.getId());
        DiscussionPostVote.VoteType requestedType = DiscussionPostVote.VoteType.valueOf(voteType.name());
        String myVote;

        if (existing.isPresent() && existing.get().getVoteType() == requestedType) {
            postVoteRepository.delete(existing.get());
            myVote = null;
        } else if (existing.isPresent()) {
            existing.get().setVoteType(requestedType);
            postVoteRepository.save(existing.get());
            myVote = requestedType.name();
        } else {
            postVoteRepository.save(DiscussionPostVote.builder().post(requirePost(postId)).user(user).voteType(requestedType).build());
            myVote = requestedType.name();
        }

        return new VoteResponse(postVoteRepository.computeScore(postId), myVote);
    }

    public List<DiscussionCommentResponse> getTopLevelComments(UUID postId, UUID currentUserId) {
        requirePost(postId);
        return commentRepository.findByPost_IdAndParentComment_IdIsNullOrderByCreatedAtAsc(postId).stream()
                .map(c -> toCommentResponse(c, currentUserId))
                .toList();
    }

    public List<DiscussionCommentResponse> getReplies(UUID parentCommentId, Integer page, Integer limit, UUID currentUserId) {
        DiscussionComment parent = requireComment(parentCommentId);
        if (parent.getParentComment() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Replies can't themselves have replies");
        }
        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_REPLY_BATCH_SIZE : Math.min(limit, 50);
        int effectivePage = (page == null || page < 0) ? 0 : page;
        Pageable pageable = PageRequest.of(effectivePage, effectiveLimit);

        return commentRepository.findByParentComment_IdOrderByCreatedAtAsc(parentCommentId, pageable).stream()
                .map(c -> toCommentResponse(c, currentUserId))
                .toList();
    }

    @Transactional
    public DiscussionCommentResponse addComment(User user, UUID postId, DiscussionCommentRequest request) {
        DiscussionPost post = requirePost(postId);

        DiscussionComment parent = null;
        if (request.parentCommentId() != null) {
            parent = requireComment(request.parentCommentId());
            if (!parent.getPost().getId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment belongs to a different post");
            }
            if (parent.getParentComment() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't reply to a reply - reply to the top-level comment instead");
            }
        }

        DiscussionComment comment = commentRepository.save(DiscussionComment.builder()
                .post(post).parentComment(parent).user(user).body(request.body()).isSpoiler(request.isSpoiler()).build());

        return toCommentResponse(comment, user.getId());
    }

    @Transactional
    public VoteResponse voteComment(User user, UUID commentId, VoteRequest.VoteType voteType) {
        requireComment(commentId);
        var existing = commentVoteRepository.findByComment_IdAndUser_Id(commentId, user.getId());
        DiscussionCommentVote.VoteType requestedType = DiscussionCommentVote.VoteType.valueOf(voteType.name());
        String myVote;

        if (existing.isPresent() && existing.get().getVoteType() == requestedType) {
            commentVoteRepository.delete(existing.get());
            myVote = null;
        } else if (existing.isPresent()) {
            existing.get().setVoteType(requestedType);
            commentVoteRepository.save(existing.get());
            myVote = requestedType.name();
        } else {
            commentVoteRepository.save(DiscussionCommentVote.builder().comment(requireComment(commentId)).user(user).voteType(requestedType).build());
            myVote = requestedType.name();
        }

        return new VoteResponse(commentVoteRepository.computeScore(commentId), myVote);
    }

    private DiscussionPost requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discussion post not found"));
    }

    private DiscussionComment requireComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    private DiscussionPostResponse toPostResponse(DiscussionPost post, UUID currentUserId) {
        long score = postVoteRepository.computeScore(post.getId());
        String myVote = currentUserId == null ? null
                : postVoteRepository.findByPost_IdAndUser_Id(post.getId(), currentUserId).map(v -> v.getVoteType().name()).orElse(null);
        long commentCount = commentRepository.countByPost_Id(post.getId());

        User u = post.getUser();
        return new DiscussionPostResponse(
                post.getId(), movieMapper.toSummary(post.getMovie()),
                new FollowUserSummary(u.getId(), u.getUsername(), u.getDisplayName()),
                post.getTitle(), post.getBody(), post.isSpoiler(), score, myVote, commentCount, post.getCreatedAt()
        );
    }

    private DiscussionCommentResponse toCommentResponse(DiscussionComment comment, UUID currentUserId) {
        long score = commentVoteRepository.computeScore(comment.getId());
        String myVote = currentUserId == null ? null
                : commentVoteRepository.findByComment_IdAndUser_Id(comment.getId(), currentUserId).map(v -> v.getVoteType().name()).orElse(null);
        long replyCount = comment.getParentComment() == null ? commentRepository.countByParentComment_Id(comment.getId()) : 0;

        User u = comment.getUser();
        return new DiscussionCommentResponse(
                comment.getId(), comment.getPost().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                new FollowUserSummary(u.getId(), u.getUsername(), u.getDisplayName()),
                comment.getBody(), comment.isSpoiler(), score, myVote, replyCount, comment.getCreatedAt()
        );
    }
}