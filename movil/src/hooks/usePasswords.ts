import { useState, useEffect, useCallback } from 'react';
import { securityApi, PasswordEntry } from '../api/securityApi';
import { storage } from '../services/storage';
import { useAuth } from '../contexts/AuthContext';

export const usePasswords = () => {
  const { user } = useAuth();
  const [passwords, setPasswords] = useState<PasswordEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [masterKeyVerified, setMasterKeyVerified] = useState(false);
  const hasMasterKey = user?.hasMasterKey || false;

  // Verificar si ya tenemos la clave verificada en storage (persistencia local)
  useEffect(() => {
    const checkVerified = async () => {
      const verified = await storage.getItem('master_key_verified');
      if (verified === 'true') setMasterKeyVerified(true);
    };
    checkVerified();
  }, []);

  const verifyMasterKey = async (masterKey: string) => {
    await securityApi.verifyMasterKey(masterKey);
    setMasterKeyVerified(true);
    await storage.setItem('master_key_verified', 'true');
  };

  const fetchPasswords = useCallback(async () => {
    if (!masterKeyVerified) return;
    try {
      setLoading(true);
      const response = await securityApi.getPasswords();
      setPasswords(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [masterKeyVerified]);

  const createPassword = async (data: { applicationName: string; password: string; lastChangeDate?: string }) => {
    const response = await securityApi.createPassword(data);
    const newPassword = response.data.data;
    setPasswords(prev => [newPassword, ...prev]);
    return newPassword;
  };

  const updatePassword = async (id: string, data: Partial<PasswordEntry>) => {
    const payload: any = {};
    if (data.applicationName) payload.applicationName = data.applicationName;
    if (data.password) payload.password = data.password;
    if (data.lastChangeDate) payload.lastChangeDate = data.lastChangeDate;
    const response = await securityApi.updatePassword(id, payload);
    const updated = response.data.data;
    setPasswords(prev => prev.map(p => p.id === id ? updated : p));
    return updated;
  };

  const deletePassword = async (id: string) => {
    await securityApi.deletePassword(id);
    setPasswords(prev => prev.filter(p => p.id !== id));
  };

  useEffect(() => {
    if (masterKeyVerified) fetchPasswords();
  }, [masterKeyVerified, fetchPasswords]);

  return {
    passwords,
    loading,
    error,
    hasMasterKey,
    masterKeyVerified,
    verifyMasterKey,
    fetchPasswords,
    createPassword,
    updatePassword,
    deletePassword,
  };
};
