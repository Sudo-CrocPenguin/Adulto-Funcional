import { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

import { secureSessionStorage } from '@/src/auth/infrastructure/SecureSessionStorage';
import { ApiClient } from '@/src/shared/api/ApiClient';
import {
  AuthResponse,
  CategoryResponse,
  EventResponse,
  FixedExpenseResponse,
  MovementResponse,
} from '@/src/shared/api/types';

type DashboardState = {
  categories: CategoryResponse[];
  movements: MovementResponse[];
  events: EventResponse[];
  fixedExpenses: FixedExpenseResponse[];
};

const emptyDashboard: DashboardState = {
  categories: [],
  movements: [],
  events: [],
  fixedExpenses: [],
};

export default function HomeScreen() {
  const [apiBaseUrl, setApiBaseUrl] = useState('http://localhost:8080');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [names, setNames] = useState('');
  const [lastnames, setLastnames] = useState('');
  const [phone, setPhone] = useState('');
  const [masterKey, setMasterKey] = useState('');
  const [registerMode, setRegisterMode] = useState(false);
  const [account, setAccount] = useState<AuthResponse | null>(null);
  const [dashboard, setDashboard] = useState<DashboardState>(emptyDashboard);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const apiClient = useMemo(() => new ApiClient(apiBaseUrl), [apiBaseUrl]);

  useEffect(() => {
    secureSessionStorage.getApiBaseUrl().then(setApiBaseUrl);
  }, []);

  async function persistApiBaseUrl() {
    await secureSessionStorage.saveApiBaseUrl(apiBaseUrl);
    setMessage('API actualizada');
  }

  async function submitAuth() {
    setLoading(true);
    setMessage('');
    try {
      await secureSessionStorage.saveApiBaseUrl(apiBaseUrl);
      const auth = registerMode
        ? await apiClient.register({ email, password, names, lastnames, phone, masterKey: masterKey || undefined })
        : await apiClient.login({ email, password });
      setAccount(auth);
      await loadDashboard();
      setMessage(registerMode ? 'Cuenta creada' : 'Sesión iniciada');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No fue posible autenticar');
    } finally {
      setLoading(false);
    }
  }

  async function loadDashboard() {
    setLoading(true);
    setMessage('');
    try {
      const [categories, movements, events, fixedExpenses] = await Promise.all([
        apiClient.listCategories(),
        apiClient.listMovements(),
        apiClient.listEvents(),
        apiClient.listFixedExpenses(),
      ]);
      setDashboard({ categories, movements, events, fixedExpenses });
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No fue posible cargar el panel');
    } finally {
      setLoading(false);
    }
  }

  async function logout() {
    setLoading(true);
    try {
      await apiClient.logout();
    } catch {
      await secureSessionStorage.clearToken();
    } finally {
      setAccount(null);
      setDashboard(emptyDashboard);
      setLoading(false);
      setMessage('Sesión cerrada');
    }
  }

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={loadDashboard} />}>
      <View style={styles.header}>
        <Text style={styles.title}>Adulto Funcional</Text>
        <Text style={styles.subtitle}>Finanzas, agenda y gestor seguro</Text>
      </View>

      <View style={styles.panel}>
        <Text style={styles.label}>API</Text>
        <View style={styles.row}>
          <TextInput
            value={apiBaseUrl}
            onChangeText={setApiBaseUrl}
            autoCapitalize="none"
            style={[styles.input, styles.flex]}
            placeholder="http://localhost:8080"
          />
          <Pressable style={styles.secondaryButton} onPress={persistApiBaseUrl}>
            <Text style={styles.secondaryButtonText}>Guardar</Text>
          </Pressable>
        </View>
      </View>

      <View style={styles.panel}>
        <View style={styles.segment}>
          <Pressable
            style={[styles.segmentButton, !registerMode && styles.segmentButtonActive]}
            onPress={() => setRegisterMode(false)}>
            <Text style={[styles.segmentText, !registerMode && styles.segmentTextActive]}>Login</Text>
          </Pressable>
          <Pressable
            style={[styles.segmentButton, registerMode && styles.segmentButtonActive]}
            onPress={() => setRegisterMode(true)}>
            <Text style={[styles.segmentText, registerMode && styles.segmentTextActive]}>Registro</Text>
          </Pressable>
        </View>

        {registerMode && (
          <>
            <TextInput value={names} onChangeText={setNames} style={styles.input} placeholder="Nombres" />
            <TextInput value={lastnames} onChangeText={setLastnames} style={styles.input} placeholder="Apellidos" />
            <TextInput value={phone} onChangeText={setPhone} style={styles.input} placeholder="Teléfono" />
            <TextInput
              value={masterKey}
              onChangeText={setMasterKey}
              style={styles.input}
              placeholder="Master Key"
              secureTextEntry
            />
          </>
        )}

        <TextInput
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
          style={styles.input}
          placeholder="Email"
        />
        <TextInput
          value={password}
          onChangeText={setPassword}
          style={styles.input}
          placeholder="Contraseña"
          secureTextEntry
        />
        <Pressable style={styles.primaryButton} onPress={submitAuth} disabled={loading}>
          <Text style={styles.primaryButtonText}>{registerMode ? 'Crear cuenta' : 'Entrar'}</Text>
        </Pressable>
      </View>

      {message ? <Text style={styles.message}>{message}</Text> : null}
      {loading ? <ActivityIndicator color="#1f7a68" /> : null}

      {account && (
        <View style={styles.sessionBar}>
          <Text style={styles.sessionText}>{account.names} {account.lastnames}</Text>
          <Pressable onPress={logout}>
            <Text style={styles.linkText}>Salir</Text>
          </Pressable>
        </View>
      )}

      <View style={styles.metricsGrid}>
        <Metric label="Categorías" value={dashboard.categories.length} />
        <Metric label="Movimientos" value={dashboard.movements.length} />
        <Metric label="Eventos" value={dashboard.events.length} />
        <Metric label="Gastos fijos" value={dashboard.fixedExpenses.length} />
      </View>

      <ListSection
        title="Movimientos recientes"
        empty="Sin movimientos"
        rows={dashboard.movements.slice(0, 4).map((item) => ({
          id: item.id,
          title: item.description || item.movementType,
          detail: `${item.movementDate} · ${item.amount}`,
        }))}
      />
      <ListSection
        title="Agenda"
        empty="Sin eventos"
        rows={dashboard.events.slice(0, 4).map((item) => ({
          id: item.id,
          title: item.title,
          detail: `${item.eventDate} · ${item.status}`,
        }))}
      />
      <ListSection
        title="Gastos fijos"
        empty="Sin gastos fijos"
        rows={dashboard.fixedExpenses.slice(0, 4).map((item) => ({
          id: item.id,
          title: item.name,
          detail: `${item.frequency} · ${item.status} · ${item.amount}`,
        }))}
      />
    </ScrollView>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <View style={styles.metric}>
      <Text style={styles.metricValue}>{value}</Text>
      <Text style={styles.metricLabel}>{label}</Text>
    </View>
  );
}

function ListSection({
  title,
  empty,
  rows,
}: {
  title: string;
  empty: string;
  rows: Array<{ id: string; title: string; detail: string }>;
}) {
  return (
    <View style={styles.panel}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {rows.length === 0 ? (
        <Text style={styles.emptyText}>{empty}</Text>
      ) : rows.map((row) => (
        <View key={row.id} style={styles.listRow}>
          <Text style={styles.rowTitle}>{row.title}</Text>
          <Text style={styles.rowDetail}>{row.detail}</Text>
        </View>
      ))}
    </View>
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
  header: {
    paddingTop: 10,
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
  label: {
    color: '#405a54',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  row: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 8,
  },
  flex: {
    flex: 1,
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
  secondaryButton: {
    alignItems: 'center',
    borderColor: '#1f7a68',
    borderRadius: 6,
    borderWidth: 1,
    minHeight: 44,
    justifyContent: 'center',
    paddingHorizontal: 12,
  },
  secondaryButtonText: {
    color: '#1f7a68',
    fontWeight: '800',
  },
  segment: {
    backgroundColor: '#edf3f1',
    borderRadius: 8,
    flexDirection: 'row',
    padding: 4,
  },
  segmentButton: {
    alignItems: 'center',
    borderRadius: 6,
    flex: 1,
    paddingVertical: 9,
  },
  segmentButtonActive: {
    backgroundColor: '#ffffff',
  },
  segmentText: {
    color: '#55706a',
    fontWeight: '700',
  },
  segmentTextActive: {
    color: '#1f7a68',
  },
  message: {
    color: '#7b4d13',
    fontWeight: '700',
  },
  sessionBar: {
    alignItems: 'center',
    backgroundColor: '#e6f2ef',
    borderRadius: 8,
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 12,
  },
  sessionText: {
    color: '#17211f',
    fontWeight: '800',
  },
  linkText: {
    color: '#1f7a68',
    fontWeight: '800',
  },
  metricsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  metric: {
    backgroundColor: '#ffffff',
    borderColor: '#dbe5e2',
    borderRadius: 8,
    borderWidth: 1,
    minHeight: 78,
    padding: 12,
    width: '48%',
  },
  metricValue: {
    color: '#1f7a68',
    fontSize: 24,
    fontWeight: '900',
  },
  metricLabel: {
    color: '#55706a',
    fontSize: 12,
    fontWeight: '700',
    marginTop: 4,
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
