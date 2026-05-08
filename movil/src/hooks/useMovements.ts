import { useState, useEffect } from 'react';

export interface Movement {
  id: string;
  movementType: 'INCOME' | 'EXPENSE';
  amount: number;
  movementDate: string;
  description?: string;
  category?: string;
}

const demoMovements: Movement[] = [
  { id: '1', movementType: 'INCOME', amount: 1500, movementDate: '2026-05-01', description: 'Salario', category: 'Trabajo' },
  { id: '2', movementType: 'EXPENSE', amount: 180, movementDate: '2026-05-02', description: 'Supermercado', category: 'Alimentación' },
  { id: '3', movementType: 'INCOME', amount: 500, movementDate: '2026-05-03', description: 'Transferencia', category: 'Personal' },
];

export const useMovements = () => {
  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMovements = async () => {
    try {
      await new Promise(resolve => setTimeout(resolve, 500));
      setMovements(demoMovements);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovements();
  }, []);

  return { movements, loading, error, fetchMovements };
};
