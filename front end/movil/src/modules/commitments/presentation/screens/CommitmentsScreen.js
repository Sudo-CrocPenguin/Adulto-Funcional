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
import { COMMITMENT_FILTERS } from '../../domain/Commitment';
import { CommitmentCard } from '../components/CommitmentCard';
import { CommitmentStreakCard } from '../components/CommitmentStreakCard';
import { CommitmentTabs } from '../components/CommitmentTabs';
import { NewCommitmentSheet } from '../components/NewCommitmentSheet';

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <MaterialCommunityIcons
        color={palette.navigationMuted}
        name="calendar-alert"
        size={54}
      />
      <Text style={[styles.errorTitle, { color: palette.text }]}>
        No pudimos cargar tus compromisos
      </Text>
      <Text style={[styles.errorMessage, { color: palette.textMuted }]}>
        {message}
      </Text>
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

function EmptyState({ activeFilter, palette }) {
  const message = activeFilter === COMMITMENT_FILTERS.pending
    ? 'No tienes compromisos pendientes.'
    : activeFilter === COMMITMENT_FILTERS.completed
      ? 'Aún no tienes compromisos completados.'
      : 'Crea tu primer compromiso con el botón +.';

  return (
    <View style={[styles.emptyState, { backgroundColor: palette.surface }]}>
      <MaterialCommunityIcons
        color={palette.navigationMuted}
        name="clipboard-check-outline"
        size={44}
      />
      <Text style={[styles.emptyText, { color: palette.textMuted }]}>{message}</Text>
    </View>
  );
}

export function CommitmentsScreen({ navigation }) {
  const { createCommitment, loadCommitments } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [activeFilter, setActiveFilter] = useState(COMMITMENT_FILTERS.all);
  const [collection, setCollection] = useState(null);
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [formVisible, setFormVisible] = useState(false);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);

    try {
      setCollection(await loadCommitments.execute(session));
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
  }, [loadCommitments, session]);

  useEffect(() => {
    load();
  }, [load]);

  const visibleCommitments = useMemo(
    () => collection?.filteredBy(activeFilter) ?? [],
    [activeFilter, collection],
  );

  const notifications = useMemo(() => {
    const nextPending = collection?.commitments.find(({ isPending }) => isPending);
    return nextPending ? [{
      date: nextPending.eventDate,
      id: `commitment:${nextPending.id}`,
      message: null,
      subject: nextPending.title,
      title: 'Compromisos',
      type: 'commitment',
    }] : [];
  }, [collection]);

  async function submitCommitment(form) {
    const commitment = await createCommitment.execute(form, session);
    setCollection((current) => current?.withAdded(commitment) ?? current);
    setFeedback('Compromiso guardado correctamente.');
  }

  function selectDestination(destination) {
    if (destination === 'Compromisos') {
      return;
    }
    if (destination === 'Inicio') {
      navigation.navigate(APP_ROUTES.home);
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
      <AuthenticatedHeader notifications={notifications} title="Compromisos" />

      {isLoading && !collection ? (
        <View accessibilityLabel="Cargando compromisos" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>
            Organizando tus compromisos…
          </Text>
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
                style={[
                  styles.feedback,
                  { backgroundColor: palette.successSoft, color: palette.success },
                ]}
              >
                {feedback}
              </Text>
            ) : null}
            {error ? (
              <Text style={[
                styles.inlineError,
                { backgroundColor: palette.errorSoft, color: palette.error },
              ]}>
                {error}
              </Text>
            ) : null}

            <View style={styles.streakContainer}>
              <CommitmentStreakCard
                days={collection.streakDays}
                palette={palette}
              />
            </View>
            <CommitmentTabs
              activeFilter={activeFilter}
              onSelect={(filter) => {
                setActiveFilter(filter);
                setFeedback(null);
              }}
              palette={palette}
            />
            <View style={styles.list}>
              {visibleCommitments.length === 0 ? (
                <EmptyState activeFilter={activeFilter} palette={palette} />
              ) : visibleCommitments.map((commitment) => (
                <CommitmentCard
                  commitment={commitment}
                  key={commitment.id}
                  palette={palette}
                />
              ))}
            </View>
          </ScrollView>

          <Pressable
            accessibilityLabel="Crear compromiso"
            accessibilityRole="button"
            onPress={() => {
              setFeedback(null);
              setFormVisible(true);
            }}
            style={({ pressed }) => [
              styles.floatingButton,
              {
                backgroundColor: palette.brandSecondary,
                shadowColor: palette.shadow,
              },
              pressed && { backgroundColor: palette.brandPressed },
            ]}
          >
            <MaterialCommunityIcons
              color={palette.surfaceOnBrand}
              name="plus"
              size={48}
            />
          </Pressable>
        </>
      )}

      <AppBottomNavigation
        activeItem="Compromisos"
        onSelect={selectDestination}
        palette={palette}
      />
      <NewCommitmentSheet
        categories={collection?.categories ?? []}
        onClose={() => setFormVisible(false)}
        onSubmit={submitCommitment}
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
    borderRadius: 12,
    justifyContent: 'center',
    minHeight: 150,
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
    marginBottom: 2,
    marginHorizontal: 28,
    padding: 10,
    textAlign: 'center',
  },
  floatingButton: {
    alignItems: 'center',
    borderRadius: 9,
    bottom: 88,
    elevation: 8,
    height: 72,
    justifyContent: 'center',
    position: 'absolute',
    right: 28,
    shadowOffset: { height: 4, width: 0 },
    shadowOpacity: 0.2,
    shadowRadius: 6,
    width: 72,
    zIndex: 8,
  },
  inlineError: {
    borderRadius: 8,
    marginBottom: 2,
    marginHorizontal: 28,
    padding: 10,
    textAlign: 'center',
  },
  list: {
    gap: 12,
    paddingBottom: 100,
    paddingHorizontal: 28,
    paddingTop: 24,
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
    flexGrow: 1,
    paddingTop: 9,
  },
  streakContainer: {
    paddingHorizontal: 28,
  },
});
