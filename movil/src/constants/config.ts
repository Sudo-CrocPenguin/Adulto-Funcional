export const API_BASE_URL = 'https://audry-subsphenoidal-bovinely.ngrok-free.dev'
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
