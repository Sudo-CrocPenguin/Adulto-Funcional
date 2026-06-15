import { useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { agendaApi, Event } from '../api/agendaApi';

export type { Event } from '../api/agendaApi';

const STREAK_KEY = 'event_streaks';
const COMPLETION_KEY = 'event_last_completion';

const formatPriority = (priority: string): string => {
  const normalized = priority.toUpperCase();
  if (normalized === 'ALTA') return 'Alta';
  if (normalized === 'MEDIA') return 'Media';
  if (normalized === 'BAJA') return 'Baja';
  return priority || 'Media';
};

const formatStatus = (status: string): string => {
  const normalized = status.toUpperCase();
  if (normalized === 'PENDIENTE') return 'Pendiente';
  if (normalized === 'COMPLETADO') return 'Completado';
  if (normalized === 'CANCELADO') return 'Cancelado';
  if (normalized === 'POSPUESTO') return 'Pospuesto';
  return status || 'Pendiente';
};

// Calcular próxima fecha según frecuencia
const getNextDueDate = (currentDueDate: string, frequency: number): string => {
  const date = new Date(currentDueDate);
  date.setDate(date.getDate() + frequency);
  return date.toISOString().split('T')[0];
};

export const useEvents = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Cargar rachas y últimas fechas de completado desde AsyncStorage
  const loadStreaks = async () => {
    try {
      const streaksJson = await AsyncStorage.getItem(STREAK_KEY);
      const streaks = streaksJson ? JSON.parse(streaksJson) : {};
      const completionsJson = await AsyncStorage.getItem(COMPLETION_KEY);
      const completions = completionsJson ? JSON.parse(completionsJson) : {};
      return { streaks, completions };
    } catch {
      return { streaks: {}, completions: {} };
    }
  };

  const saveStreaks = async (streaks: any) => {
    await AsyncStorage.setItem(STREAK_KEY, JSON.stringify(streaks));
  };

  const saveCompletions = async (completions: any) => {
    await AsyncStorage.setItem(COMPLETION_KEY, JSON.stringify(completions));
  };

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      const response = await agendaApi.getEvents();
      const fetchedEvents = response.data.data;
      const { streaks, completions } = await loadStreaks();

      // Verificar si algún evento recurrente se ha saltado (próxima fecha pasada sin completar)
      const today = new Date().toISOString().split('T')[0];
      let updatedStreaks = { ...streaks };
      let updatedCompletions = { ...completions };
      let needsSave = false;

      for (const event of fetchedEvents) {
        if (event.frequency > 0 && event.status !== 'Completado') {
          const lastCompletion = completions[event.id];
          let nextDue = event.eventDate;
          if (lastCompletion) {
            // Calcular la próxima fecha esperada después del último completado
            let tempDate = lastCompletion;
            let count = 1;
            while (count <= (streaks[event.id] || 0) + 1) {
              tempDate = getNextDueDate(tempDate, event.frequency);
              count++;
            }
            nextDue = tempDate;
          }
          if (nextDue < today) {
            // Se ha saltado: resetear racha
            updatedStreaks[event.id] = 0;
            updatedCompletions[event.id] = null;
            needsSave = true;
          }
        }
      }

      if (needsSave) {
        await saveStreaks(updatedStreaks);
        await saveCompletions(updatedCompletions);
      }

      // Fusionar rachas y última fecha completada con los eventos
      const eventsWithStreaks = fetchedEvents.map(event => ({
        ...event,
        streak: updatedStreaks[event.id] || 0,
        lastCompletionDate: updatedCompletions[event.id] || null,
      }));
      setEvents(eventsWithStreaks);
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
    // Inicializar racha en 0 si es recurrente
    if (newEvent.frequency > 0) {
      const { streaks, completions } = await loadStreaks();
      streaks[newEvent.id] = 0;
      completions[newEvent.id] = null;
      await saveStreaks(streaks);
      await saveCompletions(completions);
      newEvent.streak = 0;
    }
    setEvents(prev => [newEvent, ...prev]);
    return newEvent;
  };

  const updateEvent = async (id: string, data: Partial<Event>) => {
    const payload: any = { ...data };
    if (data.priority) payload.priority = formatPriority(data.priority);
    if (data.status) payload.status = formatStatus(data.status);
    if (data.category) payload.categoryId = data.category.id;
    delete payload.category;
    delete payload.streak; // no se envía al backend
    const response = await agendaApi.updateEvent(id, payload);
    const updated = response.data.data;
    setEvents(prev => prev.map(e => e.id === id ? { ...updated, streak: e.streak } : e));
    return updated;
  };

  const completeEvent = async (id: string) => {
    // Buscar evento actual
    const event = events.find(e => e.id === id);
    if (!event) throw new Error('Evento no encontrado');

    const today = new Date().toISOString().split('T')[0];
    const { streaks, completions } = await loadStreaks();

    let newStreak = (streaks[id] || 0) + 1;
    let newLastCompletion = today;

    // Si el evento es recurrente, actualizar su fecha (eventDate) y estado
    let updatedEventData: Partial<Event> = { status: 'Completado' };
    if (event.frequency > 0) {
      const nextDate = getNextDueDate(event.eventDate, event.frequency);
      updatedEventData = {
        status: 'Pendiente', // volver a pendiente para la siguiente ocurrencia
        eventDate: nextDate,
      };
    }

    // Guardar en storage local
    streaks[id] = newStreak;
    completions[id] = newLastCompletion;
    await saveStreaks(streaks);
    await saveCompletions(completions);

    // Actualizar en backend (sin streak)
    const payload: any = { ...updatedEventData };
    if (updatedEventData.status) payload.status = formatStatus(updatedEventData.status);
    if (updatedEventData.eventDate) payload.eventDate = updatedEventData.eventDate;
    const response = await agendaApi.updateEvent(id, payload);
    const backendUpdated = response.data.data;

    // Actualizar estado local
    setEvents(prev => prev.map(e =>
      e.id === id
        ? { ...backendUpdated, streak: newStreak, lastCompletionDate: newLastCompletion }
        : e
    ));
    return backendUpdated;
  };

  const deleteEvent = async (id: string) => {
    // Eliminar también datos locales
    const { streaks, completions } = await loadStreaks();
    delete streaks[id];
    delete completions[id];
    await saveStreaks(streaks);
    await saveCompletions(completions);
    await agendaApi.deleteEvent(id);
    setEvents(prev => prev.filter(e => e.id !== id));
  };

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  return {
    events,
    loading,
    error,
    fetchEvents,
    createEvent,
    updateEvent,
    completeEvent,
    deleteEvent,
  };
};
