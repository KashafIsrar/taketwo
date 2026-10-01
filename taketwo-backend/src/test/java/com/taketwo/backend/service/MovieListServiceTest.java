package com.taketwo.backend.service;

import com.taketwo.backend.entity.MovieList;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.controller.MovieListController.CreateListRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taketwo.backend.repository.MovieListItemRepository;
import com.taketwo.backend.repository.MovieListRepository;
import com.taketwo.backend.repository.MovieRepository;
import com.taketwo.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MovieListServiceTest {

    private MovieListRepository movieListRepository;
    private MovieListItemRepository movieListItemRepository;
    private MovieRepository movieRepository;
    private UserRepository userRepository;
    private MovieListService movieListService;

    @BeforeEach
    void setUp() {
        movieListRepository = mock(MovieListRepository.class);
        movieListItemRepository = mock(MovieListItemRepository.class);
        movieRepository = mock(MovieRepository.class);
        userRepository = mock(UserRepository.class);

        movieListService = new MovieListService(
                movieListRepository,
                movieListItemRepository,
                movieRepository,
                userRepository
        );
    }

    @Test
    void createList_shouldMapPrivateFlagToPublicFlag() {
        User user = new User();
        ArgumentCaptor<MovieList> captor = ArgumentCaptor.forClass(MovieList.class);
        when(movieListRepository.save(any(MovieList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieList created = movieListService.createList(user, "Favorites", "desc", true);

        assertFalse(created.isPublic());

        MovieList publicList = movieListService.createList(user, "Public Picks", "desc", false);
        assertTrue(publicList.isPublic());
    }

    @Test
    void createListRequest_shouldBindIsPrivateJsonKey() throws Exception {
        CreateListRequest request = new ObjectMapper().readValue(
                "{\"title\":\"Favorites\",\"description\":\"Picks\",\"isPrivate\":true}",
                CreateListRequest.class
        );

        assertTrue(request.isPrivate());
    }
}
