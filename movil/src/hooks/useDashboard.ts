import { useEffect, useState } from 'react';

// Datos de demostración fieles a los diseños
const DEMO_DATA = {
  balance: 2500,
  pendingCommitments: 8,
  upcomingExpensesCount: 3,
  passwordsCount: 12,
  streakDays: 7,
  streakDots: [7, 15, 23, 30],
  fixedExpenses: [
    { id: '1', name: 'Internet', dueDate: '24/Feb', amount: 45 },
    { id: '2', name: 'Netflix', dueDate: '10/Mar', amount: 15.99 },
  ],
  commitments: [
    { id: '1', title: 'Reunión con equipo', date: '24/Feb', status: 'pendiente' },
    { id: '2', title: 'Pagar recibo luz', date: '28/Feb', status: 'pendiente' },
  ],
  notifications: [
    { id: '1', type: 'Gastos Fijos', message: 'Tienes una notificación' },
    { id: '2', type: 'Finanzas', message: 'Tienes una notificación' },
  ],
  stats: {
    income: 3500,
    expense: 1000,
    leisure: 500,
    savings: 2000,
    monthlyData: [3500, 3000, 2500, 2000, 1500, 1000, 500, 0],
  },
};

export const useDashboard = () => {
  const [data, setData] = useState(DEMO_DATA);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  return { data, loading, error };
};
