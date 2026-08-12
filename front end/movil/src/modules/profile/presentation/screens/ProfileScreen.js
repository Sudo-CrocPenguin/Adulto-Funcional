import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
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
import { LogoutAccountError } from '../../../auth/application/LogoutAccountUseCase';
import { EditProfileSheet } from '../components/EditProfileSheet';
import { LogoutConfirmationDialog } from '../components/LogoutConfirmationDialog';
import { PasswordChangeNoticeSheet } from '../components/PasswordChangeNoticeSheet';
import { PersonalInformationCard } from '../components/PersonalInformationCard';
import { ProfileActivityCard } from '../components/ProfileActivityCard';
import { ProfileHero } from '../components/ProfileHero';

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <MaterialCommunityIcons color={palette.navigationMuted} name="account-alert-outline" size={62} />
      <Text style={[styles.errorTitle, { color: palette.text }]}>No pudimos cargar tu perfil</Text>
      <Text style={[styles.errorMessage, { color: palette.textMuted }]}>{message}</Text>
      <Pressable accessibilityRole="button" onPress={onRetry} style={({ pressed }) => [styles.retryButton, { backgroundColor: palette.brand }, pressed && styles.pressed]}>
        <Text style={[styles.retryText, { color: palette.surfaceOnBrand }]}>Reintentar</Text>
      </Pressable>
    </View>
  );
}

export function ProfileScreen({ navigation }) {
  const { loadProfile, logoutAccount, updateProfile } = useAppDependencies();
  const { closeSession, openSession, session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [changePasswordVisible, setChangePasswordVisible] = useState(false);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const [logoutVisible, setLogoutVisible] = useState(false);
  const [snapshot, setSnapshot] = useState(null);

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      setSnapshot(await loadProfile.execute(session));
    } catch (loadError) {
      setError(loadError instanceof ApiError ? loadError.message : 'Ocurrió un error inesperado. Inténtalo nuevamente.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadProfile, session]);

  useEffect(() => {
    load();
  }, [load]);

  async function submitProfile(form) {
    const profile = await updateProfile.execute(snapshot.profile, form, session);
    setSnapshot((current) => current.withProfile(profile));
    openSession(session.withProfile(profile));
    setFeedback('Perfil actualizado correctamente.');
  }

  async function logout() {
    setLoggingOut(true);
    setError(null);
    setFeedback(null);
    try {
      await logoutAccount.execute(session);
      setLogoutVisible(false);
      closeSession();
    } catch (logoutError) {
      setLogoutVisible(false);
      setError(logoutError instanceof LogoutAccountError
        ? logoutError.message
        : 'No fue posible cerrar la sesión. Inténtalo nuevamente.');
    } finally {
      setLoggingOut(false);
    }
  }

  function selectDestination(destination) {
    const destinations = {
      Compromisos: APP_ROUTES.commitments,
      Contraseñas: APP_ROUTES.passwords,
      Finanzas: APP_ROUTES.finances,
      'Gastos Fijos': APP_ROUTES.fixedExpenses,
      Inicio: APP_ROUTES.home,
    };
    if (destination === 'Perfil') return;
    if (destinations[destination]) navigation.navigate(destinations[destination]);
  }

  return (
    <SafeAreaView edges={['top', 'left', 'right']} style={[styles.safeArea, { backgroundColor: palette.background }]}> 
      <StatusBar backgroundColor={palette.brandSoft} style={isDark ? 'light' : 'dark'} />
      <AuthenticatedHeader title="Perfil" />

      {isLoading && !snapshot ? (
        <View accessibilityLabel="Cargando perfil" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Preparando tu perfil…</Text>
        </View>
      ) : error && !snapshot ? (
        <ErrorState message={error} onRetry={() => load()} palette={palette} />
      ) : (
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          refreshControl={<RefreshControl colors={[palette.brand]} onRefresh={() => load({ refresh: true })} refreshing={isRefreshing} tintColor={palette.brand} />}
          showsVerticalScrollIndicator={false}
        >
          {feedback ? <Text style={[styles.feedback, { backgroundColor: palette.successSoft, color: palette.success }]}>{feedback}</Text> : null}
          {error ? <Text style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}>{error}</Text> : null}
          <ProfileHero onEdit={() => { setFeedback(null); setEditing(true); }} palette={palette} profile={snapshot.profile} />
          <ProfileActivityCard activity={snapshot.activity} palette={palette} />
          <PersonalInformationCard palette={palette} profile={snapshot.profile} />
          <Pressable accessibilityRole="button" onPress={() => { setFeedback(null); setEditing(true); }} style={({ pressed }) => [styles.editButton, { backgroundColor: palette.brandDeep }, pressed && styles.pressed]}>
            <MaterialCommunityIcons color={palette.surfaceOnBrand} name="square-edit-outline" size={27} />
            <Text style={[styles.editText, { color: palette.surfaceOnBrand }]}>Editar perfil</Text>
          </Pressable>
          <View style={[styles.accountCard, { backgroundColor: palette.surface }]}> 
            <Text style={[styles.accountTitle, { color: palette.brandDeep }]}>CUENTA</Text>
            <Pressable accessibilityRole="button" onPress={() => setChangePasswordVisible(true)} style={({ pressed }) => [styles.accountRow, pressed && styles.pressed]}>
              <View style={[styles.accountIcon, { backgroundColor: palette.cardMuted }]}> 
                <MaterialCommunityIcons color={palette.textMuted} name="lock-outline" size={31} />
              </View>
              <View style={styles.accountTextBlock}>
                <Text style={[styles.accountLabel, { color: palette.text }]}>Cambiar contraseña</Text>
                <Text style={[styles.accountHint, { color: palette.textMuted }]}>Disponibilidad del servicio</Text>
              </View>
              <MaterialCommunityIcons color={palette.textMuted} name="chevron-right" size={32} />
            </Pressable>
            <Pressable
              accessibilityLabel="Cerrar sesión"
              accessibilityRole="button"
              onPress={() => setLogoutVisible(true)}
              style={({ pressed }) => [
                styles.accountRow,
                { borderTopColor: palette.border },
                styles.logoutRow,
                pressed && styles.pressed,
              ]}
            >
              <View style={[styles.accountIcon, { backgroundColor: palette.errorSoft }]}>
                <MaterialCommunityIcons color={palette.error} name="logout-variant" size={31} />
              </View>
              <View style={styles.accountTextBlock}>
                <Text style={[styles.accountLabel, { color: palette.error }]}>Cerrar sesión</Text>
                <Text style={[styles.accountHint, { color: palette.textMuted }]}>Salir de la cuenta en este dispositivo</Text>
              </View>
              <MaterialCommunityIcons color={palette.error} name="chevron-right" size={32} />
            </Pressable>
          </View>
        </ScrollView>
      )}

      <AppBottomNavigation activeItem="Perfil" onSelect={selectDestination} palette={palette} />
      {snapshot ? <EditProfileSheet onClose={() => setEditing(false)} onSubmit={submitProfile} palette={palette} profile={snapshot.profile} visible={editing} /> : null}
      <PasswordChangeNoticeSheet onClose={() => setChangePasswordVisible(false)} palette={palette} visible={changePasswordVisible} />
      <LogoutConfirmationDialog
        loggingOut={loggingOut}
        onCancel={() => setLogoutVisible(false)}
        onConfirm={logout}
        palette={palette}
        visible={logoutVisible}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  accountCard: { borderRadius: 20, marginHorizontal: 24, marginTop: 16, overflow: 'hidden', paddingTop: 15 },
  accountHint: { fontSize: 12, marginTop: 2 },
  accountIcon: { alignItems: 'center', borderRadius: 9, height: 54, justifyContent: 'center', width: 54 },
  accountLabel: { fontSize: 16, fontWeight: '800' },
  accountRow: { alignItems: 'center', flexDirection: 'row', minHeight: 82, paddingHorizontal: 18, paddingVertical: 12 },
  accountTextBlock: { flex: 1, marginLeft: 14 },
  accountTitle: { fontSize: 17, fontWeight: '900', paddingHorizontal: 22 },
  centerState: { alignItems: 'center', flex: 1, justifyContent: 'center', paddingHorizontal: 35 },
  editButton: { alignItems: 'center', borderRadius: 13, flexDirection: 'row', justifyContent: 'center', marginHorizontal: 28, marginTop: 16, minHeight: 62 },
  editText: { fontSize: 22, fontWeight: '900', marginLeft: 9 },
  errorMessage: { fontSize: 15, lineHeight: 22, marginTop: 8, textAlign: 'center' },
  errorTitle: { fontSize: 21, fontWeight: '800', marginTop: 12, textAlign: 'center' },
  feedback: { borderRadius: 8, marginBottom: 10, marginHorizontal: 24, padding: 10, textAlign: 'center' },
  loadingText: { fontSize: 15, marginTop: 13 },
  logoutRow: { borderTopWidth: StyleSheet.hairlineWidth },
  pressed: { opacity: 0.62 },
  retryButton: { borderRadius: 9, marginTop: 20, paddingHorizontal: 26, paddingVertical: 12 },
  retryText: { fontSize: 16, fontWeight: '800' },
  safeArea: { flex: 1 },
  scrollContent: { paddingBottom: 24 },
});
