import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useAppDependencies } from '../../../../composition/AppDependenciesContext';
import { ApiError } from '../../../../core/http/ApiError';
import { APP_ROUTES } from '../../../../navigation/routes';
import { useAppSession } from '../../../../session/AppSessionContext';
import { AppBottomNavigation } from '../../../../shared/presentation/components/AppBottomNavigation';
import { AuthenticatedHeader } from '../../../../shared/presentation/components/AuthenticatedHeader';
import { useAppTheme } from '../../../../theme/AppThemeContext';
import { FinanceSummaryCard } from '../components/FinanceSummaryCard';
import { MovementRow } from '../components/MovementRow';
import { NewMovementSheet } from '../components/NewMovementSheet';

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <MaterialCommunityIcons color={palette.navigationMuted} name="wallet-outline" size={55} />
      <Text style={[styles.errorTitle, { color: palette.text }]}>No pudimos cargar tus finanzas</Text>
      <Text style={[styles.errorMessage, { color: palette.textMuted }]}>{message}</Text>
      <Pressable
        accessibilityRole="button"
        onPress={onRetry}
        style={({ pressed }) => [
          styles.retryButton,
          { backgroundColor: palette.brand },
          pressed && { backgroundColor: palette.brandPressed },
        ]}
      >
        <Text style={[styles.retryText, { color: palette.surfaceOnBrand }]}>Reintentar</Text>
      </Pressable>
    </View>
  );
}

export function FinanceScreen({ navigation }) {
  const { createMovement, loadFinances } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [formVisible, setFormVisible] = useState(false);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);
  const [ledger, setLedger] = useState(null);
  const [search, setSearch] = useState('');

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);
    try {
      setLedger(await loadFinances.execute(session));
    } catch (loadError) {
      setError(loadError instanceof ApiError
        ? loadError.message
        : 'Ocurrió un error inesperado. Inténtalo nuevamente.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadFinances, session]);

  useEffect(() => {
    load();
  }, [load]);

  const visibleMovements = useMemo(
    () => ledger?.filteredBy(search) ?? [],
    [ledger, search],
  );

  const notifications = useMemo(() => (
    ledger?.balance < 0 ? [{
      id: 'finance:negative-balance',
      message: 'Tus egresos acumulados superan tus ingresos.',
      subject: 'Saldo negativo',
      title: 'Finanzas',
      type: 'finance',
    }] : []
  ), [ledger]);

  async function submitMovement(form) {
    const movement = await createMovement.execute(form, session);
    setLedger((current) => current?.withMovement(movement) ?? current);
    setFeedback('Movimiento registrado correctamente.');
  }

  function selectDestination(destination) {
    const destinations = {
      Compromisos: APP_ROUTES.commitments,
      'Gastos Fijos': APP_ROUTES.fixedExpenses,
      Inicio: APP_ROUTES.home,
    };
    if (destination === 'Finanzas') {
      return;
    }
    if (destinations[destination]) {
      navigation.navigate(destinations[destination]);
      return;
    }
    Alert.alert(destination, 'Esta sección será construida en una siguiente etapa.');
  }

  return (
    <SafeAreaView
      edges={['top', 'left', 'right']}
      style={[styles.safeArea, { backgroundColor: palette.background }]}
    >
      <StatusBar backgroundColor={palette.brandSoft} style={isDark ? 'light' : 'dark'} />
      <AuthenticatedHeader notifications={notifications} title="Finanzas" />

      {isLoading && !ledger ? (
        <View accessibilityLabel="Cargando finanzas" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Calculando tus finanzas…</Text>
        </View>
      ) : error && !ledger ? (
        <ErrorState message={error} onRetry={() => load()} palette={palette} />
      ) : (
        <>
          <ScrollView
            contentContainerStyle={styles.scrollContent}
            refreshControl={(
              <RefreshControl
                colors={[palette.brand]}
                onRefresh={() => load({ refresh: true })}
                refreshing={isRefreshing}
                tintColor={palette.brand}
              />
            )}
            showsVerticalScrollIndicator={false}
          >
            {feedback ? (
              <Text
                accessibilityLiveRegion="polite"
                style={[styles.feedback, { backgroundColor: palette.successSoft, color: palette.success }]}
              >
                {feedback}
              </Text>
            ) : null}
            {error ? (
              <Text style={[styles.inlineError, { backgroundColor: palette.errorSoft, color: palette.error }]}> 
                {error}
              </Text>
            ) : null}

            <View style={styles.summaryList}>
              <FinanceSummaryCard
                kind="income"
                label="TOTAL INGRESOS"
                palette={palette}
                value={ledger.totalIncome}
              />
              <FinanceSummaryCard
                kind="expense"
                label="TOTAL EGRESOS"
                palette={palette}
                value={ledger.totalExpenses}
              />
              <FinanceSummaryCard
                analytics
                kind="balance"
                label="SALDO ACTUAL"
                onAnalytics={() => navigation.navigate(APP_ROUTES.financeAnalytics)}
                palette={palette}
                value={ledger.balance}
              />
            </View>

            <View style={[styles.searchShell, { backgroundColor: palette.surface, borderColor: palette.navigationMuted }]}> 
              <MaterialCommunityIcons color={palette.navigationMuted} name="magnify" size={31} />
              <TextInput
                accessibilityLabel="Buscar movimientos"
                onChangeText={setSearch}
                placeholder="Buscar por descripción o clasificación"
                placeholderTextColor={palette.textMuted}
                returnKeyType="search"
                style={[styles.searchInput, { color: palette.text }]}
                value={search}
              />
              {search ? (
                <Pressable accessibilityLabel="Limpiar búsqueda" hitSlop={8} onPress={() => setSearch('')}> 
                  <MaterialCommunityIcons color={palette.navigationMuted} name="close" size={28} />
                </Pressable>
              ) : null}
            </View>

            <View style={[styles.movements, { backgroundColor: palette.surface }]}> 
              {visibleMovements.length > 0 ? visibleMovements.map((movement) => (
                <MovementRow key={movement.id} movement={movement} palette={palette} />
              )) : (
                <View style={styles.emptyState}>
                  <MaterialCommunityIcons color={palette.navigationMuted} name="cash-clock" size={42} />
                  <Text style={[styles.emptyText, { color: palette.textMuted }]}> 
                    {search ? 'No hay movimientos que coincidan.' : 'Registra tu primer movimiento con el botón +.'}
                  </Text>
                </View>
              )}
            </View>
          </ScrollView>

          <Pressable
            accessibilityLabel="Crear movimiento"
            accessibilityRole="button"
            onPress={() => {
              setFeedback(null);
              setFormVisible(true);
            }}
            style={({ pressed }) => [
              styles.floatingButton,
              { backgroundColor: palette.brandSecondary, shadowColor: palette.shadow },
              pressed && { backgroundColor: palette.brandPressed },
            ]}
          >
            <MaterialCommunityIcons color={palette.surfaceOnBrand} name="plus" size={48} />
          </Pressable>
        </>
      )}

      <AppBottomNavigation activeItem="Finanzas" onSelect={selectDestination} palette={palette} />
      <NewMovementSheet
        categories={ledger?.categories ?? []}
        onClose={() => setFormVisible(false)}
        onSubmit={submitMovement}
        palette={palette}
        visible={formVisible}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  centerState: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 35,
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 155,
    padding: 20,
  },
  emptyText: {
    fontSize: 15,
    lineHeight: 21,
    marginTop: 9,
    textAlign: 'center',
  },
  errorMessage: {
    fontSize: 15,
    lineHeight: 22,
    marginTop: 8,
    textAlign: 'center',
  },
  errorTitle: {
    fontSize: 21,
    fontWeight: '800',
    marginTop: 13,
    textAlign: 'center',
  },
  feedback: {
    borderRadius: 8,
    fontSize: 14,
    marginBottom: 11,
    padding: 10,
    textAlign: 'center',
  },
  floatingButton: {
    alignItems: 'center',
    borderRadius: 7,
    bottom: 84,
    elevation: 5,
    height: 70,
    justifyContent: 'center',
    position: 'absolute',
    right: 22,
    shadowOffset: { height: 3, width: 0 },
    shadowOpacity: 0.22,
    shadowRadius: 5,
    width: 70,
  },
  inlineError: {
    borderRadius: 8,
    fontSize: 14,
    marginBottom: 11,
    padding: 10,
    textAlign: 'center',
  },
  loadingText: {
    fontSize: 15,
    marginTop: 13,
  },
  movements: {
    borderRadius: 8,
    marginBottom: 90,
    overflow: 'hidden',
  },
  retryButton: {
    borderRadius: 8,
    marginTop: 20,
    paddingHorizontal: 24,
    paddingVertical: 13,
  },
  retryText: {
    fontSize: 16,
    fontWeight: '800',
  },
  safeArea: {
    flex: 1,
  },
  scrollContent: {
    padding: 13,
  },
  searchInput: {
    flex: 1,
    fontSize: 15,
    marginHorizontal: 8,
    minHeight: 49,
  },
  searchShell: {
    alignItems: 'center',
    borderRadius: 11,
    borderWidth: 1,
    flexDirection: 'row',
    marginVertical: 13,
    paddingHorizontal: 12,
  },
  summaryList: {
    gap: 8,
  },
});
