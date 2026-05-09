import { useState, useEffect } from 'react';
import { securityApi, PasswordEntry } from '../api/securityApi';
import { storage } from '../services/storage';

export const usePasswords = () => {
  const [passwords, setPasswords] = useState<PasswordEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasMasterKey, setHasMasterKey] = useState<boolean | null>(null);
  const [masterKeyVerified, setMasterKeyVerified] = useState(false);

  const checkMasterKey = async () => {
    try {
      const response = await securityApi.hasMasterKey();
      setHasMasterKey(response.data.data.hasMasterKey);
    } catch (err) {
      setHasMasterKey(false);
    }
  };

  const createMasterKey = async (masterKey: string) => {
    await securityApi.createMasterKey(masterKey);
    setHasMasterKey(true);
    setMasterKeyVerified(true);
    await storage.setItem('master_key_verified', 'true');
  };

  const verifyMasterKey = async (masterKey: string) => {
    await securityApi.verifyMasterKey(masterKey);
    setMasterKeyVerified(true);
    await storage.setItem('master_key_verified', 'true');
  };

  const fetchPasswords = async () => {
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
  };

  const createPassword = async (data: Omit<PasswordEntry, 'id'>) => {
    const response = await securityApi.createPassword(data);
    setPasswords(prev => [response.data.data, ...prev]);
    return response.data.data;
  };

  const updatePassword = async (id: string, data: Partial<PasswordEntry>) => {
    const response = await securityApi.updatePassword(id, data);
    setPasswords(prev => prev.map(p => p.id === id ? response.data.data : p));
    return response.data.data;
  };

  const deletePassword = async (id: string) => {
    await securityApi.deletePassword(id);
    setPasswords(prev => prev.filter(p => p.id !== id));
  };

  useEffect(() => {
    const checkVerified = async () => {
      const verified = await storage.getItem('master_key_verified');
      if (verified === 'true') setMasterKeyVerified(true);
    };
    checkVerified();
    checkMasterKey();
  }, []);

  useEffect(() => {
    if (masterKeyVerified) fetchPasswords();
  }, [masterKeyVerified]);

  return {
    passwords,
    loading,
    error,
    hasMasterKey,
    masterKeyVerified,
    checkMasterKey,
    createMasterKey,
    verifyMasterKey,
    createPassword,
    updatePassword,
    deletePassword,
    fetchPasswords,
  };
};
