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

export interface MasterKeyStatusResponse {
  hasMasterKey: boolean;
  sessionActive?: boolean;
}

export const securityApi = {
  // Master Key
  createMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<void>>('/api/security/master-key', { masterKey }),
  verifyMasterKey: (masterKey: string) =>
    apiClient.post<ApiResponse<void>>('/api/security/master-key/verify', { masterKey }),
  changeMasterKey: (currentMasterKey: string, newMasterKey: string) =>
    apiClient.patch<ApiResponse<void>>('/api/security/master-key', { currentMasterKey, newMasterKey }),
  clearMasterKeySession: () =>
    apiClient.delete<ApiResponse<void>>('/api/security/master-key/session'),
  getMasterKeyStatus: () =>
    apiClient.get<ApiResponse<MasterKeyStatusResponse>>('/api/security/master-key/status'),

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
