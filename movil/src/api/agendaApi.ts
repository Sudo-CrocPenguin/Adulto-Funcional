import apiClient from './client';
import { ApiResponse } from '../types/auth.types';

export interface Event {
  id: string;
  title: string;
  priority: 'BAJA' | 'MEDIA' | 'ALTA';
  eventDate: string;
  frequency: number; // 0=único, 1=diario, 7=semanal, 30=mensual, 365=anual
  reminder: string;
  startHour: string;
  endHour: string;
  description?: string;
  status: 'PENDIENTE' | 'COMPLETADO' | 'CANCELADO';
  category?: { id: string; name: string };
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
