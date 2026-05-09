import { useState, useEffect, useCallback } from 'react';
import { financesApi, Category } from '../api/financesApi';

export const useCategories = (type: 'FINANCES' | 'AGENDA' = 'FINANCES') => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async () => {
    try {
      const response = await financesApi.getCategories();
      const filtered = response.data.data.filter(c => c.type === type);
      setCategories(filtered);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [type]);

  const createCategory = async (name: string): Promise<Category> => {
    const response = await financesApi.createCategory({ name, type });
    const newCategory = response.data.data;
    setCategories(prev => [...prev, newCategory]);
    return newCategory;
  };

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  return { categories, loading, error, fetchCategories, createCategory };
};
