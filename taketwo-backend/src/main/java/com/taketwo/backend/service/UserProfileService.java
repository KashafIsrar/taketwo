package com.taketwo.backend.service;

import com.taketwo.backend.dto.UserProfileDto;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final MovieLogRepository movieLogRepository;
    private final FollowRepository followRepository;

    public UserProfileService(UserRepository userRepository, 
                              MovieLogRepository movieLogRepository, 
                              FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.movieLogRepository = movieLogRepository;
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID targetUserId, UUID currentUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalLogged = movieLogRepository.countByUser_Id(targetUserId);
        Double avgRating = movieLogRepository.findAverageRatingByUserId(targetUserId);
        
        double formattedAvg = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 0.0;

        long followers = followRepository.countByFollowee_Id(targetUserId);
        long following = followRepository.countByFollower_Id(targetUserId);

        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            isFollowing = followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, targetUserId);
        }

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                totalLogged,
                formattedAvg,
                followers,
                following,
                isFollowing
        );
    }
}