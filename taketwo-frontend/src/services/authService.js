import api from './api';

export async function register({ username, email, password }) {
  const { data } = await api.post('/auth/register', { username, email, password });
  return data;
}

export async function login({ emailOrUsername, password }) {
  const { data } = await api.post('/auth/login', { emailOrUsername, password });
  return data;
}

export function logout() {
  localStorage.removeItem('taketwo_token');
}
