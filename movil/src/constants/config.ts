import Constants from 'expo-constants';
import { Platform } from 'react-native';

const DEFAULT_API_PORT = '8080';

const normalizeBaseUrl = (url: string) => url.replace(/\/+$/, '');

const getExpoHost = (): string | null => {
  const hostUri = Constants.expoConfig?.hostUri ?? Constants.expoGoConfig?.debuggerHost;
  const host = hostUri?.split(':')[0]?.trim();

  return host || null;
};

const inferDevelopmentApiUrl = (): string => {
  const expoHost = getExpoHost();

  if (expoHost && expoHost !== 'localhost' && expoHost !== '127.0.0.1') {
    return `http://${expoHost}:${DEFAULT_API_PORT}`;
  }

  if (Platform.OS === 'android') {
    return `http://10.0.2.2:${DEFAULT_API_PORT}`;
  }

  return `http://localhost:${DEFAULT_API_PORT}`;
};

const configuredApiUrl = process.env.EXPO_PUBLIC_API_URL?.trim();

export const API_BASE_URL = normalizeBaseUrl(configuredApiUrl || inferDevelopmentApiUrl());
export const API_TIMEOUT = Number(process.env.EXPO_PUBLIC_API_TIMEOUT || 30000);

export const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  TOKEN_TYPE: 'token_type',
  EXPIRES_IN: 'expires_in',
  ACCOUNT_ID: 'account_id',
  USER_EMAIL: 'user_email',
  USER_NAMES: 'user_names',
  USER_LASTNAMES: 'user_lastnames',
  USER_PHONE: 'user_phone',
  HAS_MASTER_KEY: 'has_master_key',
};

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    LOGOUT: '/api/auth/logout',
  },
  ACCOUNT: {
    GET: (id: string) => `/api/account/${id}`,
    UPDATE: (id: string) => `/api/account/${id}`,
    CHANGE_PASSWORD: (id: string) => `/api/account/${id}/password`,
    DELETE: (id: string) => `/api/account/${id}`,
  },
  FINANCES: {
    MOVEMENTS: '/api/finances/movements',
    FIXED_EXPENSES: '/api/finances/fixed-expenses',
  },
  AGENDA: {
    EVENTS: '/api/agenda/events',
  },
  SECURITY: {
    PASSWORDS: '/api/security/passwords',
    MASTER_KEY_STATUS: '/api/security/master-key/status',
    CREATE_MASTER_KEY: '/api/security/master-key',
    VERIFY_MASTER_KEY: '/api/security/master-key/verify',
  },
};
