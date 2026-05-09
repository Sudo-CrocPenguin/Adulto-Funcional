import { useState, useEffect, useCallback } from 'react';
import { agendaApi, Event } from '../api/agendaApi';

const formatPriority = (priority: 'ALTA' | 'MEDIA' | 'BAJA'): string => {
  switch (priority) {
    case 'ALTA': return 'Alta';
    case 'MEDIA': return 'Media';
    case 'BAJA': return 'Baja';
    default: return 'Media';
  }
};

const formatStatus = (status: string): string => {
  switch (status.toUpperCase()) {
    case 'PENDIENTE': return 'Pendiente';
    case 'COMPLETADO': return 'Completado';
    case 'CANCELADO': return 'Cancelado';
    default: return 'Pendiente';
  }
};

export const useEvents = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      const response = await agendaApi.getEvents();
      setEvents(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const createEvent = async (data: Omit<Event, 'id'>) => {
    const payload = {
      title: data.title.trim(),
      priority: formatPriority(data.priority),
      eventDate: data.eventDate,
      frequency: data.frequency,
      reminder: data.reminder,
      startHour: data.startHour,
      endHour: data.endHour,
      description: data.description || '',
      status: formatStatus(data.status),
      categoryId: data.category?.id,
    };
    const response = await agendaApi.createEvent(payload as any);
    const newEvent = response.data.data;
    // Agregar al estado local (optimista)
    setEvents(prev => [newEvent, ...prev]);
    return newEvent;
  };

  const updateEvent = async (id: string, data: Partial<Event>) => {
    const payload = { ...data };
    if (data.priority) payload.priority = formatPriority(data.priority);
    if (data.status) payload.status = formatStatus(data.status);
    if (data.category) payload.categoryId = data.category.id;
    delete (payload as any).category;
    const response = await agendaApi.updateEvent(id, payload);
    const updatedEvent = response.data.data;
    setEvents(prev => prev.map(e => e.id === id ? updatedEvent : e));
    return updatedEvent;
  };

  const deleteEvent = async (id: string) => {
    await agendaApi.deleteEvent(id);
    setEvents(prev => prev.filter(e => e.id !== id));
  };

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  return { events, loading, error, fetchEvents, createEvent, updateEvent, deleteEvent };
};
