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
    const hasUsableToken = token && token !== 'undefined' && token !== 'null';

    if (hasUsableToken && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers['X-Client-Type'] = 'mobile';
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      await storage.deleteItem(STORAGE_KEYS.TOKEN);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
