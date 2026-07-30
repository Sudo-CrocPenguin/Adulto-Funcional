import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export type EventPriority = 'Baja' | 'Media' | 'Alta';
export type EventStatus = 'Pendiente' | 'Completado' | 'Cancelado' | 'Pospuesto';

export interface Event {
  id: string;
  title: string;
  priority: EventPriority;
  eventDate: string;
  frequency: number; // 0=único, 1=diario, 7=semanal, 30=mensual, 365=anual
  reminder: string;
  startHour: string;
  endHour: string;
  description?: string;
  status: EventStatus;
  category?: { id: string; name: string };
  streak?: number;          // racha (solo para recurrentes)
  lastCompletionDate?: string; // última vez que se completó (ISO)
}

export const agendaApi = {
  getEvents: (params?: { status?: string; priority?: string; categoryId?: string }) =>
    apiClient.get<ApiResponse<Event[]>>('/api/agenda/events', { params }),
  createEvent: (data: Omit<Event, 'id'>) =>
    apiClient.post<ApiResponse<Event>>('/api/agenda/events', data),
  updateEvent: (id: string, data: Partial<Event>) =>
    apiClient.patch<ApiResponse<Event>>(`/api/agenda/events/${id}`, data),
  deleteEvent: (id: string) =>
    apiClient.delete<ApiResponse<void>>(`/api/agenda/events/${id}`),
};
