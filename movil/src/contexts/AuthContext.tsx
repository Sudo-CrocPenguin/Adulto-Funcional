/**
 * Contexto de autenticación global.
 * Provee estado de usuario, funciones de login/register/logout y verificación de sesión.
 *
 * @author Miguel Angel Blandon Montes
 */

import React, { createContext, ReactNode, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/authApi';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

interface AuthContextType {
  user: Partial<AuthResponse> | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  checkAuthStatus: () => Promise<void>;
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
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
    } catch (err) {
      console.error('Error checking auth status', err);
      setError('Error de autenticación');
    } finally {
      setIsLoading(false);
    }
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
      setError(err.response?.data?.message || 'Error al iniciar sesión');
      throw err;
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
      setError(err.response?.data?.message || 'Error al registrarse');
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
      setUser(null);
      setIsAuthenticated(false);
    } catch (err) {
      console.error('Logout error', err);
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const value = {
    user,
    isLoading,
    isAuthenticated,
    login,
    register,
    logout,
    checkAuthStatus,
    error,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};