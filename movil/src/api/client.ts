import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const token = await storage.getItem(STORAGE_KEYS.TOKEN);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Client-Type'] = 'mobile';
    console.log(`📡 ${config.method?.toUpperCase()} ${config.url}`);
    console.log('🔑 Headers enviados:', config.headers);
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    console.error('❌ Respuesta error:', error.response?.status, error.response?.data);
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.error('❌ Error 401/403, eliminando token');
      await storage.deleteItem(STORAGE_KEYS.TOKEN);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
