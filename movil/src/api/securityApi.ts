import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export interface PasswordEntry {
  id: string;
  applicationName: string;
  password: string; // viene desencriptada
  category?: string;
  lastChangeDate: string;
}

export const securityApi = {
  // Master Key
  hasMasterKey: () => apiClient.get<ApiResponse<{ hasMasterKey: boolean }>>('/api/security/master-key/status'),
  createMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<void>>('/api/security/master-key', { masterKey }),
  verifyMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<{ verified: boolean }>>('/api/security/master-key/verify', { masterKey }),

  // Passwords
  getPasswords: () =>
    apiClient.get<ApiResponse<PasswordEntry[]>>('/api/security/passwords'),
  getPassword: (id: string) =>
    apiClient.get<ApiResponse<PasswordEntry>>(`/api/security/passwords/${id}`),
  createPassword: (data: Omit<PasswordEntry, 'id'>) =>
    apiClient.post<ApiResponse<PasswordEntry>>('/api/security/passwords', data),
  updatePassword: (id: string, data: Partial<PasswordEntry>) =>
    apiClient.patch<ApiResponse<PasswordEntry>>(`/api/security/passwords/${id}`, data),
  deletePassword: (id: string) =>
    apiClient.delete<ApiResponse<void>>(`/api/security/passwords/${id}`),
};
