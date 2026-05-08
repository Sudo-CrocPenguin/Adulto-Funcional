import { useEffect, useState } from 'react';

export const useDashboard = () => {
  const [data, setData] = useState({
    totalIncome: 3500,
    totalExpense: 1000,
    balance: 2500,
    upcomingFixedExpenses: [
      { id: '1', name: 'Gimnasio', amount: 45, nextDueDate: '2026-05-15', status: 'ACTIVE' },
      { id: '2', name: 'Netflix', amount: 15.99, nextDueDate: '2026-05-10', status: 'ACTIVE' },
      { id: '3', name: 'Alquiler', amount: 500, nextDueDate: '2026-05-20', status: 'ACTIVE' },
    ],
    upcomingEvents: [
      { id: '1', title: 'Reunión con equipo', eventDate: '2026-05-08', status: 'PENDIENTE' },
      { id: '2', title: 'Pagar recibo luz', eventDate: '2026-05-12', status: 'PENDIENTE' },
    ],
    recentMovements: [],
    streak: 7,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  return { data, loading, error };
};
