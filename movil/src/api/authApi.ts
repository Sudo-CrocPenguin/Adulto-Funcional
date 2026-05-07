/**
 * @file authApi.ts
 * @description Capa de acceso a datos para las operaciones de autenticación
 *              de la aplicación Adulto Funcional. Gestiona login, registro,
 *              logout y consulta de sesión, con soporte para un modo demo
 *              que simula respuestas del servidor sin necesidad de backend real.
 *
 * @module src/api/authApi
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Cuando {@link DEMO_MODE} está activo, todas las operaciones de red son
 * simuladas con un retardo de 800 ms. Las credenciales válidas en modo demo
 * están definidas en {@link DEMO_CREDENTIALS}.
 *
 * En producción (DEMO_MODE = false) las funciones deben conectarse al backend
 * real; actualmente lanzarán un error indicando que el backend no está disponible.
 *
 * Los datos de sesión se persisten usando {@link storage} bajo las claves
 * definidas en {@link STORAGE_KEYS}.
 *
 * @example
 * import * as authApi from '../api/authApi';
 *
 * const response = await authApi.login({ email: '...', password: '...' });
 * const isActive = await authApi.isAuthenticated();
 * await authApi.logout();
 */

import { STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth.types';

/**
 * @constant DEMO_MODE
 * @description Activa o desactiva el modo demo. Cuando es `true`, las llamadas
 *              a la API son simuladas localmente sin conexión a un backend real.
 *              Cambiar a `false` para conectar con el servidor de producción.
 */
const DEMO_MODE = true;

/**
 * @constant DEMO_CREDENTIALS
 * @description Credenciales válidas en modo demo para simular un inicio de sesión exitoso.
 *
 * @property {string} email    - Correo electrónico de prueba.
 * @property {string} password - Contraseña de prueba.
 */
const DEMO_CREDENTIALS = {
  email: 'admin@desarrollo.com',
  password: 'Admin123!',
};

/**
 * @function getDemoAuthResponse
 * @description Genera un objeto {@link AuthResponse} simulado para uso en modo demo.
 *              El campo `names` varía según si el email contiene la palabra "admin".
 *
 * @param {string} email - Correo electrónico del usuario que realiza la operación.
 * @returns {AuthResponse} Respuesta de autenticación ficticia con datos de prueba.
 */
const getDemoAuthResponse = (email: string): AuthResponse => ({
  token: 'demo-jwt-token-xyz',
  tokenType: 'Bearer',
  expiresIn: 3600000,
  accountId: '123e4567-e89b-12d3-a456-426614174000',
  names: email.includes('admin') ? 'Admin' : 'Usuario',
  lastnames: 'Demo',
  email: email,
  phone: '+573001234567',
  createdAt: new Date().toISOString(),
  hasMasterKey: false,
});

/**
 * @function storeAuthData
 * @description Persiste todos los campos de una respuesta de autenticación en el
 *              almacenamiento seguro usando las claves definidas en {@link STORAGE_KEYS}.
 *              Es utilizada internamente por {@link login} y {@link register}.
 *
 * @param {AuthResponse} authData - Datos de autenticación a almacenar.
 * @returns {Promise<void>}
 */
const storeAuthData = async (authData: AuthResponse): Promise<void> => {
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

/**
 * @function login
 * @description Autentica a un usuario con su correo y contraseña.
 *              En modo demo valida contra {@link DEMO_CREDENTIALS} con un retardo
 *              simulado de 800 ms. Si las credenciales son correctas, almacena
 *              los datos de sesión y retorna el {@link AuthResponse}.
 *
 * @param {LoginRequest} credentials - Objeto con el email y contraseña del usuario.
 * @returns {Promise<AuthResponse>} Datos de la sesión autenticada.
 *
 * @throws {Error} Si las credenciales no coinciden con las de demo.
 * @throws {Error} Si `DEMO_MODE` es `false` y el backend no está disponible.
 *
 * @example
 * const session = await login({ email: 'admin@desarrollo.com', password: 'Admin123!' });
 */
export const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
  console.log('🔐 Intentando login con:', credentials.email);

  if (DEMO_MODE) {
    await new Promise(resolve => setTimeout(resolve, 800)); // simula latencia de red

    if (
      credentials.email === DEMO_CREDENTIALS.email &&
      credentials.password === DEMO_CREDENTIALS.password
    ) {
      console.log('✅ Login demo exitoso');
      const authData = getDemoAuthResponse(credentials.email);
      await storeAuthData(authData);
      return authData;
    } else {
      throw new Error('Credenciales inválidas. Usa: admin@desarrollo.com / Admin123!');
    }
  }

  throw new Error('Backend no disponible');
};

/**
 * @function register
 * @description Registra una nueva cuenta de usuario.
 *              En modo demo genera una respuesta ficticia con un retardo simulado
 *              de 800 ms, almacena los datos de sesión y retorna el {@link AuthResponse}.
 *
 * @param {RegisterRequest} userData - Objeto con los datos del nuevo usuario.
 * @returns {Promise<AuthResponse>} Datos de la sesión creada.
 *
 * @throws {Error} Si `DEMO_MODE` es `false` y el backend no está disponible.
 *
 * @example
 * const session = await register({ names: 'Ana', email: '...', password: '...' });
 */
export const register = async (userData: RegisterRequest): Promise<AuthResponse> => {
  if (DEMO_MODE) {
    await new Promise(resolve => setTimeout(resolve, 800)); // simula latencia de red
    const authData = getDemoAuthResponse(userData.email);
    await storeAuthData(authData);
    return authData;
  }

  throw new Error('Backend no disponible');
};

/**
 * @function logout
 * @description Elimina todos los datos de sesión del almacenamiento seguro,
 *              cerrando efectivamente la sesión del usuario actual.
 *
 * @returns {Promise<void>}
 *
 * @example
 * await logout();
 */
export const logout = async (): Promise<void> => {
  await storage.deleteItem(STORAGE_KEYS.TOKEN);
  await storage.deleteItem(STORAGE_KEYS.TOKEN_TYPE);
  await storage.deleteItem(STORAGE_KEYS.EXPIRES_IN);
  await storage.deleteItem(STORAGE_KEYS.ACCOUNT_ID);
  await storage.deleteItem(STORAGE_KEYS.USER_EMAIL);
  await storage.deleteItem(STORAGE_KEYS.USER_NAMES);
  await storage.deleteItem(STORAGE_KEYS.USER_LASTNAMES);
  await storage.deleteItem(STORAGE_KEYS.USER_PHONE);
  await storage.deleteItem(STORAGE_KEYS.HAS_MASTER_KEY);
};

/**
 * @function isAuthenticated
 * @description Verifica si existe una sesión activa comprobando la presencia
 *              del token de autenticación en el almacenamiento seguro.
 *
 * @returns {Promise<boolean>} `true` si hay un token almacenado, `false` en caso contrario.
 *
 * @example
 * const active = await isAuthenticated();
 * if (active) { ... }
 */
export const isAuthenticated = async (): Promise<boolean> => {
  const token = await storage.getItem(STORAGE_KEYS.TOKEN);
  return token !== null;
};

/**
 * @function getUserData
 * @description Reconstruye los datos del usuario autenticado leyendo cada campo
 *              individualmente desde el almacenamiento seguro.
 *              Retorna `null` si no existe un `accountId` almacenado,
 *              lo que indica que no hay sesión activa.
 *
 * @returns {Promise<Partial<AuthResponse> | null>} Datos parciales del usuario o `null`.
 *
 * @example
 * const user = await getUserData();
 * if (user) console.log(user.names);
 */
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