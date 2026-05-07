/**
 * Configuración global de la aplicación.
 * Define la URL base de la API, timeouts y claves para SecureStore.
 *
 * @author Miguel Angel Blandon Montes
 */

// TODO: Cambiar según entorno (desarrollo, producción)
// Para emulador Android: http://10.0.2.2:8080
// Para dispositivo físico: http://192.168.x.x:8080


export const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://10.0.2.2:8080';

export const API_TIMEOUT = 30000; // 30 segundos

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
    DELETE: (id: string) => `/api/account/${id}`,
  },
  FINANCES: {
    MOVEMENTS: '/api/finances/movements',
    MOVEMENT: (id: string) => `/api/finances/movements/${id}`,
    CATEGORIES: '/api/finances/categories',
    FIXED_EXPENSES: '/api/finances/fixed-expenses',
    FIXED_EXPENSE: (id: string) => `/api/finances/fixed-expenses/${id}`,
  },
  AGENDA: {
    EVENTS: '/api/agenda/events',
    EVENT: (id: string) => `/api/agenda/events/${id}`,
  },
  SECURITY: {
    PASSWORDS: '/api/security/passwords',
    PASSWORD: (id: string) => `/api/security/passwords/${id}`,
    VERIFY_MASTER_KEY: '/api/security/verify-master-key',
  },
};