import { useState, useEffect } from 'react';
import { storage } from '../services/storage';
import { STORAGE_KEYS } from '../constants/config';
import apiClient from '../api/client';

export interface UserProfile {
  id: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
}

export const useProfile = () => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const accountId = await storage.getItem(STORAGE_KEYS.ACCOUNT_ID);
      if (!accountId) throw new Error('No autenticado');
      // TODO: Llamada real a /api/account/{id}
      // const response = await apiClient.get(`/api/account/${accountId}`);
      // setProfile(response.data.data);
      await new Promise(resolve => setTimeout(resolve, 500));
      setProfile({
        id: accountId,
        names: await storage.getItem(STORAGE_KEYS.USER_NAMES) || 'Usuario',
        lastnames: await storage.getItem(STORAGE_KEYS.USER_LASTNAMES) || '',
        email: await storage.getItem(STORAGE_KEYS.USER_EMAIL) || '',
        phone: await storage.getItem(STORAGE_KEYS.USER_PHONE) || '',
        createdAt: new Date().toISOString(),
      });
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (data: Partial<UserProfile>) => {
    // TODO: PATCH a /api/account/{id}
    if (profile) {
      const updated = { ...profile, ...data };
      setProfile(updated);
      await storage.setItem(STORAGE_KEYS.USER_NAMES, updated.names);
      await storage.setItem(STORAGE_KEYS.USER_LASTNAMES, updated.lastnames);
      await storage.setItem(STORAGE_KEYS.USER_EMAIL, updated.email);
      await storage.setItem(STORAGE_KEYS.USER_PHONE, updated.phone);
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    // TODO: POST a /api/account/change-password
    console.log('Cambiar contraseña', oldPassword, newPassword);
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  return { profile, loading, error, fetchProfile, updateProfile, changePassword };
};
