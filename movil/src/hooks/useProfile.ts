import { useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { storage } from '../services/storage';
import { STORAGE_KEYS } from '../constants/config';
import { useMovements } from './useMovements';
import { useFixedExpenses } from './useFixedExpenses';
import { useEvents } from './useEvents';
import * as accountApi from '../api/accountApi';

const PASSWORD_COUNT_KEY = 'password_count';

export interface UserProfile {
  id: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
}

export const useProfile = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { events, loading: eventsLoading } = useEvents();
  const { expenses, loading: expensesLoading } = useFixedExpenses();
  const { movements, loading: movementsLoading } = useMovements();

  const [stats, setStats] = useState({
    completedEvents: 0,
    maxStreak: 0,
    passwordCount: 0,
    fixedExpensesCount: 0,
  });

  const fetchProfile = async () => {
    try {
      const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
      if (!accountId) throw new Error('No autenticado');
      const account = await accountApi.getAccount(accountId);
      setProfile(account);
      await storage.setItem(STORAGE_KEYS.USER_NAMES, account.names);
      await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, account.lastnames);
      await storage.setItem(STORAGE_KEYS.USER_EMAIL, account.email);
      await storage.setItem(STORAGE_KEYS.USER_PHONE, account.phone);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (data: Partial<UserProfile>) => {
    if (profile) {
      const updated = await accountApi.updateAccount(profile.id, {
        names: data.names ?? profile.names,
        lastnames: data.lastnames ?? profile.lastnames,
        phone: data.phone ?? profile.phone,
        email: data.email ?? profile.email,
      });
      setProfile(updated);
      await storage.setItem(STORAGE_KEYS.USER_NAMES, updated.names);
      await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, updated.lastnames);
      await storage.setItem(STORAGE_KEYS.USER_EMAIL, updated.email);
      await storage.setItem(STORAGE_KEYS.USER_PHONE, updated.phone);
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
    if (!accountId) throw new Error('No autenticado');
    await accountApi.changePassword(accountId, {
      currentPassword: oldPassword,
      newPassword,
    });
  };

  const deleteAccount = async () => {
    const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
    if (!accountId) throw new Error('No autenticado');
    await accountApi.deleteAccount(accountId);
  };

  useEffect(() => {
    const loadStats = async () => {
      if (eventsLoading || expensesLoading || movementsLoading) return;
      // Compromisos completados
      const completedEvents = events.filter(e => e.status === 'Completado').length;
      // Número de contraseñas guardadas
      const passwordCount = parseInt(await AsyncStorage.getItem(PASSWORD_COUNT_KEY) || '0', 10);
      // Número de gastos fijos registrados
      const fixedExpensesCount = expenses.length;
      // Racha máxima (por ahora, usamos un valor fijo o la racha actual del dashboard)
      // Podemos calcular la racha máxima de eventos completados consecutivos (simplificado)
      // Por simplicidad, mostraremos la racha actual del dashboard o 0
      const maxStreak = 7; // Placeholder, se puede calcular después

      setStats({
        completedEvents,
        maxStreak,
        passwordCount,
        fixedExpensesCount,
      });
    };
    loadStats();
  }, [events, expenses, eventsLoading, expensesLoading, movementsLoading]);

  useEffect(() => {
    fetchProfile();
  }, []);

  return { profile, loading, error, stats, fetchProfile, updateProfile, changePassword, deleteAccount };
};
