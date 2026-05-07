/**
 * API de autenticación: login, register, logout.
 *
 * @author Miguel Angel Blandon Montes
 */

import { API_ENDPOINTS, STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';
import apiClient from './client';

/**
 * Inicia sesión con email y contraseña.
 * Guarda el token y datos del usuario en SecureStore.
 */
export const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>(
    API_ENDPOINTS.AUTH.LOGIN,
    credentials
  );
  const authData = response.data.data;
  await storage.setItem(STORAGE_KEYS.TOKEN, authData.token);
  await storage.setItem(STORAGE_KEYS.TOKEN_TYPE, authData.tokenType);
  await storage.setItem(STORAGE_KEYS.EXPIRES_IN, authData.expiresIn.toString());
  await storage.setItem(STORAGE_KEYS.ACCOUNT_ID, authData.accountId);
  await storage.setItem(STORAGE_KEYS.USER_EMAIL, authData.email);
  await storage.setItem(STORAGE_KEYS.USER_NAMES, authData.names);
  await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, authData.lastnames);
  await storage.setItem(STORAGE_KEYS.USER_PHONE, authData.phone);
  await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, authData.hasMasterKey.toString());
  return authData;
};

/**
 * Registra un nuevo usuario.
 * Guarda el token y datos del usuario en SecureStore.
 */
export const register = async (userData: RegisterRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>(
    API_ENDPOINTS.AUTH.REGISTER,
    userData
  );
  const authData = response.data.data;
  await storage.setItem(STORAGE_KEYS.TOKEN, authData.token);
  await storage.setItem(STORAGE_KEYS.TOKEN_TYPE, authData.tokenType);
  await storage.setItem(STORAGE_KEYS.EXPIRES_IN, authData.expiresIn.toString());
  await storage.setItem(STORAGE_KEYS.ACCOUNT_ID, authData.accountId);
  await storage.setItem(STORAGE_KEYS.USER_EMAIL, authData.email);
  await storage.setItem(STORAGE_KEYS.USER_NAMES, authData.names);
  await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, authData.lastnames);
  await storage.setItem(STORAGE_KEYS.USER_PHONE, authData.phone);
  await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, authData.hasMasterKey.toString());
  return authData;
};

/**
 * Cierra sesión: limpia el almacenamiento local.
 * Opcionalmente se puede llamar a un endpoint de logout en el backend.
 */
export const logout = async (): Promise<void> => {
  // Limpiar todo lo relacionado con autenticación
  await storage.deleteItem(STORAGE_KEYS.TOKEN);
  await storage.deleteItem(STORAGE_KEYS.TOKEN_TYPE);
  await storage.deleteItem(STORAGE_KEYS.EXPIRES_IN);
  await storage.deleteItem(STORAGE_KEYS.ACCOUNT_ID);
  await storage.deleteItem(STORAGE_KEYS.USER_EMAIL);
  await storage.deleteItem(STORAGE_KEYS.USER_NAMES);
  await storage.deleteItem(STORAGE_KEYS.USER_LASTNAMES);
  await storage.deleteItem(STORAGE_KEYS.USER_PHONE);
  await storage.deleteItem(STORAGE_KEYS.HAS_MASTER_KEY);
  // Opcional: llamar a endpoint de logout
  // await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
};

/**
 * Verifica si hay un token guardado (sesión activa).
 */
export const isAuthenticated = async (): Promise<boolean> => {
  const token = await storage.getItem(STORAGE_KEYS.TOKEN);
  return token !== null;
};

/**
 * Obtiene los datos del usuario guardados localmente.
 */
export const getUserData = async (): Promise<Partial<AuthResponse> | null> => {
  const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
  if (!accountId) return null;
  return {
    accountId,
    email: await storage.getItem(STORAGE_KEYS.USER_EMAIL) || undefined,
    names: await storage.getItem(STORAGE_KEYS.USER_NAMES) || undefined,
    lastnames: await storage.getItem(STORAGE_KEYS.USER_LASTNAMES) || undefined,
    phone: await storage.getItem(STORAGE_KEYS.USER_PHONE) || undefined,
    hasMasterKey: (await storage.getItem(STORAGE_KEYS.HAS_MASTER_KEY)) === 'true',
  };
};