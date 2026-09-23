package com.taketwo.backend.service;

import com.taketwo.backend.dto.CommentResponse;
import com.taketwo.backend.dto.FollowUserSummary;
import com.taketwo.backend.entity.LogComment;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.LogCommentRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final LogCommentRepository logCommentRepository;
    private final MovieLogRepository movieLogRepository;

    @Transactional
    public CommentResponse addComment(User currentUser, UUID movieLogId, String commentText) {
        var movieLog = movieLogRepository.findById(movieLogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log entry not found"));

        LogComment comment = logCommentRepository.save(
                LogComment.builder().user(currentUser).movieLog(movieLog).commentText(commentText).build()
        );

        return toResponse(comment);
    }

    public List<CommentResponse> getComments(UUID movieLogId) {
        return logCommentRepository.findByMovieLog_IdOrderByCreatedAtAsc(movieLogId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(LogComment comment) {
        User user = comment.getUser();
        return new CommentResponse(
                comment.getId(),
                new FollowUserSummary(user.getId(), user.getUsername(), user.getDisplayName()),
                comment.getCommentText(),
                comment.getCreatedAt()
        );
    }
}
