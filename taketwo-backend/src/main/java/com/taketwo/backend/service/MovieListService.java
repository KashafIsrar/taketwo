package com.taketwo.backend.service;

import com.taketwo.backend.entity.Movie;
import com.taketwo.backend.entity.MovieList;
import com.taketwo.backend.entity.MovieListItem;
import com.taketwo.backend.entity.User;
import com.taketwo.backend.repository.MovieListItemRepository;
import com.taketwo.backend.repository.MovieListRepository;
import com.taketwo.backend.repository.MovieRepository;
import com.taketwo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieListService {

    private final MovieListRepository movieListRepository;
    private final MovieListItemRepository movieListItemRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public List<MovieList> getUserLists(UUID userId, UUID currentUserId) {
        if (userId.equals(currentUserId)) {
            return movieListRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return movieListRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId);
    }

    public MovieList getListById(UUID listId) {
        return movieListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found"));
    }

    @Transactional
    public MovieList createList(User user, String title, String description, boolean isPrivate) {
        MovieList list = MovieList.builder()
                .user(user)
                .title(title)
                .description(description)
                .isPublic(!isPrivate)
                .build();
        return movieListRepository.save(list);
    }

    @Transactional
    public MovieList addMovieToList(UUID listId, Long tmdbId, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to modify this list");
        }

        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> movieRepository.save(Movie.builder().tmdbId(tmdbId).build()));

        boolean exists = list.getItems().stream()
                .anyMatch(item -> item.getMovie().getTmdbId().equals(tmdbId));

        if (!exists) {
            MovieListItem item = MovieListItem.builder()
                    .movieList(list)
                    .movie(movie)
                    .position(list.getItems().size())
                    .build();
            list.getItems().add(item);
            return movieListRepository.save(list);
        }

        return list;
    }

    @Transactional
    public void deleteList(UUID listId, User user) {
        MovieList list = getListById(listId);
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this list");
        }
        movieListRepository.delete(list);
    }
}