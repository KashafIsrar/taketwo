package com.taketwo.backend.service;

import com.taketwo.backend.dto.FollowStatusResponse;
import com.taketwo.backend.dto.FollowUserSummary;
import com.taketwo.backend.entity.Follow;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowStatusResponse toggleFollow(User currentUser, UUID targetUserId) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't follow yourself");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var existing = followRepository.findByFollower_IdAndFollowee_Id(currentUser.getId(), targetUserId);

        boolean nowFollowing;
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            nowFollowing = false;
        } else {
            Follow follow = Follow.builder().follower(currentUser).followee(target).build();
            followRepository.save(follow);
            nowFollowing = true;
        }

        return new FollowStatusResponse(nowFollowing, followRepository.countByFollowee_Id(targetUserId));
    }

    public FollowStatusResponse getStatus(User currentUser, UUID targetUserId) {
        boolean following = followRepository.existsByFollower_IdAndFollowee_Id(currentUser.getId(), targetUserId);
        return new FollowStatusResponse(following, followRepository.countByFollowee_Id(targetUserId));
    }

    public List<FollowUserSummary> getFollowers(UUID userId) {
        return followRepository.findByFollowee_IdOrderByCreatedAtDesc(userId).stream()
                .map(f -> toSummary(f.getFollower()))
                .toList();
    }

    public List<FollowUserSummary> getFollowing(UUID userId) {
        return followRepository.findByFollower_IdOrderByCreatedAtDesc(userId).stream()
                .map(f -> toSummary(f.getFollowee()))
                .toList();
    }

    private FollowUserSummary toSummary(User user) {
        return new FollowUserSummary(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
