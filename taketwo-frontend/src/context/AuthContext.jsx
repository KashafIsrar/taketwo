import { createContext, useContext, useState, useCallback } from 'react';
import * as authService from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('taketwo_user');
    return stored ? JSON.parse(stored) : null;
  });

  const persistSession = useCallback((authResponse) => {
    const { token, userId, username, displayName } = authResponse;
    localStorage.setItem('taketwo_token', token);
    const nextUser = { userId, username, displayName };
    localStorage.setItem('taketwo_user', JSON.stringify(nextUser));
    setUser(nextUser);
  }, []);

  const login = useCallback(async (credentials) => {
    const authResponse = await authService.login(credentials);
    persistSession(authResponse);
  }, [persistSession]);

  const register = useCallback(async (details) => {
    const authResponse = await authService.register(details);
    persistSession(authResponse);
  }, [persistSession]);

  const logout = useCallback(() => {
    authService.logout();
    localStorage.removeItem('taketwo_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
