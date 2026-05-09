import { useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { agendaApi, Event } from '../api/agendaApi';
import { useNotifications } from './useNotifications';

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
  const { scheduleForItem, cancelForItem } = useNotifications();

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

  const getNotificationKey = (id: string) => `eventNotifications_${id}`;

  const loadNotificationIds = async (id: string): Promise<string[]> => {
    const stored = await AsyncStorage.getItem(getNotificationKey(id));
    return stored ? JSON.parse(stored) : [];
  };

  const saveNotificationIds = async (id: string, ids: string[]) => {
    await AsyncStorage.setItem(getNotificationKey(id), JSON.stringify(ids));
  };

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
    
    // Programar notificaciones (basadas en la hora de inicio)
    const eventDateTime = new Date(data.startHour);
    const notificationIds = await scheduleForItem(newEvent.id, 'event', data.title, eventDateTime);
    await saveNotificationIds(newEvent.id, notificationIds);
    
    setEvents(prev => [newEvent, ...prev]);
    return newEvent;
  };

  const updateEvent = async (id: string, data: Partial<Event>) => {
    const payload: any = { ...data };
    if (data.priority) payload.priority = formatPriority(data.priority);
    if (data.status) payload.status = formatStatus(data.status);
    if (data.category) payload.categoryId = data.category.id;
    delete payload.category;
    const response = await agendaApi.updateEvent(id, payload);
    const updated = response.data.data;
    
    // Reprogramar notificaciones si cambió la hora
    if (data.startHour) {
      const oldIds = await loadNotificationIds(id);
      await cancelForItem(oldIds);
      const eventDateTime = new Date(data.startHour);
      const newIds = await scheduleForItem(id, 'event', updated.title, eventDateTime);
      await saveNotificationIds(id, newIds);
    }
    
    setEvents(prev => prev.map(e => e.id === id ? updated : e));
    return updated;
  };

  const deleteEvent = async (id: string) => {
    const notificationIds = await loadNotificationIds(id);
    await cancelForItem(notificationIds);
    await AsyncStorage.removeItem(getNotificationKey(id));
    await agendaApi.deleteEvent(id);
    setEvents(prev => prev.filter(e => e.id !== id));
  };

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  return { events, loading, error, fetchEvents, createEvent, updateEvent, deleteEvent };
};
