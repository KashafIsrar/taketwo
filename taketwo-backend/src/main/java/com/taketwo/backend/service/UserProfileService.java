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

import java.util.ArrayList;
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

        long totalLogged = 0;
        Double averageRating = null;
        try {
            totalLogged = movieLogRepository.countByUser_Id(userId);
            if (totalLogged > 0) {
                averageRating = movieLogRepository.findAverageRatingByUserId(userId);
            }
        } catch (Exception e) {
            // Fallback gracefully if movie logs query fails
        }

        long followerCount = 0;
        long followingCount = 0;
        try {
            followerCount = followRepository.countByFollowee_Id(userId);
            followingCount = followRepository.countByFollower_Id(userId);
        } catch (Exception e) {
            // Fallback gracefully if follow counts fail
        }

        Boolean isFollowing = false;
        try {
            isFollowing = (currentUserId != null && !currentUserId.equals(userId))
                    ? followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, userId)
                    : null;
        } catch (Exception e) {
            isFollowing = false;
        }

        return new ProfileResponse(
                user.getId(), 
                user.getUsername(), 
                user.getDisplayName(), 
                user.getBio(),
                user.getProfilePictureUrl(), 
                totalLogged, 
                averageRating, 
                followerCount, 
                followingCount,
                user.getFavoriteMovies() != null ? user.getFavoriteMovies() : List.of(), 
                isFollowing
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

    public void blockUser(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself.");
        }
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getBlockedUsers() == null) {
            currentUser.setBlockedUsers(new ArrayList<>());
        }

        String targetIdStr = targetUserId.toString();
        if (!currentUser.getBlockedUsers().contains(targetIdStr)) {
            currentUser.getBlockedUsers().add(targetIdStr);
            userRepository.save(currentUser);
        }
    }

    public void unblockUser(UUID currentUserId, UUID targetUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getBlockedUsers() != null) {
            currentUser.getBlockedUsers().remove(targetUserId.toString());
            userRepository.save(currentUser);
        }
    }

    public List<MemberSearchResponse> getBlockedUsers(UUID currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (currentUser.getBlockedUsers() == null || currentUser.getBlockedUsers().isEmpty()) {
            return List.of();
        }

        List<UUID> blockedUuids = currentUser.getBlockedUsers().stream()
                .map(UUID::fromString)
                .toList();

        List<User> blockedUsersList = userRepository.findAllById(blockedUuids);

        return blockedUsersList.stream()
                .map(user -> new MemberSearchResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, user.getId())
                ))
                .toList();
    }
}