import { useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { financesApi, FixedExpense } from '../api/financesApi';
import { useMovements } from './useMovements';
import { useNotifications } from './useNotifications';

export const useFixedExpenses = () => {
  const [expenses, setExpenses] = useState<FixedExpense[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { createMovement } = useMovements();
  const { scheduleForItem, cancelForItem } = useNotifications();

  const fetchExpenses = useCallback(async () => {
    try {
      setLoading(true);
      const response = await financesApi.getFixedExpenses();
      setExpenses(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const getNotificationKey = (id: string) => `fixedExpenseNotifications_${id}`;

  const loadNotificationIds = async (id: string): Promise<string[]> => {
    const stored = await AsyncStorage.getItem(getNotificationKey(id));
    return stored ? JSON.parse(stored) : [];
  };

  const saveNotificationIds = async (id: string, ids: string[]) => {
    await AsyncStorage.setItem(getNotificationKey(id), JSON.stringify(ids));
  };

  const createExpense = async (data: Omit<FixedExpense, 'id'>) => {
    const statusValue = data.status === 'ACTIVO' ? 'ACTIVE' : 
                        data.status === 'INACTIVO' ? 'INACTIVE' : data.status;
    const payload = {
      name: data.name,
      frequency: data.frequency,
      amount: data.amount,
      status: statusValue,
      nextDueDate: data.nextDueDate,
      categoryId: data.category?.id,
    };
    const response = await financesApi.createFixedExpense(payload as any);
    const newExpense = response.data.data;
    
    // Programar notificaciones
    const dueDate = new Date(newExpense.nextDueDate);
    const dueDateTime = new Date(dueDate);
    dueDateTime.setHours(12, 0, 0, 0); // Hora por defecto al mediodía
    const notificationIds = await scheduleForItem(newExpense.id, 'fixedExpense', newExpense.name, dueDateTime);
    await saveNotificationIds(newExpense.id, notificationIds);
    
    setExpenses(prev => [newExpense, ...prev]);
    return newExpense;
  };

  const updateExpense = async (id: string, data: Partial<FixedExpense>) => {
    const payload: any = { ...data };
    if (data.status) {
      payload.status = data.status === 'ACTIVO' ? 'ACTIVE' : 
                       data.status === 'INACTIVO' ? 'INACTIVE' : data.status;
    }
    if (data.category) {
      payload.categoryId = data.category.id;
      delete payload.category;
    }
    const response = await financesApi.updateFixedExpense(id, payload);
    const updated = response.data.data;
    
    // Reprogramar notificaciones si cambió la fecha
    if (data.nextDueDate) {
      const oldIds = await loadNotificationIds(id);
      await cancelForItem(oldIds);
      const dueDate = new Date(updated.nextDueDate);
      const dueDateTime = new Date(dueDate);
      dueDateTime.setHours(12, 0, 0, 0);
      const newIds = await scheduleForItem(id, 'fixedExpense', updated.name, dueDateTime);
      await saveNotificationIds(id, newIds);
    }
    
    setExpenses(prev => prev.map(e => e.id === id ? updated : e));
    return updated;
  };

  const deleteExpense = async (id: string) => {
    const notificationIds = await loadNotificationIds(id);
    await cancelForItem(notificationIds);
    await AsyncStorage.removeItem(getNotificationKey(id));
    await financesApi.deleteFixedExpense(id);
    setExpenses(prev => prev.filter(e => e.id !== id));
  };

  const markAsPaid = async (expense: FixedExpense) => {
    await createMovement({
      movementType: 'EXPENSE',
      amount: expense.amount,
      movementDate: new Date().toISOString().split('T')[0],
      description: `Pago: ${expense.name}`,
      category: expense.category,
    });

    const currentDue = new Date(expense.nextDueDate);
    let nextDueDate = new Date(currentDue);
    switch (expense.frequency) {
      case 'DAILY': nextDueDate.setDate(currentDue.getDate() + 1); break;
      case 'WEEKLY': nextDueDate.setDate(currentDue.getDate() + 7); break;
      case 'BIWEEKLY': nextDueDate.setDate(currentDue.getDate() + 14); break;
      case 'MONTHLY': nextDueDate.setMonth(currentDue.getMonth() + 1); break;
      case 'QUARTERLY': nextDueDate.setMonth(currentDue.getMonth() + 3); break;
      case 'SEMIANNUAL': nextDueDate.setMonth(currentDue.getMonth() + 6); break;
      case 'ANNUAL': nextDueDate.setFullYear(currentDue.getFullYear() + 1); break;
      default: break;
    }

    await updateExpense(expense.id, {
      nextDueDate: nextDueDate.toISOString().split('T')[0],
      status: 'ACTIVE',
    });
    await fetchExpenses();
  };

  useEffect(() => {
    fetchExpenses();
  }, [fetchExpenses]);

  return { expenses, loading, error, fetchExpenses, createExpense, updateExpense, deleteExpense, markAsPaid };
};
