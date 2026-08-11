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
import { FixedExpensePaymentSyncError } from '../../application/PayFixedExpenseUseCase';
import { FIXED_EXPENSE_TABS } from '../../domain/FixedExpenseCollection';
import { FixedExpenseCard } from '../components/FixedExpenseCard';
import { FixedExpenseFilters } from '../components/FixedExpenseFilters';
import { NewFixedExpenseSheet } from '../components/NewFixedExpenseSheet';

const INITIAL_FILTERS = Object.freeze({
  categoryId: '',
  frequency: '',
  status: '',
  tab: FIXED_EXPENSE_TABS.all,
});

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <MaterialCommunityIcons color={palette.navigationMuted} name="hand-coin-outline" size={55} />
      <Text style={[styles.errorTitle, { color: palette.text }]}>No pudimos cargar tus gastos fijos</Text>
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

export function FixedExpensesScreen({ navigation }) {
  const { createFixedExpense, loadFixedExpenses, payFixedExpense } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [collection, setCollection] = useState(null);
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [filters, setFilters] = useState(INITIAL_FILTERS);
  const [formVisible, setFormVisible] = useState(false);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);
  const [payingId, setPayingId] = useState(null);

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);
    try {
      setCollection(await loadFixedExpenses.execute(session));
    } catch (loadError) {
      setError(loadError instanceof ApiError
        ? loadError.message
        : 'Ocurrió un error inesperado. Inténtalo nuevamente.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadFixedExpenses, session]);

  useEffect(() => {
    load();
  }, [load]);

  const visibleExpenses = useMemo(
    () => collection?.filteredBy(filters) ?? [],
    [collection, filters],
  );
  const notifications = useMemo(() => (
    collection?.filteredBy({
      categoryId: '',
      frequency: '',
      status: 'ACTIVE',
      tab: FIXED_EXPENSE_TABS.dueSoon,
    }).slice(0, 3).map((expense) => ({
      date: expense.nextDueDate,
      id: `fixed-expense:${expense.id}`,
      message: `Monto: $${expense.amount}`,
      subject: expense.name,
      title: 'Gastos Fijos',
      type: 'fixed-expense',
    })) ?? []
  ), [collection]);

  async function submitFixedExpense(form) {
    const expense = await createFixedExpense.execute(form, session);
    setCollection((current) => current?.withExpense(expense) ?? current);
    setFeedback('Gasto fijo guardado correctamente.');
  }

  async function pay(expense) {
    setPayingId(expense.id);
    setFeedback(null);
    setError(null);
    try {
      const { updatedExpense } = await payFixedExpense.execute(expense, session);
      setCollection((current) => current?.withUpdated(updatedExpense) ?? current);
      setFeedback(
        `Pago de ${expense.name} registrado como egreso. El próximo vencimiento fue actualizado.`,
      );
    } catch (paymentError) {
      if (paymentError instanceof FixedExpensePaymentSyncError) {
        setError(paymentError.message);
        await load({ refresh: true });
      } else {
        setError(paymentError instanceof ApiError
          ? paymentError.message
          : 'No fue posible registrar el pago. No se actualizó el vencimiento.');
      }
    } finally {
      setPayingId(null);
    }
  }

  function confirmPayment(expense) {
    Alert.alert(
      'Registrar pago',
      `Se creará un egreso de $${expense.amount} para ${expense.name} y se avanzará su próximo vencimiento.`,
      [
        { style: 'cancel', text: 'Cancelar' },
        { onPress: () => pay(expense), text: 'Registrar' },
      ],
    );
  }

  function selectDestination(destination) {
    const destinations = {
      Compromisos: APP_ROUTES.commitments,
      Contraseñas: APP_ROUTES.passwords,
      Finanzas: APP_ROUTES.finances,
      Inicio: APP_ROUTES.home,
    };
    if (destination === 'Gastos Fijos') {
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
      <AuthenticatedHeader notifications={notifications} title="Gastos Fijos" />

      {isLoading && !collection ? (
        <View accessibilityLabel="Cargando gastos fijos" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Ordenando tus vencimientos…</Text>
        </View>
      ) : error && !collection ? (
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
              <Text
                accessibilityLiveRegion="assertive"
                style={[styles.inlineError, { backgroundColor: palette.errorSoft, color: palette.error }]}
              >
                {error}
              </Text>
            ) : null}

            <FixedExpenseFilters
              categories={collection.categories}
              filters={filters}
              onChange={(nextFilters) => {
                setFilters(nextFilters);
                setFeedback(null);
              }}
              palette={palette}
            />
            <View style={styles.list}>
              {visibleExpenses.length > 0 ? visibleExpenses.map((expense) => (
                <FixedExpenseCard
                  expense={expense}
                  isPaying={payingId === expense.id}
                  key={expense.id}
                  now={collection.now}
                  onPay={() => confirmPayment(expense)}
                  palette={palette}
                />
              )) : (
                <View style={[styles.emptyState, { backgroundColor: palette.surface }]}> 
                  <MaterialCommunityIcons color={palette.navigationMuted} name="calendar-remove-outline" size={44} />
                  <Text style={[styles.emptyText, { color: palette.textMuted }]}> 
                    {collection.expenses.length === 0
                      ? 'Crea tu primer gasto fijo con el botón +.'
                      : 'No hay gastos que coincidan con estos filtros.'}
                  </Text>
                </View>
              )}
            </View>
          </ScrollView>

          <Pressable
            accessibilityLabel="Crear gasto fijo"
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

      <AppBottomNavigation activeItem="Gastos Fijos" onSelect={selectDestination} palette={palette} />
      <NewFixedExpenseSheet
        categories={collection?.categories ?? []}
        onClose={() => setFormVisible(false)}
        onSubmit={submitFixedExpense}
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
    borderRadius: 9,
    justifyContent: 'center',
    minHeight: 170,
    padding: 20,
  },
  emptyText: {
    fontSize: 15,
    lineHeight: 21,
    marginTop: 10,
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
    lineHeight: 20,
    marginBottom: 14,
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
    lineHeight: 20,
    marginBottom: 14,
    padding: 10,
    textAlign: 'center',
  },
  list: {
    gap: 12,
    marginBottom: 90,
  },
  loadingText: {
    fontSize: 15,
    marginTop: 13,
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
    paddingHorizontal: 14,
    paddingTop: 31,
  },
});
