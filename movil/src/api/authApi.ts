import apiClient from './client';
import { API_ENDPOINTS, STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';
import { LoginRequest, RegisterRequest, AuthResponse, ApiResponse } from '../types/auth.types';

// 🔧 MODO DEMO = false → Usar backend real
const DEMO_MODE = false;

// Credenciales de prueba solo para modo demo (no afecta al backend real)
const DEMO_CREDENTIALS = {
  email: 'admin@desarrollo.com',
  password: 'Admin123!',
};

const storeAuthData = async (authData: AuthResponse) => {
  await storage.setItem(STORAGE_KEYS.TOKEN, authData.token);
  await storage.setItem(STORAGE_KEYS.TOKEN_TYPE, authData.tokenType);
  await storage.setItem(STORAGE_KEYS.EXPIRES_IN, authData.expiresIn.toString());
  await storage.setItem(STORAGE_KEYS.ACCOUNT_ID, authData.accountId);
  await storage.setItem(STORAGE_KEYS.USER_EMAIL, authData.email);
  await storage.setItem(STORAGE_KEYS.USER_NAMES, authData.names);
  await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, authData.lastnames);
  await storage.setItem(STORAGE_KEYS.USER_PHONE, authData.phone);
  await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, authData.hasMasterKey.toString());
};

export const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
  if (DEMO_MODE) {
    await new Promise(resolve => setTimeout(resolve, 800));
    if (credentials.email === DEMO_CREDENTIALS.email && credentials.password === DEMO_CREDENTIALS.password) {
      const demoAuth: AuthResponse = {
        token: 'demo-token',
        tokenType: 'Bearer',
        expiresIn: 3600000,
        accountId: '123e4567-e89b-12d3-a456-426614174000',
        names: 'Admin',
        lastnames: 'Demo',
        email: credentials.email,
        phone: '+573001234567',
        createdAt: new Date().toISOString(),
        hasMasterKey: false,
      };
      await storeAuthData(demoAuth);
      return demoAuth;
    } else {
      throw new Error('Credenciales inválidas. Demo: admin@desarrollo.com / Admin123!');
    }
  }

  // Llamada real al backend
  const response = await apiClient.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.AUTH.LOGIN, credentials);
  const authData = response.data.data;
  await storeAuthData(authData);
  return authData;
};

export const register = async (userData: RegisterRequest): Promise<AuthResponse> => {
  if (DEMO_MODE) {
    await new Promise(resolve => setTimeout(resolve, 800));
    const demoAuth: AuthResponse = {
      token: 'demo-token',
      tokenType: 'Bearer',
      expiresIn: 3600000,
      accountId: '123e4567-e89b-12d3-a456-426614174000',
      names: userData.names,
      lastnames: userData.lastnames,
      email: userData.email,
      phone: userData.phone,
      createdAt: new Date().toISOString(),
      hasMasterKey: false,
    };
    await storeAuthData(demoAuth);
    return demoAuth;
  }

  const response = await apiClient.post<ApiResponse<AuthResponse>>(API_ENDPOINTS.AUTH.REGISTER, userData);
  const authData = response.data.data;
  await storeAuthData(authData);
  return authData;
};

export const logout = async (): Promise<void> => {
  // Opcional: llamar al endpoint /logout si lo deseas
  try {
    await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
  } catch (error) {
    console.warn('Error al cerrar sesión en backend', error);
  } finally {
    await storage.deleteItem(STORAGE_KEYS.TOKEN);
    await storage.deleteItem(STORAGE_KEYS.TOKEN_TYPE);
    await storage.deleteItem(STORAGE_KEYS.EXPIRES_IN);
    await storage.deleteItem(STORAGE_KEYS.ACCOUNT_ID);
    await storage.deleteItem(STORAGE_KEYS.USER_EMAIL);
    await storage.deleteItem(STORAGE_KEYS.USER_NAMES);
    await storage.deleteItem(STORAGE_KEYS.USER_LASTNAMES);
    await storage.deleteItem(STORAGE_KEYS.USER_PHONE);
    await storage.deleteItem(STORAGE_KEYS.HAS_MASTER_KEY);
  }
};

export const isAuthenticated = async (): Promise<boolean> => {
  const token = await storage.getItem(STORAGE_KEYS.TOKEN);
  return token !== null;
};

export const getUserData = async (): Promise<Partial<AuthResponse> | null> => {
  const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
  if (!accountId) return null;
  return {
    accountId,
    email: (await storage.getItem(STORAGE_KEYS.USER_EMAIL)) || undefined,
    names: (await storage.getItem(STORAGE_KEYS.USER_NAMES)) || undefined,
    lastnames: (await storage.getItem(STORAGE_KEYS.USER_LASTNAMES)) || undefined,
    phone: (await storage.getItem(STORAGE_KEYS.USER_PHONE)) || undefined,
    hasMasterKey: (await storage.getItem(STORAGE_KEYS.HAS_MASTER_KEY)) === 'true',
  };
};
