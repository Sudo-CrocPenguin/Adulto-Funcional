import axios from 'axios';

/**
 * Instancia de Axios preconfigurada para comunicación con el backend Spring Boot.
 * 
 * Base URL: http://localhost:8080/api
 * Producción: https://audry-subsphenoidal-bovinely.ngrok-free.dev/api
 * 
 * Características:
 * - URL base configurada una sola vez
 * - Token JWT agregado automáticamente desde sessionStorage
 * - Redirección automática al login si el token expira (401)
 * - withCredentials: true para cookies HttpOnly
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  
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
    const token = sessionStorage.getItem('token'); 
    if (token) {
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
    return Promise.reject(error);
  }
);

export default api;