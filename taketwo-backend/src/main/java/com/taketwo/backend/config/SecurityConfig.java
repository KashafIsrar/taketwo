package com.taketwo.backend.config;

import com.taketwo.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Preflight requests never carry credentials - if a browser's OPTIONS
                        // request ever fails authorization, the browser reports it as an opaque
                        // CORS failure, indistinguishable from a real 403 without this.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // "Is this on MY watchlist" needs to know who's asking - must be
                        // matched before the general public-GET rule below.
                        .requestMatchers(HttpMethod.GET, "/api/movies/watchlist/status/**").authenticated()
                        // "Have I liked this log" needs to know who's asking too - same reason,
                        // same placement requirement.
                        .requestMatchers(HttpMethod.GET, "/api/movies/logs/*/like-status").authenticated()
                        // Browsing, search, movie detail, and reading someone's diary/watchlist stay public.
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        // Writing a log entry or touching a watchlist requires a signed-in user.
                        .requestMatchers(HttpMethod.POST, "/api/movies/logs", "/api/movies/watchlist").authenticated()
                        // "Am I following this user" needs to know who's asking - same rule as
                        // watchlist/status, and for the same reason: this MUST come before the
                        // general public-GET rule for /api/users/** below, not after it.
                        .requestMatchers(HttpMethod.GET, "/api/users/*/follow-status").authenticated()
                        // Followers/following lists and public profile data stay public.
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        // Following/unfollowing someone requires a signed-in user.
                        .requestMatchers(HttpMethod.POST, "/api/users/*/follow").authenticated()
                        // Add among the other GET permitAll rules, before anyRequest().authenticated():
                        .requestMatchers(HttpMethod.GET, "/api/discussions/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    // Spring Security 6 defaults to 403 for an unauthenticated request to a
                    // protected route, which is easy to mistake for a permitAll bug. This makes
                    // "no/expired token" return 401 instead, so it's distinguishable in the
                    // network tab from an actual authorization misconfiguration (still a 403).
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":401,\"message\":\"Authentication required\"}");
                }))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // TODO: replace with the deployed frontend origin(s) before going live.
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}