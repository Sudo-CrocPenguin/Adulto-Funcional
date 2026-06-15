import { useState, useEffect, useCallback } from 'react';
import { financesApi, FixedExpense } from '../api/financesApi';
import { useMovements } from './useMovements';

export const useFixedExpenses = () => {
  const [expenses, setExpenses] = useState<FixedExpense[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { createMovement } = useMovements();

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

  const createExpense = async (data: Omit<FixedExpense, 'id'>) => {
    const payload = {
      name: data.name,
      frequency: data.frequency,
      amount: data.amount,
      status: data.status,
      nextDueDate: data.nextDueDate,
      categoryId: data.category?.id,
    };
    const response = await financesApi.createFixedExpense(payload);
    const newExpense = response.data.data;
    setExpenses(prev => [newExpense, ...prev]);
    return newExpense;
  };

  const updateExpense = async (id: string, data: Partial<FixedExpense>) => {
    const payload: any = { ...data };
    if (data.category) {
      payload.categoryId = data.category.id;
      delete payload.category;
    }
    const response = await financesApi.updateFixedExpense(id, payload);
    const updated = response.data.data;
    setExpenses(prev => prev.map(e => e.id === id ? updated : e));
    return updated;
  };

  const deleteExpense = async (id: string) => {
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
