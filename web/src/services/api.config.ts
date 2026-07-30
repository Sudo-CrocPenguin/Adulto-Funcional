import axios from 'axios';

/**
 * Instancia de Axios preconfigurada para comunicación con el backend Spring Boot.
 * 
 * Base URL local por defecto: http://localhost:8080/api
 * Producción: configurar VITE_API_URL con la URL pública del backend + /api
 * 
 * Características:
 * - URL base configurada una sola vez
 * - Cookie HttpOnly enviada automáticamente con withCredentials
 * - Token JWT agregado solo si existe una copia valida en storage
 * - Redirección automática al login si el token expira (401)
 * - withCredentials: true para cookies HttpOnly
 */
const api = axios.create({
  baseURL: (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api').replace(/\/+$/, ''),
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

/**
 * Interceptor de solicitudes.
 * Agrega el token JWT automáticamente a todas las peticiones.
 */
api.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('token') ?? localStorage.getItem('token')
    const hasUsableToken = token && token !== 'undefined' && token !== 'null'

    if (hasUsableToken) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Interceptor de respuestas.
 * Si el backend responde 401 (no autorizado), limpia sesión y redirige.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('accountId');
      sessionStorage.removeItem('names');
      sessionStorage.removeItem('masterKeyVerified');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('accountId');
      localStorage.removeItem('names');
      localStorage.removeItem('authPersistence');
      localStorage.removeItem('masterKeyVerified');
    }
    return Promise.reject(error);
  }
);

export default api;
