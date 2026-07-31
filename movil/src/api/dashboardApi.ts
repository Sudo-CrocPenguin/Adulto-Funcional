/**
 * API para datos del dashboard (resúmenes, estadísticas).
 * Actualmente usa datos simulados hasta que los endpoints reales estén listos.
 *
 * @author Miguel Angel Blandon Montes
 */

import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export interface DashboardSummary {
  currentBalance: number;
  pendingEvents: number;
  upcomingFixedExpenses: number;
  savedPasswords: number;
  streakDays: number;
}

export interface MonthlyData {
  month: string;
  income: number;
  expense: number;
  leisure: number;
  savings: number;
}

export interface UpcomingItem {
  id: string;
  name: string;
  category: string;
  date: string;
  amount?: number;
  status?: string;
}

// Datos simulados (mientras no hay backend)
const mockSummary: DashboardSummary = {
  currentBalance: 2500.00,
  pendingEvents: 8,
  upcomingFixedExpenses: 3,
  savedPasswords: 12,
  streakDays: 7,
};

const mockMonthlyData: MonthlyData[] = [
  { month: 'Ene', income: 3000, expense: 1800, leisure: 400, savings: 800 },
  { month: 'Feb', income: 3200, expense: 2000, leisure: 500, savings: 700 },
  { month: 'Mar', income: 3500, expense: 2200, leisure: 600, savings: 700 },
];

const mockUpcomingFixedExpenses: UpcomingItem[] = [
  { id: '1', name: 'Internet', category: 'Servicios', date: '2026-05-10', amount: 45, status: 'Pendiente' },
  { id: '2', name: 'Netflix', category: 'Suscripción', date: '2026-05-15', amount: 15, status: 'Pendiente' },
];

const mockUpcomingEvents: UpcomingItem[] = [
  { id: '1', name: 'Reunión con equipo', category: 'Trabajo', date: '2026-05-08' },
  { id: '2', name: 'Pagar factura luz', category: 'Hogar', date: '2026-05-12' },
];

export const getDashboardSummary = async (accountId: string): Promise<DashboardSummary> => {
  // TODO: Reemplazar con llamada real cuando exista el endpoint
  // const response = await apiClient.get<ApiResponse<DashboardSummary>>(`/api/dashboard/${accountId}/summary`);
  // return response.data.data;
  console.log('Obteniendo resumen para cuenta:', accountId);
  return mockSummary;
};

export const getMonthlyStats = async (accountId: string): Promise<MonthlyData[]> => {
  // TODO: Reemplazar con endpoint real
  return mockMonthlyData;
};

export const getUpcomingFixedExpenses = async (accountId: string): Promise<UpcomingItem[]> => {
  // TODO: Reemplazar con endpoint real
  return mockUpcomingFixedExpenses;
};

export const getUpcomingEvents = async (accountId: string): Promise<UpcomingItem[]> => {
  // TODO: Reemplazar con endpoint real
  return mockUpcomingEvents;
};
