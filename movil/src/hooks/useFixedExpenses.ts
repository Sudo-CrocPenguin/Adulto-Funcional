import { useState, useEffect } from 'react';
import { storage } from '../services/storage';
import { STORAGE_KEYS } from '../constants/config';

export interface FixedExpense {
  id: string;
  name: string;
  category: string;
  frequency: 'Diario' | 'Semanal' | 'Mensual' | 'Anual';
  amount: number;
  nextDueDate: string;
  status: 'Activo' | 'Inactivo';
}

const demoFixedExpenses: FixedExpense[] = [
  { id: '1', name: 'Gimnasio', category: 'Salud', frequency: 'Mensual', amount: 45, nextDueDate: '2026-02-27', status: 'Activo' },
  { id: '2', name: 'Alquiler', category: 'Vivienda', frequency: 'Mensual', amount: 500, nextDueDate: '2026-03-05', status: 'Activo' },
  { id: '3', name: 'Netflix', category: 'Suscripción', frequency: 'Mensual', amount: 15.99, nextDueDate: '2026-03-05', status: 'Pagado' },
];

export const useFixedExpenses = () => {
  const [expenses, setExpenses] = useState<FixedExpense[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
      await new Promise(resolve => setTimeout(resolve, 500));
      setExpenses(demoFixedExpenses);
    } catch (err: any) {
      setError(err.message);
      setExpenses(demoFixedExpenses);
    } finally {
      setLoading(false);
    }
  };

  const createExpense = async (expense: Omit<FixedExpense, 'id'>) => {
    const newExpense = { ...expense, id: Date.now().toString() };
    setExpenses(prev => [newExpense, ...prev]);
    return newExpense;
  };

  const updateExpense = async (id: string, data: Partial<FixedExpense>) => {
    setExpenses(prev => prev.map(e => e.id === id ? { ...e, ...data } : e));
  };

  const deleteExpense = async (id: string) => {
    setExpenses(prev => prev.filter(e => e.id !== id));
  };

  useEffect(() => {
    fetchExpenses();
  }, []);

  return { expenses, loading, error, fetchExpenses, createExpense, updateExpense, deleteExpense };
};
