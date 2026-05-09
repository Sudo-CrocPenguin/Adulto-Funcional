import { useEffect, useState } from 'react';
import { useMovements } from './useMovements';
import { useFixedExpenses } from './useFixedExpenses';
import { useEvents } from './useEvents';

export const useDashboard = () => {
  const { movements, loading: movementsLoading } = useMovements();
  const { expenses, loading: expensesLoading } = useFixedExpenses();
  const { events, loading: eventsLoading } = useEvents();

  const [data, setData] = useState({
    totalIncome: 0,
    totalExpense: 0,
    balance: 0,
    upcomingFixedExpenses: [] as any[],
    upcomingEvents: [] as any[],
    streak: 7,
    chartData: { labels: [] as string[], incomes: [] as number[], expenses: [] as number[] },
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (movementsLoading || expensesLoading || eventsLoading) return;

    const totalIncome = movements.filter(m => m.movementType === 'INCOME').reduce((s, m) => s + m.amount, 0);
    const totalExpense = movements.filter(m => m.movementType === 'EXPENSE').reduce((s, m) => s + m.amount, 0);
    const balance = totalIncome - totalExpense;

    const upcomingFixedExpenses = expenses
      .filter(e => new Date(e.nextDueDate) >= new Date())
      .sort((a, b) => new Date(a.nextDueDate).getTime() - new Date(b.nextDueDate).getTime())
      .slice(0, 3);

    const upcomingEvents = events
      .filter(e => e.status !== 'COMPLETADO')
      .sort((a, b) => new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime())
      .slice(0, 3);

    // Últimos 3 meses
    const today = new Date();
    const labels = [];
    const incomesByMonth = [];
    const expensesByMonth = [];

    for (let i = 2; i >= 0; i--) {
      const month = new Date(today.getFullYear(), today.getMonth() - i, 1);
      labels.push(month.toLocaleString('default', { month: 'short' }));
      const monthMovements = movements.filter(m => {
        const d = new Date(m.movementDate);
        return d.getMonth() === month.getMonth() && d.getFullYear() === month.getFullYear();
      });
      incomesByMonth.push(monthMovements.filter(m => m.movementType === 'INCOME').reduce((s, m) => s + m.amount, 0));
      expensesByMonth.push(monthMovements.filter(m => m.movementType === 'EXPENSE').reduce((s, m) => s + m.amount, 0));
    }

    setData({
      totalIncome, totalExpense, balance,
      upcomingFixedExpenses, upcomingEvents, streak: 7,
      chartData: { labels, incomes: incomesByMonth, expenses: expensesByMonth },
    });
    setLoading(false);
  }, [movements, expenses, events, movementsLoading, expensesLoading, eventsLoading]);

  return { data, loading, error: null };
};
