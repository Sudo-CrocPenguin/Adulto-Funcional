import apiClient from './client';
import { API_ENDPOINTS } from '../constants/config';
import { ApiResponse } from '../types/auth.types';

export interface AccountResponse {
  id: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
}

export interface UpdateAccountRequest {
  names: string;
  lastnames: string;
  phone: string;
  email: string;
}

export const getAccount = async (accountId: string): Promise<AccountResponse> => {
  const response = await apiClient.get<ApiResponse<AccountResponse>>(API_ENDPOINTS.ACCOUNT.GET(accountId));
  return response.data.data;
};

export const updateAccount = async (accountId: string, data: UpdateAccountRequest): Promise<AccountResponse> => {
  const response = await apiClient.patch<ApiResponse<AccountResponse>>(API_ENDPOINTS.ACCOUNT.UPDATE(accountId), data);
  return response.data.data;
};
