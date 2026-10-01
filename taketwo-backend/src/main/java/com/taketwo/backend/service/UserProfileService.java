package com.taketwo.backend.service;

import com.taketwo.backend.dto.MemberSearchResponse;
import com.taketwo.backend.dto.ProfileResponse;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final MovieLogRepository movieLogRepository;
    private final FollowRepository followRepository;

    public ProfileResponse getProfile(UUID userId, UUID currentUserId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    long totalLogged = movieLogRepository.countByUser_Id(userId);
    Double averageRating = totalLogged > 0 ? movieLogRepository.findAverageRatingByUserId(userId) : null;
    long followerCount = followRepository.countByFollowee_Id(userId);
    long followingCount = followRepository.countByFollower_Id(userId);

    Boolean isFollowing = (currentUserId != null && !currentUserId.equals(userId))
            ? followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, userId)
            : null;

    return new ProfileResponse(
            user.getId(), user.getUsername(), user.getDisplayName(), user.getBio(),
            user.getProfilePictureUrl(), totalLogged, averageRating, followerCount, followingCount,
            user.getFavoriteMovies(), isFollowing
    );
}

    public List<MemberSearchResponse> searchUsers(String query, UUID currentUserId) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        return userRepository
                .findTop20ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByUsernameAsc(
                        normalizedQuery, normalizedQuery)
                .stream()
                .map(user -> new MemberSearchResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        currentUserId != null
                                && !currentUserId.equals(user.getId())
                                && followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, user.getId())
                ))
                .toList();
    }

    public void updateFavoriteMovies(UUID userId, List<Map<String, Object>> favoriteMovies) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        user.setFavoriteMovies(favoriteMovies);
        userRepository.save(user);
    }

    public void updateProfile(UUID userId, String displayName, String bio, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }
        user.setBio(bio);
        user.setProfilePictureUrl(profilePictureUrl);

        userRepository.save(user);
    }
}