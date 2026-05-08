/**
 * Configuración global de la aplicación.
 * 
 * @author Miguel Angel Blandon Montes
 */

// Detectar si estamos en web (navegador) o en móvil/emulador
const isWeb = typeof window !== 'undefined' && window.location?.protocol === 'http:';

// Para web local, usar localhost. Para emulador Android, usar 10.0.2.2. Para dispositivo físico, IP de la máquina.
export const API_BASE_URL = isWeb 
  ? 'http://localhost:8080' 
  : 'http://10.0.2.2:8080';

export const API_TIMEOUT = 30000;

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
  },
};
