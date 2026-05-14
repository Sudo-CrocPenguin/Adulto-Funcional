import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import * as authApi from '../api/authApi';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';
import { updateStreak, getCurrentStreak, getMaxStreak } from '../services/streakService';

interface AuthContextType {
  user: Partial<AuthResponse> | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkAuthStatus: () => Promise<void>;
  error: string | null;
  refreshUser: () => Promise<void>;
  streak: number;
  maxStreak: number;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<Partial<AuthResponse> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [streak, setStreak] = useState(0);
  const [maxStreak, setMaxStreak] = useState(0);

  const updateAndLoadStreak = async () => {
    const { current, max } = await updateStreak();
    setStreak(current);
    setMaxStreak(max);
  };

  const checkAuthStatus = async () => {
    try {
      setIsLoading(true);
      const auth = await authApi.isAuthenticated();
      setIsAuthenticated(auth);
      if (auth) {
        const userData = await authApi.getUserData();
        setUser(userData);
        await updateAndLoadStreak();
      } else {
        setUser(null);
      }
    } catch (err: any) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const refreshUser = async () => {
    const userData = await authApi.getUserData();
    setUser(userData);
  };

  const login = async (credentials: LoginRequest) => {
    try {
      setError(null);
      setIsLoading(true);
      const response = await authApi.login(credentials);
      setUser({
        accountId: response.accountId,
        email: response.email,
        names: response.names,
        lastnames: response.lastnames,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      });
      setIsAuthenticated(true);
      await updateAndLoadStreak();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Error en login';
      setError(msg);
      throw new Error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (userData: RegisterRequest) => {
    try {
      setError(null);
      setIsLoading(true);
      const response = await authApi.register(userData);
      setUser({
        accountId: response.accountId,
        email: response.email,
        names: response.names,
        lastnames: response.lastnames,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      });
      setIsAuthenticated(true);
      await updateAndLoadStreak();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Error en registro';
      setError(msg);
      throw new Error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    await authApi.logout();
    setUser(null);
    setIsAuthenticated(false);
    setStreak(0);
    setMaxStreak(0);
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, isAuthenticated, login, register, logout, checkAuthStatus, error, refreshUser, streak, maxStreak }}>
      {children}
    </AuthContext.Provider>
  );
};
