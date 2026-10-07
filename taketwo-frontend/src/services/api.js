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
  }
);

// --- Feed & User Endpoints ---
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
    params: { q: query },
  });
  return response.data;
};

// --- Messaging & Chat Endpoints ---
export const getInbox = async () => {
  const response = await api.get('/conversations');
  return response.data;
};

export const getOrCreateDm = async (targetUserId) => {
  const response = await api.post(`/conversations/dm/${targetUserId}`);
  return response.data;
};

export const getMessages = async (conversationId, after) => {
  const response = await api.get(`/conversations/${conversationId}/messages`, {
    params: after ? { after } : {},
  });
  return response.data;
};

export const sendMessage = async (conversationId, content) => {
  const response = await api.post(`/conversations/${conversationId}/messages`, { content });
  return response.data;
};

export const markRead = async (conversationId) => {
  const response = await api.post(`/conversations/${conversationId}/read`);
  return response.data;
};
export const markConversationRead = markRead;

export const createGroup = async (groupData) => {
  const response = await api.post('/conversations/group', groupData);
  return response.data;
};

export const getMutualFollowers = async (conversationId) => {
  const url = conversationId
    ? `/conversations/mutual-followers?conversationId=${conversationId}`
    : '/conversations/mutual-followers';
  const response = await api.get(url);
  return response.data;
};

export const inviteToGroup = async (conversationId, targetUserId) => {
  const response = await api.post(`/conversations/${conversationId}/invite`, { targetUserId });
  return response.data;
};

export const kickFromGroup = async (conversationId, targetUserId) => {
  const response = await api.delete(`/conversations/${conversationId}/kick/${targetUserId}`);
  return response.data;
};

export const renameGroup = async (conversationId, name) => {
  const response = await api.patch(`/conversations/${conversationId}/rename`, { name });
  return response.data;
};

export const getParticipants = async (conversationId) => {
  const response = await api.get(`/conversations/${conversationId}/participants`);
  return response.data;
};

// --- User Block Management Endpoints ---
export const blockUser = async (userId) => {
  const response = await api.post(`/users/${userId}/block`);
  return response.data;
};

export const unblockUser = async (userId) => {
  const response = await api.post(`/users/${userId}/unblock`);
  return response.data;
};

export const getBlockedUsers = async () => {
  const response = await api.get('/users/blocked');
  return response.data;
};

// --- Discussion Forum Endpoints ---
export const getDiscussionPosts = async (tmdbId, sort = 'new') => {
  const response = await api.get(`/discussions/movies/${tmdbId}/posts`, { params: { sort } });
  return response.data;
};

export const createDiscussionPost = async (tmdbId, { title, body, isSpoiler }) => {
  const response = await api.post(`/discussions/movies/${tmdbId}/posts`, { title, body, isSpoiler });
  return response.data;
};

export const getDiscussionPost = async (postId) => {
  const response = await api.get(`/discussions/posts/${postId}`);
  return response.data;
};

export const voteOnPost = async (postId, voteType) => {
  const response = await api.post(`/discussions/posts/${postId}/vote`, { voteType });
  return response.data;
};

export const getTopLevelComments = async (postId) => {
  const response = await api.get(`/discussions/posts/${postId}/comments`);
  return response.data;
};

export const getReplies = async (commentId, page = 0, limit = 5) => {
  const response = await api.get(`/discussions/comments/${commentId}/replies`, { params: { page, limit } });
  return response.data;
};

export const addDiscussionComment = async (postId, { body, isSpoiler, parentCommentId }) => {
  const response = await api.post(`/discussions/posts/${postId}/comments`, {
    body,
    isSpoiler,
    parentCommentId: parentCommentId || null,
  });
  return response.data;
};

export const voteOnComment = async (commentId, voteType) => {
  const response = await api.post(`/discussions/comments/${commentId}/vote`, { voteType });
  return response.data;
};

export default api;