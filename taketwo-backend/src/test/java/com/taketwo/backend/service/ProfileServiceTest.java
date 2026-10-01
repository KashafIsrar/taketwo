package com.taketwo.backend.service;

import com.taketwo.backend.dto.MemberSearchResponse;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.FollowRepository;
import com.taketwo.backend.repository.MovieLogRepository;
import com.taketwo.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProfileServiceTest {

    private UserRepository userRepository;
    private FollowRepository followRepository;
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        followRepository = mock(FollowRepository.class);
        userProfileService = new UserProfileService(
                userRepository,
                mock(MovieLogRepository.class),
                followRepository
        );
    }

    @Test
    void searchUsers_shouldReturnUserIdAndFollowStatus() {
        UUID currentUserId = UUID.randomUUID();
        UUID resultUserId = UUID.randomUUID();
        User resultUser = User.builder()
                .id(resultUserId)
                .username("cinephile")
                .displayName("Cinema Fan")
                .build();
        when(userRepository
                .findTop20ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByUsernameAsc(
                        "cin", "cin"))
                .thenReturn(List.of(resultUser));
        when(followRepository.existsByFollower_IdAndFollowee_Id(currentUserId, resultUserId)).thenReturn(true);

        List<MemberSearchResponse> results = userProfileService.searchUsers("  cin  ", currentUserId);

        assertEquals(1, results.size());
        assertEquals(resultUserId, results.get(0).id());
        assertEquals("cinephile", results.get(0).username());
        assertEquals("Cinema Fan", results.get(0).displayName());
        assertTrue(results.get(0).isFollowing());
        verify(userRepository)
                .findTop20ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByUsernameAsc(
                        "cin", "cin");
    }

    @Test
    void searchUsers_shouldReturnEmptyForBlankQuery() {
        assertTrue(userProfileService.searchUsers("  ", null).isEmpty());
        verifyNoInteractions(userRepository, followRepository);
    }
}