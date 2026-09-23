package com.taketwo.backend.service;

import com.taketwo.backend.dto.ProfileResponse;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final MovieLogRepository movieLogRepository;
    private final FollowRepository followRepository;

    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long totalLogged = movieLogRepository.countByUser_Id(userId);

        // findAverageRatingByUserId returns null (not 0.0) when totalLogged is 0 -
        // surface that honestly as null rather than showing a fake "0.0" average.
        Double averageRating = totalLogged > 0
                ? movieLogRepository.findAverageRatingByUserId(userId)
                : null;

        long followerCount = followRepository.countByFollowee_Id(userId);
        long followingCount = followRepository.countByFollower_Id(userId);

        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                totalLogged,
                averageRating,
                followerCount,
                followingCount
        );
    }
}
