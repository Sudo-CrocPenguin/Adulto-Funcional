import { useState, useEffect } from 'react';
import apiClient from '../api/client';
import { storage } from '../services/storage';
import { STORAGE_KEYS } from '../constants/config';

export interface Event {
  id: string;
  title: string;
  category?: string;
  frequency: string; // 'Única', 'Diaria', 'Semanal', 'Mensual'
  priority: 'Alta' | 'Media' | 'Baja';
  date: string; // ISO date
  reminder?: string;
  status: 'Pendiente' | 'Completado' | 'Cancelado';
}

// Datos de demostración
const demoEvents: Event[] = [
  { id: '1', title: 'Preparar presentación', category: 'Trabajo', frequency: 'Única', priority: 'Alta', date: '2026-05-15', status: 'Pendiente' },
  { id: '2', title: 'Pagar recibo de la luz', category: 'Hogar', frequency: 'Mensual', priority: 'Media', date: '2026-05-20', status: 'Pendiente' },
  { id: '3', title: 'Comprar despensa', category: 'Personal', frequency: 'Semanal', priority: 'Baja', date: '2026-05-10', status: 'Completado' },
];

export const useEvents = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
      // TODO: Reemplazar con llamada real cuando el endpoint /api/agenda/events esté listo
      // const response = await apiClient.get(`/api/agenda/events?accountId=${accountId}`);
      // setEvents(response.data.data);
      // Simulamos delay
      await new Promise(resolve => setTimeout(resolve, 500));
      setEvents(demoEvents);
    } catch (err: any) {
      setError(err.message);
      setEvents(demoEvents); // fallback a demo
    } finally {
      setLoading(false);
    }
  };

  const createEvent = async (eventData: Omit<Event, 'id'>) => {
    // TODO: Llamada POST a /api/agenda/events
    const newEvent = { ...eventData, id: Date.now().toString() };
    setEvents(prev => [newEvent, ...prev]);
    return newEvent;
  };

  const updateEvent = async (id: string, eventData: Partial<Event>) => {
    // TODO: Llamada PATCH a /api/agenda/events/{id}
    setEvents(prev => prev.map(e => e.id === id ? { ...e, ...eventData } : e));
  };

  const deleteEvent = async (id: string) => {
    // TODO: Llamada DELETE
    setEvents(prev => prev.filter(e => e.id !== id));
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  return { events, loading, error, fetchEvents, createEvent, updateEvent, deleteEvent };
};
