package com.taketwo.backend.controller;

import com.taketwo.backend.entity.MovieList;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.UserRepository;
import com.taketwo.backend.service.MovieListService;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class MovieListController {

    private final MovieListService movieListService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        String principal = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(principal);
        if (userOpt.isEmpty()) userOpt = userRepository.findByEmail(principal);
        if (userOpt.isEmpty()) {
            try {
                userOpt = userRepository.findById(UUID.fromString(principal));
            } catch (IllegalArgumentException ignored) {}
        }
        return userOpt.orElse(null);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MovieList>> getUserLists(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getAuthenticatedUser(userDetails);
        UUID currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(movieListService.getUserLists(userId, currentUserId));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<MovieList> getListById(@PathVariable UUID listId) {
        return ResponseEntity.ok(movieListService.getListById(listId));
    }

    @PostMapping
    public ResponseEntity<MovieList> createList(
            @Valid @RequestBody CreateListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        
        MovieList created = movieListService.createList(
                user, request.getTitle(), request.getDescription(), request.isPrivate());
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{listId}/items")
    public ResponseEntity<MovieList> addMovieToList(
            @PathVariable UUID listId,
            @RequestBody AddMovieRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(movieListService.addMovieToList(listId, request.getTmdbId(), user));
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(
            @PathVariable UUID listId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        movieListService.deleteList(listId, user);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CreateListRequest {
        @NotBlank
        private String title;
        private String description;

        @JsonProperty("isPrivate")
        private boolean isPrivate;
    }

    @Data
    public static class AddMovieRequest {
        private Long tmdbId;
    }
}