package com.taketwo.backend.service;

import com.taketwo.backend.dto.AuthResponse;
import com.taketwo.backend.dto.LoginRequest;
import com.taketwo.backend.dto.RegisterRequest;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.UserRepository;
import com.taketwo.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.emailOrUsername())
                .or(() -> userRepository.findByUsername(request.emailOrUsername()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }
}
