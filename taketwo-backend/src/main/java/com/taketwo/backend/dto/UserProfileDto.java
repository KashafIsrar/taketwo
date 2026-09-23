package com.taketwo.backend.dto;

import java.util.UUID;

public class UserProfileDto {
    private UUID userId;
    private String username;
    private String displayName;
    private long totalLoggedFilms;
    private Double averageRating;
    private long followersCount;
    private long followingCount;
    private boolean isFollowing;

    public UserProfileDto() {}

    public UserProfileDto(UUID userId, String username, String displayName, 
                          long totalLoggedFilms, Double averageRating, 
                          long followersCount, long followingCount, boolean isFollowing) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.totalLoggedFilms = totalLoggedFilms;
        this.averageRating = averageRating;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isFollowing = isFollowing;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public long getTotalLoggedFilms() { return totalLoggedFilms; }
    public void setTotalLoggedFilms(long totalLoggedFilms) { this.totalLoggedFilms = totalLoggedFilms; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public long getFollowersCount() { return followersCount; }
    public void setFollowersCount(long followersCount) { this.followersCount = followersCount; }

    public long getFollowingCount() { return followingCount; }
    public void setFollowingCount(long followingCount) { this.followingCount = followingCount; }

    public boolean isIsFollowing() { return isFollowing; }
    public void setIsFollowing(boolean following) { isFollowing = following; }
}