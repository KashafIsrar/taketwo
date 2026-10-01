import axios from 'axios';

// Base API configuration
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
});

// Attach JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('taketwo_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle expired tokens
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('taketwo_token');
    }
    return Promise.reject(error);
  },
);

// --- Social System Endpoints ---
export const getActivityFeed = async () => {
  const response = await api.get('/feed');
  return response.data;
};

export const getFollowStatus = async (userId) => {
  const response = await api.get(`/users/${userId}/follow-status`);
  return response.data;
};

export const toggleFollow = async (userId) => {
  const response = await api.post(`/users/${userId}/follow`);
  return response.data;
};

export const getUserProfile = async (userId) => {
  const response = await api.get(`/users/${userId}/profile`);
  return response.data;
};

// --- Movie Lists Endpoints ---
export const getUserLists = async (userId) => {
  const response = await api.get(`/lists/user/${userId}`);
  return response.data;
};

export const getListById = async (listId) => {
  const response = await api.get(`/lists/${listId}`);
  return response.data;
};

export const createList = async (listData) => {
  const response = await api.post('/lists', listData);
  return response.data;
};

export const addMovieToList = async (listId, tmdbId) => {
  const response = await api.post(`/lists/${listId}/items`, { tmdbId });
  return response.data;
};

export const deleteList = async (listId) => {
  const response = await api.delete(`/lists/${listId}`);
  return response.data;
};

export const searchMovies = async (query) => {
  const response = await api.get('/movies/search', {
    params: { q: query } // Changed from 'query' to 'q' to match MovieController
  });
  return response.data;
};

export default api;