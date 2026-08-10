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
import { DashboardNotification } from '../../domain/DashboardNotification';
import { useAppSession } from '../../../../session/AppSessionContext';
import { useAppTheme } from '../../../../theme/AppThemeContext';
import { AppBottomNavigation } from '../../../../shared/presentation/components/AppBottomNavigation';
import { AuthenticatedHeader } from '../../../../shared/presentation/components/AuthenticatedHeader';
import { StatisticsCard } from '../components/StatisticsCard';
import { StreakCard } from '../components/StreakCard';
import { SummaryCard } from '../components/SummaryCard';
import { UpcomingCard } from '../components/UpcomingCard';

function formatCurrency(value) {
  return `$${new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 0,
  }).format(value)}`;
}

function formatDate(value) {
  if (!value) {
    return null;
  }

  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
  }).format(date).replace('.', '');
}

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <Text style={[styles.errorTitle, { color: palette.text }]}>No pudimos cargar tu inicio</Text>
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

export function HomeScreen() {
  const { loadDashboard } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);

  const notifications = useMemo(
    () => DashboardNotification.fromSnapshot(dashboard),
    [dashboard],
  );

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);

    try {
      setDashboard(await loadDashboard.execute(session));
    } catch (loadError) {
      setError(
        loadError instanceof ApiError
          ? loadError.message
          : 'Ocurrió un error inesperado. Inténtalo nuevamente.',
      );
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadDashboard, session]);

  useEffect(() => {
    load();
  }, [load]);

  function showUpcoming(item, type) {
    if (!item) {
      return;
    }
    const title = item.title || item.name;
    const date = formatDate(item.eventDate || item.nextDueDate);
    Alert.alert(type, `${title}${date ? ` · ${date}` : ''}`);
  }

  function showComingSoon(destination) {
    if (destination === 'Inicio') {
      return;
    }
    Alert.alert(destination, 'Esta sección será construida en la siguiente etapa.');
  }

  return (
    <SafeAreaView
      edges={['top', 'left', 'right']}
      style={[styles.safeArea, { backgroundColor: palette.background }]}
    >
      <StatusBar backgroundColor={palette.brandSoft} style={isDark ? 'light' : 'dark'} />
      <AuthenticatedHeader notifications={notifications} title="Inicio" />

      {isLoading && !dashboard ? (
        <View accessibilityLabel="Cargando inicio" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Organizando tu información…</Text>
        </View>
      ) : error && !dashboard ? (
        <ErrorState message={error} onRetry={() => load()} palette={palette} />
      ) : (
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
          {error ? (
            <Text style={[
              styles.inlineError,
              { backgroundColor: palette.errorSoft, color: palette.error },
            ]}>
              {error}
            </Text>
          ) : null}
          <View style={styles.summaryGrid}>
            <SummaryCard
              backgroundColor={palette.brand}
              icon="currency-usd"
              label="SALDO ACTUAL"
              palette={palette}
              value={formatCurrency(dashboard.balance)}
            />
            <SummaryCard
              backgroundColor={palette.brandSecondary}
              icon="clipboard-list-outline"
              label="COMPROMISOS PENDIENTES"
              palette={palette}
              value={String(dashboard.pendingCommitmentsCount)}
            />
            <SummaryCard
              backgroundColor={palette.brandSecondary}
              icon="clock-outline"
              label="PRÓXIMOS GASTOS"
              palette={palette}
              value={String(dashboard.upcomingExpensesCount)}
            />
            <SummaryCard
              backgroundColor={palette.brandSecondary}
              icon="lock-outline"
              label="CONTRASEÑAS"
              palette={palette}
              value={dashboard.passwordsCount === null
                ? '—'
                : String(dashboard.passwordsCount)}
            />
          </View>

          <View style={styles.horizontalPadding}>
            <StreakCard days={dashboard.streakDays} palette={palette} />
            <View style={styles.upcomingRow}>
              <UpcomingCard
                date={formatDate(dashboard.nextFixedExpense?.nextDueDate)}
                emptyLabel="Sin gastos próximos"
                onPress={() => showUpcoming(dashboard.nextFixedExpense, 'Gasto fijo')}
                palette={palette}
                title={dashboard.nextFixedExpense?.name}
                type="Gastos fijos"
              />
              <UpcomingCard
                date={formatDate(dashboard.nextCommitment?.eventDate)}
                emptyLabel="Sin compromisos"
                onPress={() => showUpcoming(dashboard.nextCommitment, 'Compromiso')}
                palette={palette}
                title={dashboard.nextCommitment?.title}
                type="Compromisos"
              />
            </View>
          </View>

          <StatisticsCard palette={palette} statistics={dashboard.statistics} />
        </ScrollView>
      )}

      <AppBottomNavigation
        activeItem="Inicio"
        onSelect={showComingSoon}
        palette={palette}
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
  errorMessage: {
    fontSize: 15,
    lineHeight: 22,
    marginTop: 8,
    textAlign: 'center',
  },
  errorTitle: {
    fontSize: 21,
    fontWeight: '800',
    textAlign: 'center',
  },
  horizontalPadding: {
    paddingHorizontal: 28,
  },
  inlineError: {
    borderRadius: 8,
    marginBottom: 12,
    marginHorizontal: 28,
    padding: 10,
    textAlign: 'center',
  },
  loadingText: {
    fontSize: 15,
    marginTop: 13,
  },
  retryButton: {
    borderRadius: 9,
    marginTop: 20,
    paddingHorizontal: 26,
    paddingVertical: 12,
  },
  retryText: {
    fontSize: 16,
    fontWeight: '800',
  },
  safeArea: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 20,
    paddingTop: 18,
  },
  summaryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    paddingHorizontal: 28,
  },
  upcomingRow: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 10,
  },
});
