package com.taketwo.backend.service;

import com.taketwo.backend.dto.LikeStatusResponse;
import com.taketwo.backend.entity.LogLike;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.LogLikeRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LogLikeRepository logLikeRepository;
    private final MovieLogRepository movieLogRepository;
    private final NotificationService notificationService; // Added for notifications

    @Transactional
    public LikeStatusResponse toggleLike(User currentUser, UUID movieLogId) {
        var movieLog = movieLogRepository.findById(movieLogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log entry not found"));

        var existing = logLikeRepository.findByUser_IdAndMovieLog_Id(currentUser.getId(), movieLogId);

        boolean nowLiked;
        if (existing.isPresent()) {
            logLikeRepository.delete(existing.get());
            nowLiked = false;
        } else {
            logLikeRepository.save(LogLike.builder().user(currentUser).movieLog(movieLog).build());
            nowLiked = true;
            notificationService.notifyLike(currentUser, movieLog); // Triggers notification when liked
        }

        return new LikeStatusResponse(nowLiked, logLikeRepository.countByMovieLog_Id(movieLogId));
    }

    public LikeStatusResponse getStatus(User currentUser, UUID movieLogId) {
        boolean liked = logLikeRepository.existsByUser_IdAndMovieLog_Id(currentUser.getId(), movieLogId);
        return new LikeStatusResponse(liked, logLikeRepository.countByMovieLog_Id(movieLogId));
    }
}