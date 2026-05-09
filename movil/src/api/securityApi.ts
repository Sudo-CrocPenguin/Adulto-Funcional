import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export interface PasswordEntry {
  id: string;
  applicationName: string;
  password?: string;
  lastChangeDate: string;
}

export interface CreatePasswordRequest {
  applicationName: string;
  password: string;
  lastChangeDate?: string;
}

export interface UpdatePasswordRequest {
  applicationName?: string;
  password?: string;
  lastChangeDate?: string;
}

export const securityApi = {
  // Master Key
  createMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<void>>('/api/security/master-key', { masterKey }),
  verifyMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<void>>('/api/security/passwords/master-key/verify', { masterKey }),
  getMasterKeyStatus: () =>
    apiClient.get<ApiResponse<{ hasMasterKey: boolean }>>('/api/security/master-key/status'),

  // Passwords CRUD
  getPasswords: () =>
    apiClient.get<ApiResponse<PasswordEntry[]>>('/api/security/passwords'),
  getPassword: (id: string) =>
    apiClient.get<ApiResponse<PasswordEntry>>(`/api/security/passwords/${id}`),
  createPassword: (data: CreatePasswordRequest) =>
    apiClient.post<ApiResponse<PasswordEntry>>('/api/security/passwords', data),
  updatePassword: (id: string, data: UpdatePasswordRequest) =>
    apiClient.patch<ApiResponse<PasswordEntry>>(`/api/security/passwords/${id}`, data),
  deletePassword: (id: string) =>
    apiClient.delete<ApiResponse<void>>(`/api/security/passwords/${id}`),
};
