package com.taketwo.backend.service;

import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    // JwtAuthFilter sets the authenticated principal's name to the user's
    // email (see JwtService/UserDetailsServiceImpl) - resolve back to the entity.
    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
