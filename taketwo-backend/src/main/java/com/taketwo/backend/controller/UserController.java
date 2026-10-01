package com.taketwo.backend.controller;

import com.taketwo.backend.dto.MemberSearchResponse;
import com.taketwo.backend.dto.ProfileResponse;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/search")
    public List<MemberSearchResponse> searchUsers(
            @RequestParam(name = "q", required = false) String query,
            Authentication authentication) {
        User currentUser = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                ? currentUserService.getCurrentUser(authentication)
                : null;
        return userProfileService.searchUsers(query, currentUser != null ? currentUser.getId() : null);
    }

    @GetMapping("/{userId}/profile")
    public ProfileResponse getProfile(@PathVariable UUID userId, Authentication authentication) {
        User currentUser = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                ? currentUserService.getCurrentUser(authentication)
                : null;
        
        return userProfileService.getProfile(userId, currentUser != null ? currentUser.getId() : null);
    }

    @PutMapping("/favorites")
    public ResponseEntity<?> updateFavorites(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        User currentUser = null;
        try {
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                currentUser = currentUserService.getCurrentUser(authentication);
            }
        } catch (Exception e) {
            // Fallback handled safely
        }

        if (currentUser == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in again.");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> favoriteMovies = (List<Map<String, Object>>) request.get("favoriteMovies");
        
        userProfileService.updateFavoriteMovies(currentUser.getId(), favoriteMovies);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        User currentUser = null;
        try {
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                currentUser = currentUserService.getCurrentUser(authentication);
            }
        } catch (Exception e) {
            // Fallback handled safely
        }

        if (currentUser == null) {
            return ResponseEntity.status(401).body("Unauthorized: Please log in again.");
        }

        String displayName = request.get("displayName");
        String bio = request.get("bio");
        String profilePictureUrl = request.get("profilePictureUrl");

        userProfileService.updateProfile(currentUser.getId(), displayName, bio, profilePictureUrl);
        return ResponseEntity.ok().build();
    }
}