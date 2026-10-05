package com.taketwo.backend.service;

import com.taketwo.backend.entity.ChallengeProgress;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.ChallengeProgressRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private static final int ACTIVE_CHALLENGE_COUNT = 3;

    private final MovieLogRepository movieLogRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final UserRepository userRepository;

    // --- Badge catalog: real metrics only, no fake totals-as-proxy-for-genre ---
    private record BadgeDefinition(String id, String title, String description, long goal, ToLongFunction<UUID> currentSupplier) {}

    private List<BadgeDefinition> badgeCatalog() {
        return List.of(
                new BadgeDefinition("first_log", "First Take", "Log your first film", 1,
                        userId -> movieLogRepository.countByUser_Id(userId)),
                new BadgeDefinition("film_fanatic", "Film Fanatic", "Log 25 films total", 25,
                        userId -> movieLogRepository.countByUser_Id(userId)),
                new BadgeDefinition("nightmare_fiend", "Nightmare Fiend", "Log 10 Horror films", 10,
                        userId -> movieLogRepository.countByUserIdAndGenreName(userId, "Horror")),
                new BadgeDefinition("retro_lens", "Retro Lens", "Log 10 films from the 1980s", 10,
                        userId -> movieLogRepository.countByUserIdAndReleaseDateRange(
                                userId, LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1)))
        );
    }

    // --- Challenge catalog: a larger pool than what's shown at once, for rotation ---
    private record ChallengeDefinition(String key, String title, String description, long goal, ToLongFunction<UUID> currentSupplier) {}

    private Map<String, ChallengeDefinition> challengeCatalog() {
        List<ChallengeDefinition> defs = List.of(
                new ChallengeDefinition("eighties_nostalgia", "1980s Nostalgia", "Watch 5 films from the 1980s", 5,
                        userId -> movieLogRepository.countByUserIdAndReleaseDateRange(
                                userId, LocalDate.of(1980, 1, 1), LocalDate.of(1990, 1, 1))),
                new ChallengeDefinition("nineties_flashback", "90s Flashback", "Watch 5 films from the 1990s", 5,
                        userId -> movieLogRepository.countByUserIdAndReleaseDateRange(
                                userId, LocalDate.of(1990, 1, 1), LocalDate.of(2000, 1, 1))),
                new ChallengeDefinition("horror_marathon", "Horror Marathon", "Watch 5 Horror films", 5,
                        userId -> movieLogRepository.countByUserIdAndGenreName(userId, "Horror")),
                new ChallengeDefinition("scifi_deep_dive", "Sci-Fi Deep Dive", "Watch 5 Science Fiction films", 5,
                        userId -> movieLogRepository.countByUserIdAndGenreName(userId, "Science Fiction")),
                new ChallengeDefinition("drama_deep_dive", "Drama Deep Dive", "Watch 5 Drama films", 5,
                        userId -> movieLogRepository.countByUserIdAndGenreName(userId, "Drama")),
                new ChallengeDefinition("comedy_binge", "Comedy Binge", "Watch 5 Comedy films", 5,
                        userId -> movieLogRepository.countByUserIdAndGenreName(userId, "Comedy"))
        );
        return defs.stream().collect(Collectors.toMap(ChallengeDefinition::key, d -> d));
    }

    @Transactional
    public Map<String, Object> getUserGamificationProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        long totalLogged = movieLogRepository.countByUser_Id(userId);

        List<LocalDate> watchedDates = movieLogRepository.findDistinctWatchedDatesByUserId(userId);
        int currentStreak = computeCurrentStreak(watchedDates);
        int longestStreak = computeLongestStreak(watchedDates);

        List<Map<String, Object>> badges = badgeCatalog().stream()
                .map(b -> {
                    long current = Math.min(b.currentSupplier().applyAsLong(userId), b.goal());
                    boolean unlocked = current >= b.goal();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", b.id());
                    m.put("title", b.title());
                    m.put("description", b.description());
                    m.put("current", current);
                    m.put("goal", b.goal());
                    m.put("unlocked", unlocked);
                    m.put("status", unlocked ? "Unlocked" : "Locked");
                    return m;
                })
                .toList();

        var challengeResult = resolveChallenges(user, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentStreakDays", currentStreak);
        result.put("longestStreak", longestStreak);
        result.put("totalLogged", totalLogged);
        result.put("badges", badges);
        result.put("activeChallenges", challengeResult.active());
        result.put("justCompleted", challengeResult.justCompleted());
        return result;
    }

    private record ChallengeResolution(List<Map<String, Object>> active, List<Map<String, Object>> justCompleted) {}

    private ChallengeResolution resolveChallenges(User user, UUID userId) {
        Map<String, ChallengeDefinition> catalog = challengeCatalog();
        List<ChallengeProgress> assignments = challengeProgressRepository.findByUser_IdAndCompletedAtIsNull(userId);

        List<Map<String, Object>> justCompleted = new ArrayList<>();
        List<ChallengeProgress> stillActive = new ArrayList<>();

        // Check each currently-active assignment against its real metric.
        // Anything that's hit goal gets marked completed now (persisted,
        // never reassigned again) and reported separately so the frontend
        // knows to show the celebration modal.
        for (ChallengeProgress progress : assignments) {
            ChallengeDefinition def = catalog.get(progress.getChallengeKey());
            if (def == null) continue; // catalog changed since assignment - skip gracefully

            long current = def.currentSupplier().applyAsLong(userId);
            if (current >= def.goal()) {
                progress.setCompletedAt(Instant.now());
                challengeProgressRepository.save(progress);
                justCompleted.add(toChallengeMap(def, current));
            } else {
                stillActive.add(progress);
            }
        }

        // Top up to ACTIVE_CHALLENGE_COUNT with challenges this user has
        // never been assigned before (completed or currently active) -
        // this is the actual "rotation": only happens when a slot opens up.
        Set<String> alreadyUsedKeys = challengeProgressRepository.findByUser_Id(userId).stream()
                .map(ChallengeProgress::getChallengeKey)
                .collect(Collectors.toSet());

        List<String> candidateKeys = new ArrayList<>(catalog.keySet());
        candidateKeys.removeAll(alreadyUsedKeys);
        Collections.shuffle(candidateKeys);

        int needed = ACTIVE_CHALLENGE_COUNT - stillActive.size();
        for (int i = 0; i < needed && i < candidateKeys.size(); i++) {
            ChallengeProgress fresh = ChallengeProgress.builder()
                    .user(user)
                    .challengeKey(candidateKeys.get(i))
                    .build();
            challengeProgressRepository.save(fresh);
            stillActive.add(fresh);
        }

        List<Map<String, Object>> activeResponse = stillActive.stream()
                .map(p -> {
                    ChallengeDefinition def = catalog.get(p.getChallengeKey());
                    long current = def.currentSupplier().applyAsLong(userId);
                    return toChallengeMap(def, current);
                })
                .toList();

        return new ChallengeResolution(activeResponse, justCompleted);
    }

    private Map<String, Object> toChallengeMap(ChallengeDefinition def, long current) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", def.key());
        m.put("title", def.title());
        m.put("description", def.description());
        m.put("current", Math.min(current, def.goal()));
        m.put("goal", def.goal());
        return m;
    }

    // A streak only counts as "current" if the most recent watch was today
    // or yesterday - otherwise it's broken and reset to 0, not just stale.
    private int computeCurrentStreak(List<LocalDate> sortedDescDates) {
        if (sortedDescDates.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        LocalDate mostRecent = sortedDescDates.get(0);
        if (mostRecent.isBefore(today.minusDays(1))) return 0;

        int streak = 1;
        LocalDate expected = mostRecent.minusDays(1);
        for (int i = 1; i < sortedDescDates.size(); i++) {
            LocalDate d = sortedDescDates.get(i);
            if (d.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (d.isBefore(expected)) {
                break;
            }
        }
        return streak;
    }

    private int computeLongestStreak(List<LocalDate> sortedDescDates) {
        if (sortedDescDates.isEmpty()) return 0;
        List<LocalDate> asc = new ArrayList<>(sortedDescDates);
        Collections.reverse(asc);

        int longest = 1, current = 1;
        for (int i = 1; i < asc.size(); i++) {
            if (asc.get(i).equals(asc.get(i - 1).plusDays(1))) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }
        return longest;
    }
}