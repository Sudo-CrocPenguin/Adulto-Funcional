import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import * as authApi from '../api/authApi';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

// Importación condicional para evitar errores en Expo Go
let Notifications: any = null;
if (typeof window !== 'undefined' && !window?.expo?.isExpoGo) {
  try {
    Notifications = require('expo-notifications');
  } catch (e) {
    console.warn('expo-notifications no disponible en este entorno');
  }
}

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

  const checkAuthStatus = async () => {
    try {
      setIsLoading(true);
      const auth = await authApi.isAuthenticated();
      setIsAuthenticated(auth);
      if (auth) {
        const userData = await authApi.getUserData();
        setUser(userData);
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
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Error en registro';
      setError(msg);
      throw new Error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    // Cancelar notificaciones solo si el módulo está disponible
    if (Notifications && Notifications.cancelAllScheduledNotificationsAsync) {
      await Notifications.cancelAllScheduledNotificationsAsync();
    }
    await authApi.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, isAuthenticated, login, register, logout, checkAuthStatus, error, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};
