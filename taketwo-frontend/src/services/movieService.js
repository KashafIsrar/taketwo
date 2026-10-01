import api from './api';

export async function getPopular(page = 1) {
  const { data } = await api.get('/movies/popular', { params: { page } });
  return data;
}

export async function getTrending(window = 'week') {
  const { data } = await api.get('/movies/trending', { params: { window } });
  return data;
}

export async function searchMovies(query, page = 1) {
  const { data } = await api.get('/movies/search', { params: { q: query, page } });
  return data;
}

export async function getMovieDetails(tmdbId) {
  const { data } = await api.get(`/movies/${tmdbId}`);
  return data;
}

export async function logMovie({ tmdbId, watchedDate, rating, rewatch, reviewText, containsSpoilers }) {
  const { data } = await api.post('/movies/logs', {
    tmdbId,
    watchedDate,
    rating,
    rewatch,
    reviewText: reviewText || null,
    containsSpoilers: containsSpoilers || false,
  });
  return data;
}

export async function getUserDiary(userId) {
  const { data } = await api.get(`/movies/logs/user/${userId}`);
  return data;
}

// Toggle endpoint - if the movie is already on the watchlist, this removes it.
export async function toggleWatchlist(tmdbId) {
  const { data } = await api.post('/movies/watchlist', { tmdbId });
  return data;
}

export async function getUserWatchlist(userId) {
  const { data } = await api.get(`/movies/watchlist/user/${userId}`);
  return data;
}

export async function getWatchlistStatus(tmdbId) {
  const { data } = await api.get(`/movies/watchlist/status/${tmdbId}`);
  return data.onWatchlist;
}
// --- Movie Lists (Phase 6E) ---

export async function getAllPublicLists() {
  const { data } = await api.get('/lists/public');
  return data;
}

export async function getUserLists(userId) {
  const { data } = await api.get(`/lists/user/${userId}`);
  return data;
}

export async function getListDetails(listId) {
  const { data } = await api.get(`/lists/${listId}`);
  return data;
}

export async function createMovieList(listData) {
  const { data } = await api.post('/lists', {
    title: listData.title,
    description: listData.description ?? '',
    isPrivate: Boolean(listData.isPrivate),
  });
  return data;
}

export async function addMovieToList(listId, movieId, position) {
  const { data } = await api.post(`/lists/${listId}/items`, { movieId, position });
  return data;
}

export async function removeMovieFromList(listId, itemId) {
  const { data } = await api.delete(`/lists/${listId}/items/${itemId}`);
  return data;
}

export async function deleteMovieList(listId) {
  const { data } = await api.delete(`/lists/${listId}`);
  return data;
}