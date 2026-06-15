import { useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { securityApi, PasswordEntry } from '../api/securityApi';
import { useAuth } from '../contexts/AuthContext';
import { STORAGE_KEYS } from '../constants/config';
import { storage } from '../services/storage';

const PASSWORD_COUNT_KEY = 'password_count';

export const usePasswords = () => {
  const { user } = useAuth();
  const [passwords, setPasswords] = useState<PasswordEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [masterKeyVerified, setMasterKeyVerified] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [hasMasterKey, setHasMasterKey] = useState(user?.hasMasterKey || false);

  // No cargar estado de persistencia al inicio (para que siempre pida la clave)
  // En lugar, exponemos función para resetear la verificación

  const resetVerification = async () => {
    try {
      await securityApi.clearMasterKeySession();
    } catch {
      // La sesión local se cierra aunque el backend no pueda limpiar la sesión remota.
    }
    setMasterKeyVerified(false);
    setError(null);
  };

  const refreshMasterKeyStatus = useCallback(async () => {
    try {
      const response = await securityApi.getMasterKeyStatus();
      const status = response.data.data.hasMasterKey;
      setHasMasterKey(status);
      await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, status.toString());
      return status;
    } catch {
      setHasMasterKey(user?.hasMasterKey || false);
      return user?.hasMasterKey || false;
    }
  }, [user?.hasMasterKey]);

  const verifyMasterKey = async (masterKey: string) => {
    setVerifying(true);
    setError(null);
    try {
      await securityApi.verifyMasterKey(masterKey);
      setMasterKeyVerified(true);
      return true;
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Clave maestra incorrecta';
      setError(msg);
      return false;
    } finally {
      setVerifying(false);
    }
  };

  const saveCount = async (count: number) => {
    await AsyncStorage.setItem(PASSWORD_COUNT_KEY, count.toString());
  };

  const createMasterKey = async (masterKey: string) => {
    await securityApi.createMasterKey(masterKey);
    setHasMasterKey(true);
    setMasterKeyVerified(true);
    await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, 'true');
  };

  const changeMasterKey = async (currentMasterKey: string, newMasterKey: string) => {
    await securityApi.changeMasterKey(currentMasterKey, newMasterKey);
    setMasterKeyVerified(true);
    await storage.setItem(STORAGE_KEYS.HAS_MASTER_KEY, 'true');
  };

  const fetchPasswords = useCallback(async () => {
    if (!masterKeyVerified) return;
    try {
      setLoading(true);
      const response = await securityApi.getPasswords();
      const pws = response.data.data;
      setPasswords(pws);
      await saveCount(pws.length);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [masterKeyVerified]);

  const createPassword = async (data: { applicationName: string; password: string; lastChangeDate?: string }) => {
    const response = await securityApi.createPassword(data);
    const newPassword = response.data.data;
    const newList = [newPassword, ...passwords];
    setPasswords(newList);
    await saveCount(newList.length);
    return newPassword;
  };

  const updatePassword = async (id: string, data: Partial<PasswordEntry>) => {
    const payload: any = {};
    if (data.applicationName) payload.applicationName = data.applicationName;
    if (data.password) payload.password = data.password;
    if (data.lastChangeDate) payload.lastChangeDate = data.lastChangeDate;
    const response = await securityApi.updatePassword(id, payload);
    const updated = response.data.data;
    const newList = passwords.map(p => p.id === id ? updated : p);
    setPasswords(newList);
    return updated;
  };

  const deletePassword = async (id: string) => {
    await securityApi.deletePassword(id);
    const newList = passwords.filter(p => p.id !== id);
    setPasswords(newList);
    await saveCount(newList.length);
  };

  useEffect(() => {
    if (masterKeyVerified) {
      fetchPasswords();
    }
  }, [masterKeyVerified, fetchPasswords]);

  useEffect(() => {
    setHasMasterKey(user?.hasMasterKey || false);
  }, [user?.hasMasterKey]);

  useEffect(() => {
    refreshMasterKeyStatus();
  }, [refreshMasterKeyStatus]);

  return {
    passwords,
    loading,
    error,
    verifying,
    hasMasterKey,
    masterKeyVerified,
    verifyMasterKey,
    resetVerification,
    refreshMasterKeyStatus,
    createMasterKey,
    changeMasterKey,
    fetchPasswords,
    createPassword,
    updatePassword,
    deletePassword,
  };
};
