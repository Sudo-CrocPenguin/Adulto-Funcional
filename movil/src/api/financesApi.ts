import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export interface Movement {
  id: string;
  movementType: 'INCOME' | 'EXPENSE';
  amount: number;
  movementDate: string;
  description?: string;
  category?: { id: string; name: string };
}

export type FixedExpenseFrequency =
  | 'WEEKLY'
  | 'BIWEEKLY'
  | 'MONTHLY'
  | 'QUARTERLY'
  | 'SEMIANNUAL'
  | 'ANNUAL';

export type FixedExpenseStatus = 'ACTIVE' | 'INACTIVE';

export interface FixedExpense {
  id: string;
  name: string;
  category?: { id: string; name: string };
  frequency: FixedExpenseFrequency;
  amount: number;
  nextDueDate: string;
  status: FixedExpenseStatus;
}

export interface CreateFixedExpenseRequest {
  name: string;
  categoryId?: string;
  frequency: FixedExpenseFrequency;
  amount: number;
  nextDueDate: string;
  status: FixedExpenseStatus;
}

export type UpdateFixedExpenseRequest = Partial<CreateFixedExpenseRequest>;

export interface Category {
  id: string;
  name: string;
  type: 'FINANCES' | 'AGENDA';
}

export const financesApi = {
  // Movements
  getMovements: (params?: { startDate?: string; endDate?: string; movementType?: string }) =>
    apiClient.get<ApiResponse<Movement[]>>('/api/finances/movements', { params }),
  createMovement: (data: Omit<Movement, 'id'>) =>
    apiClient.post<ApiResponse<Movement>>('/api/finances/movements', data),
  updateMovement: (id: string, data: Partial<Movement>) =>
    apiClient.patch<ApiResponse<Movement>>(`/api/finances/movements/${id}`, data),
  deleteMovement: (id: string) =>
    apiClient.delete<ApiResponse<void>>(`/api/finances/movements/${id}`),

  // Fixed Expenses
  getFixedExpenses: (params?: { status?: string; categoryId?: string }) =>
    apiClient.get<ApiResponse<FixedExpense[]>>('/api/finances/fixed-expenses', { params }),
  createFixedExpense: (data: CreateFixedExpenseRequest) =>
    apiClient.post<ApiResponse<FixedExpense>>('/api/finances/fixed-expenses', data),
  updateFixedExpense: (id: string, data: UpdateFixedExpenseRequest) =>
    apiClient.patch<ApiResponse<FixedExpense>>(`/api/finances/fixed-expenses/${id}`, data),
  deleteFixedExpense: (id: string) =>
    apiClient.delete<ApiResponse<void>>(`/api/finances/fixed-expenses/${id}`),

  // Categories
  getCategories: () =>
    apiClient.get<ApiResponse<Category[]>>('/api/finances/categories'),
  createCategory: (data: { name: string; type: 'FINANCES' | 'AGENDA' }) =>
    apiClient.post<ApiResponse<Category>>('/api/finances/categories', data),
};
