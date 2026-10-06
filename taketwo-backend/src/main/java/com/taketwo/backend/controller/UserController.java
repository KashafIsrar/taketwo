package com.taketwo.backend.controller;

import com.taketwo.backend.dto.MemberSearchResponse;
import com.taketwo.backend.dto.ProfileResponse;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.service.CurrentUserService;
import com.taketwo.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // Added {"/{id}", "/{id}/profile"} to support both endpoint forms
    @GetMapping({ "/{id}", "/{id}/profile" })
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = (authentication != null && authentication.isAuthenticated())
                ? currentUserService.getCurrentUser(authentication).getId()
                : null;
        return ResponseEntity.ok(userProfileService.getProfile(id, currentUserId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberSearchResponse>> searchUsers(@RequestParam String q, Authentication authentication) {
        UUID currentUserId = (authentication != null && authentication.isAuthenticated())
                ? currentUserService.getCurrentUser(authentication).getId()
                : null;
        return ResponseEntity.ok(userProfileService.searchUsers(q, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body("You can only edit your own profile.");
        }

        userProfileService.updateProfile(
                id,
                body.get("displayName"),
                body.get("bio"),
                body.get("profilePictureUrl")
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/favorites")
    public ResponseEntity<?> updateFavorites(
            @PathVariable UUID id,
            @RequestBody List<Map<String, Object>> favoriteMovies,
            Authentication authentication
    ) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).body("You can only edit your own favorites.");
        }

        userProfileService.updateFavoriteMovies(id, favoriteMovies);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable UUID id, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        userProfileService.blockUser(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable UUID id, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        userProfileService.unblockUser(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<MemberSearchResponse>> getBlockedUsers(Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        List<MemberSearchResponse> blocked = userProfileService.getBlockedUsers(currentUser.getId());
        return ResponseEntity.ok(blocked);
    }
}