import { useState, useEffect, useCallback } from 'react';
import { financesApi, Movement } from '../api/financesApi';
import { useAuth } from '../contexts/AuthContext';

export const useMovements = () => {
  const { user } = useAuth();
  const [movements, setMovements] = useState<Movement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMovements = useCallback(async () => {
    if (!user?.accountId) return;
    try {
      setLoading(true);
      const response = await financesApi.getMovements();
      setMovements(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [user?.accountId]);

  const createMovement = async (data: Omit<Movement, 'id'>) => {
    // Preparar payload para backend: espera categoryId (string) no objeto category
    const payload = {
      movementType: data.movementType,
      amount: data.amount,
      movementDate: data.movementDate,
      description: data.description || '',
      categoryId: data.category?.id,   // ← backend espera categoryId
    };
    const response = await financesApi.createMovement(payload as any);
    const newMovement = response.data.data;
    setMovements(prev => [newMovement, ...prev]);
    return newMovement;
  };

  const updateMovement = async (id: string, data: Partial<Movement>) => {
    // Similar: si se quiere cambiar categoría, enviar categoryId
    const payload: any = { ...data };
    if (data.category) {
      payload.categoryId = data.category.id;
      delete payload.category;
    }
    const response = await financesApi.updateMovement(id, payload);
    const updated = response.data.data;
    setMovements(prev => prev.map(m => m.id === id ? updated : m));
    return updated;
  };

  const deleteMovement = async (id: string) => {
    await financesApi.deleteMovement(id);
    setMovements(prev => prev.filter(m => m.id !== id));
  };

  useEffect(() => {
    fetchMovements();
  }, [fetchMovements]);

  return { movements, loading, error, fetchMovements, createMovement, updateMovement, deleteMovement };
};
