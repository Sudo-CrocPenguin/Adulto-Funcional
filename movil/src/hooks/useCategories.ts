import { useState, useEffect, useCallback } from 'react';
import { financesApi, Category } from '../api/financesApi';
import { getApiErrorMessage } from '../services/errorHandler';

export const useCategories = (type: 'FINANCES' | 'AGENDA' = 'FINANCES') => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async () => {
    try {
      const response = await financesApi.getCategories();
      const filtered = response.data.data.filter(c => c.type === type);
      setCategories(filtered);
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, 'No se pudieron cargar las categorías'));
    } finally {
      setLoading(false);
    }
  }, [type]);

  const createCategory = async (name: string): Promise<Category> => {
    try {
      const response = await financesApi.createCategory({ name, type });
      const newCategory = response.data.data;
      setCategories(prev => [...prev, newCategory]);
      return newCategory;
    } catch (err: unknown) {
      throw new Error(getApiErrorMessage(err, 'No se pudo crear la categoría'));
    }
  };

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  return { categories, loading, error, fetchCategories, createCategory };
};
