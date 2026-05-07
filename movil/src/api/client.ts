/**
 * Cliente HTTP configurado con axios.
 * Incluye interceptores para agregar token JWT (desde SecureStore o cookie) y manejar errores.
 *
 * @author Miguel Angel Blandon Montes
 */

import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  withCredentials: true, // Permite enviar y recibir cookies HttpOnly
});

// Interceptor para añadir token al header Authorizationexport src/api/client.ts;
     //si existe en SecureStore
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const token = await storage.getItem(STORAGE_KEYS.TOKEN);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Opcional: añadir header para identificar cliente nativo
    if (config.headers) {
      config.headers['X-Client-Type'] = 'mobile';
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para manejar errores de autenticación (401)
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token inválido o expirado: limpiar almacenamiento
      await storage.deleteItem(STORAGE_KEYS.TOKEN);
      // TODO: Disparar evento de logout global (usar emitter o contexto)
    }
    return Promise.reject(error);
  }
);

export default apiClient;