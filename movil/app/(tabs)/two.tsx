import { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

import { secureSessionStorage } from '@/src/auth/infrastructure/SecureSessionStorage';
import { ApiClient } from '@/src/shared/api/ApiClient';
import { PasswordResponse } from '@/src/shared/api/types';

export default function PasswordsScreen() {
  const [apiBaseUrl, setApiBaseUrl] = useState('http://localhost:8080');
  const [masterKey, setMasterKey] = useState('');
  const [passwords, setPasswords] = useState<PasswordResponse[]>([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const apiClient = useMemo(() => new ApiClient(apiBaseUrl), [apiBaseUrl]);

  useEffect(() => {
    secureSessionStorage.getApiBaseUrl().then(setApiBaseUrl);
  }, []);

  async function verifyAndLoad() {
    setLoading(true);
    setMessage('');
    try {
      await apiClient.verifyMasterKey(masterKey);
      const response = await apiClient.listPasswords();
      setPasswords(response);
      setMessage('Master Key verificada');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No fue posible cargar contraseñas');
    } finally {
      setLoading(false);
    }
  }

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.content}>
      <View>
        <Text style={styles.title}>Gestor de claves</Text>
        <Text style={styles.subtitle}>Credenciales cifradas por cuenta</Text>
      </View>

      <View style={styles.panel}>
        <TextInput
          value={masterKey}
          onChangeText={setMasterKey}
          style={styles.input}
          placeholder="Master Key"
          secureTextEntry
        />
        <Pressable style={styles.primaryButton} onPress={verifyAndLoad} disabled={loading}>
          <Text style={styles.primaryButtonText}>Verificar</Text>
        </Pressable>
      </View>

      {message ? <Text style={styles.message}>{message}</Text> : null}
      {loading ? <ActivityIndicator color="#1f7a68" /> : null}

      <View style={styles.panel}>
        <Text style={styles.sectionTitle}>Credenciales</Text>
        {passwords.length === 0 ? (
          <Text style={styles.emptyText}>Sin credenciales visibles</Text>
        ) : passwords.map((item) => (
          <View key={item.id} style={styles.listRow}>
            <Text style={styles.rowTitle}>{item.applicationName}</Text>
            <Text style={styles.rowDetail}>{item.lastChangeDate ?? 'Sin fecha de cambio'}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: '#f4f7f6',
  },
  content: {
    gap: 14,
    padding: 18,
    paddingBottom: 36,
  },
  title: {
    color: '#17211f',
    fontSize: 28,
    fontWeight: '800',
  },
  subtitle: {
    color: '#55706a',
    fontSize: 14,
    marginTop: 4,
  },
  panel: {
    backgroundColor: '#ffffff',
    borderColor: '#dbe5e2',
    borderRadius: 8,
    borderWidth: 1,
    gap: 10,
    padding: 14,
  },
  input: {
    backgroundColor: '#f9fbfa',
    borderColor: '#cddbd7',
    borderRadius: 6,
    borderWidth: 1,
    color: '#17211f',
    minHeight: 44,
    paddingHorizontal: 12,
  },
  primaryButton: {
    alignItems: 'center',
    backgroundColor: '#1f7a68',
    borderRadius: 6,
    minHeight: 46,
    justifyContent: 'center',
  },
  primaryButtonText: {
    color: '#ffffff',
    fontWeight: '800',
  },
  message: {
    color: '#7b4d13',
    fontWeight: '700',
  },
  sectionTitle: {
    color: '#17211f',
    fontSize: 16,
    fontWeight: '900',
  },
  emptyText: {
    color: '#71847f',
  },
  listRow: {
    borderTopColor: '#edf3f1',
    borderTopWidth: 1,
    gap: 2,
    paddingTop: 10,
  },
  rowTitle: {
    color: '#17211f',
    fontWeight: '800',
  },
  rowDetail: {
    color: '#55706a',
    fontSize: 12,
  },
});
