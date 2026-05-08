import { useState, useEffect } from 'react';
import { storage } from '../services/storage';
import { STORAGE_KEYS } from '../constants/config';

export interface PasswordEntry {
  id: string;
  name: string;
  username?: string;
  password: string;
  category?: string;
  lastChangeDate: string;
}

// Demo data
const demoPasswords: PasswordEntry[] = [
  { id: '1', name: 'Netflix', username: 'usuario@netflix.com', password: 'pass123', category: 'Entretenimiento', lastChangeDate: '2026-01-12' },
  { id: '2', name: 'Spotify', username: 'usuario@spotify.com', password: 'pass456', category: 'Música', lastChangeDate: '2026-01-13' },
];

export const usePasswords = () => {
  const [passwords, setPasswords] = useState<PasswordEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [masterKeyVerified, setMasterKeyVerified] = useState(false);

  // Verificar si ya existe clave maestra
  const hasMasterKey = async () => {
    const mk = await storage.getItem(STORAGE_KEYS.HAS_MASTER_KEY);
    return mk === 'true';
  };

  const verifyMasterKey = async (key: string) => {
    // TODO: Llamada real al backend /api/security/verify-master-key
    // Demo: cualquier clave de 8+ caracteres es válida
    if (key.length >= 8) {
      setMasterKeyVerified(true);
      return true;
    }
    throw new Error('Clave maestra incorrecta');
  };

  const createMasterKey = async (key: string) => {
    // TODO: Llamada POST /api/security/master-key
    await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, 'true');
    setMasterKeyVerified(true);
  };

  const resetMasterKeyRequest = async (email: string) => {
    // TODO: enviar código al email
    console.log('Código enviado a', email);
  };

  const resetMasterKeyVerify = async (code: string, newKey: string) => {
    // TODO: verificar código y cambiar clave
    await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, 'true');
    setMasterKeyVerified(true);
  };

  const fetchPasswords = async () => {
    try {
      setLoading(true);
      // TODO: Llamada a /api/security/passwords
      await new Promise(resolve => setTimeout(resolve, 500));
      setPasswords(demoPasswords);
    } catch (err: any) {
      setError(err.message);
      setPasswords(demoPasswords);
    } finally {
      setLoading(false);
    }
  };

  const createPassword = async (pwd: Omit<PasswordEntry, 'id'>) => {
    const newPwd = { ...pwd, id: Date.now().toString() };
    setPasswords(prev => [newPwd, ...prev]);
    return newPwd;
  };

  const updatePassword = async (id: string, data: Partial<PasswordEntry>) => {
    setPasswords(prev => prev.map(p => p.id === id ? { ...p, ...data } : p));
  };

  const deletePassword = async (id: string) => {
    setPasswords(prev => prev.filter(p => p.id !== id));
  };

  useEffect(() => {
    const check = async () => {
      const has = await hasMasterKey();
      if (has) {
        // No auto-verificar, esperamos que el usuario ingrese clave
        setMasterKeyVerified(false);
      }
      fetchPasswords();
    };
    check();
  }, []);

  return {
    passwords,
    loading,
    error,
    masterKeyVerified,
    hasMasterKey,
    verifyMasterKey,
    createMasterKey,
    resetMasterKeyRequest,
    resetMasterKeyVerify,
    fetchPasswords,
    createPassword,
    updatePassword,
    deletePassword,
  };
};
