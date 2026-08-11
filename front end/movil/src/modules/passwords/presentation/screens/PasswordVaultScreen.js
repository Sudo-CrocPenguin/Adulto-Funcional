import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
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
import { PasswordVault } from '../../domain/PasswordVault';
import { VaultAccess } from '../../domain/VaultAccess';
import { VaultSnapshot } from '../../domain/VaultSnapshot';
import { ChangeMasterKeySheet } from '../components/ChangeMasterKeySheet';
import { ConfigureMasterKeyCard } from '../components/ConfigureMasterKeyCard';
import { CredentialCard } from '../components/CredentialCard';
import { CredentialFormSheet } from '../components/CredentialFormSheet';
import { DeleteCredentialDialog } from '../components/DeleteCredentialDialog';
import { MasterKeyHelpSheet } from '../components/MasterKeyHelpSheet';
import { UnlockVaultCard } from '../components/UnlockVaultCard';

const REVEAL_DURATION_MS = 30_000;

function ErrorState({ message, onRetry, palette }) {
  return (
    <View style={styles.centerState}>
      <MaterialCommunityIcons color={palette.navigationMuted} name="shield-lock-outline" size={58} />
      <Text style={[styles.errorTitle, { color: palette.text }]}>No pudimos consultar la bóveda</Text>
      <Text style={[styles.errorMessage, { color: palette.textMuted }]}>{message}</Text>
      <Pressable
        accessibilityRole="button"
        onPress={onRetry}
        style={({ pressed }) => [styles.retryButton, { backgroundColor: palette.brand }, pressed && styles.pressed]}
      >
        <Text style={[styles.retryText, { color: palette.surfaceOnBrand }]}>Reintentar</Text>
      </Pressable>
    </View>
  );
}

function lockedSnapshot(configured = true) {
  return new VaultSnapshot({
    access: new VaultAccess({ configured, expiresAt: null, verified: false }),
    vault: PasswordVault.create(),
  });
}

function expirationLabel(expiresAt) {
  if (!expiresAt) return null;
  const date = new Date(expiresAt);
  if (Number.isNaN(date.getTime())) return null;
  return new Intl.DateTimeFormat('es-CO', { hour: '2-digit', minute: '2-digit' }).format(date);
}

export function PasswordVaultScreen({ navigation }) {
  const {
    changeMasterKey,
    configureMasterKey,
    createCredential,
    deleteCredential,
    loadVault,
    lockVault,
    revealCredential,
    updateCredential,
    verifyMasterKey,
  } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const revealTimers = useRef(new Map());
  const [changeVisible, setChangeVisible] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [editor, setEditor] = useState(null);
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [helpVisible, setHelpVisible] = useState(false);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);
  const [revealed, setRevealed] = useState({});
  const [revealingId, setRevealingId] = useState(null);
  const [snapshot, setSnapshot] = useState(null);

  const clearSecrets = useCallback(() => {
    revealTimers.current.forEach((timer) => clearTimeout(timer));
    revealTimers.current.clear();
    setRevealed({});
    setRevealingId(null);
  }, []);

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    clearSecrets();
    try {
      setSnapshot(await loadVault.execute(session));
    } catch (loadError) {
      setError(loadError instanceof ApiError
        ? loadError.message
        : 'Ocurrió un error inesperado. Inténtalo nuevamente.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [clearSecrets, loadVault, session]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => () => clearSecrets(), [clearSecrets]);

  useEffect(() => {
    if (!snapshot?.access?.isUnlockedAt() || !snapshot.access.expiresAt) return undefined;
    const remaining = new Date(snapshot.access.expiresAt).getTime() - Date.now();
    const timer = setTimeout(() => {
      clearSecrets();
      setEditor(null);
      setSnapshot(lockedSnapshot(true));
      setFeedback('La sesión de la bóveda expiró. Vuelve a ingresar tu Master Key.');
    }, Math.max(remaining, 0) + 100);
    return () => clearTimeout(timer);
  }, [clearSecrets, snapshot]);

  const unlocked = Boolean(snapshot?.access?.isUnlockedAt());
  const notifications = useMemo(
    () => unlocked ? snapshot.vault.changeNotifications() : [],
    [snapshot, unlocked],
  );

  async function recoverLockedVault(message) {
    clearSecrets();
    setEditor(null);
    setChangeVisible(false);
    setSnapshot(lockedSnapshot(true));
    setFeedback(message || 'La bóveda se bloqueó. Vuelve a ingresar tu Master Key.');
  }

  function isExpiredError(operationError) {
    return operationError instanceof ApiError && operationError.code === 'MASTER_KEY_REQUIRED';
  }

  async function unlock(form) {
    const nextSnapshot = await verifyMasterKey.execute(form, session);
    setSnapshot(nextSnapshot);
    setFeedback('Bóveda desbloqueada correctamente.');
  }

  async function configure(form) {
    const access = await configureMasterKey.execute(form, session);
    clearSecrets();
    setSnapshot(new VaultSnapshot({ access }));
    setFeedback('Master Key creada. Ingrésala para desbloquear tu bóveda.');
  }

  async function change(form) {
    const access = await changeMasterKey.execute(form, session);
    clearSecrets();
    setSnapshot(new VaultSnapshot({ access }));
    setFeedback('Master Key cambiada. Todas las sesiones quedaron bloqueadas.');
  }

  async function lock() {
    setError(null);
    try {
      const access = await lockVault.execute(session);
      clearSecrets();
      setSnapshot(new VaultSnapshot({ access }));
      setFeedback('Bóveda bloqueada correctamente.');
    } catch (operationError) {
      setError(operationError instanceof ApiError ? operationError.message : 'No fue posible bloquear la bóveda.');
    }
  }

  async function submitCredential(form) {
    try {
      const credential = editor?.credential
        ? await updateCredential.execute(editor.credential, form, session)
        : await createCredential.execute(form, session);
      setSnapshot((current) => new VaultSnapshot({
        access: current.access,
        vault: current.vault.withCredential(credential),
      }));
      setFeedback(editor?.credential ? 'Contraseña actualizada correctamente.' : 'Contraseña guardada correctamente.');
    } catch (operationError) {
      if (isExpiredError(operationError)) {
        await recoverLockedVault('La sesión de la bóveda expiró antes de guardar los cambios.');
        return;
      }
      throw operationError;
    }
  }

  function hideCredential(credentialId) {
    const timer = revealTimers.current.get(credentialId);
    if (timer) clearTimeout(timer);
    revealTimers.current.delete(credentialId);
    setRevealed((current) => {
      const next = { ...current };
      delete next[credentialId];
      return next;
    });
  }

  async function toggleReveal(credential) {
    if (Object.prototype.hasOwnProperty.call(revealed, credential.id)) {
      hideCredential(credential.id);
      return;
    }
    setRevealingId(credential.id);
    setError(null);
    try {
      const revealedCredential = await revealCredential.execute(credential.id, session);
      setRevealed((current) => ({ ...current, [credential.id]: revealedCredential.password }));
      revealTimers.current.set(
        credential.id,
        setTimeout(() => hideCredential(credential.id), REVEAL_DURATION_MS),
      );
    } catch (operationError) {
      if (isExpiredError(operationError)) {
        await recoverLockedVault('La sesión de la bóveda expiró antes de revelar la contraseña.');
      } else {
        setError(operationError instanceof ApiError ? operationError.message : 'No fue posible revelar la contraseña.');
      }
    } finally {
      setRevealingId(null);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setError(null);
    try {
      const deletedId = await deleteCredential.execute(deleteTarget.id, session);
      hideCredential(deletedId);
      setSnapshot((current) => new VaultSnapshot({
        access: current.access,
        vault: current.vault.withoutCredential(deletedId),
      }));
      setFeedback('Contraseña eliminada correctamente.');
      setDeleteTarget(null);
    } catch (operationError) {
      if (isExpiredError(operationError)) {
        setDeleteTarget(null);
        await recoverLockedVault('La sesión de la bóveda expiró antes de eliminar la contraseña.');
      } else {
        setError(operationError instanceof ApiError ? operationError.message : 'No fue posible eliminar la contraseña.');
      }
    } finally {
      setDeleting(false);
    }
  }

  function selectDestination(destination) {
    const destinations = {
      Compromisos: APP_ROUTES.commitments,
      Finanzas: APP_ROUTES.finances,
      'Gastos Fijos': APP_ROUTES.fixedExpenses,
      Inicio: APP_ROUTES.home,
      Perfil: APP_ROUTES.profile,
    };
    if (destination === 'Contraseñas') return;
    if (destinations[destination]) {
      navigation.navigate(destinations[destination]);
      return;
    }
    Alert.alert(destination, 'Esta sección será construida en la siguiente etapa.');
  }

  return (
    <SafeAreaView edges={['top', 'left', 'right']} style={[styles.safeArea, { backgroundColor: palette.background }]}> 
      <StatusBar backgroundColor={palette.brandSoft} style={isDark ? 'light' : 'dark'} />
      <AuthenticatedHeader notifications={notifications} title="Gestor de Contraseñas" />

      {isLoading && !snapshot ? (
        <View accessibilityLabel="Cargando bóveda" style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Comprobando la protección de tu bóveda…</Text>
        </View>
      ) : error && !snapshot ? (
        <ErrorState message={error} onRetry={() => load()} palette={palette} />
      ) : !snapshot.access.configured ? (
        <ScrollView
          contentContainerStyle={styles.accessContent}
          refreshControl={<RefreshControl colors={[palette.brand]} onRefresh={() => load({ refresh: true })} refreshing={isRefreshing} tintColor={palette.brand} />}
        >
          {feedback ? <Text style={[styles.screenFeedback, { backgroundColor: palette.successSoft, color: palette.success }]}>{feedback}</Text> : null}
          <ConfigureMasterKeyCard onSubmit={configure} palette={palette} />
        </ScrollView>
      ) : !unlocked ? (
        <ScrollView
          contentContainerStyle={styles.accessContent}
          refreshControl={<RefreshControl colors={[palette.brand]} onRefresh={() => load({ refresh: true })} refreshing={isRefreshing} tintColor={palette.brand} />}
        >
          {feedback ? <Text style={[styles.screenFeedback, { backgroundColor: palette.brandSoft, color: palette.brandDeep }]}>{feedback}</Text> : null}
          <UnlockVaultCard onHelp={() => setHelpVisible(true)} onUnlock={unlock} palette={palette} />
        </ScrollView>
      ) : (
        <ScrollView
          contentContainerStyle={styles.vaultContent}
          refreshControl={<RefreshControl colors={[palette.brand]} onRefresh={() => load({ refresh: true })} refreshing={isRefreshing} tintColor={palette.brand} />}
          showsVerticalScrollIndicator={false}
        >
          {feedback ? <Text style={[styles.screenFeedback, { backgroundColor: palette.successSoft, color: palette.success }]}>{feedback}</Text> : null}
          {error ? <Text style={[styles.screenFeedback, { backgroundColor: palette.errorSoft, color: palette.error }]}>{error}</Text> : null}
          <View style={styles.listHeading}>
            <View style={styles.listTitleBlock}>
              <Text style={[styles.listTitle, { color: palette.navigationMuted }]}>Mis Contraseñas</Text>
              {expirationLabel(snapshot.access.expiresAt) ? (
                <Text style={[styles.expiration, { color: palette.textMuted }]}>Abierta hasta {expirationLabel(snapshot.access.expiresAt)}</Text>
              ) : null}
            </View>
            <View style={styles.listActions}>
              <Pressable
                accessibilityLabel="Bloquear bóveda"
                accessibilityRole="button"
                onPress={lock}
                style={({ pressed }) => [styles.lockButton, { backgroundColor: palette.cardMuted }, pressed && styles.pressed]}
              >
                <MaterialCommunityIcons color={palette.brandDeep} name="lock-outline" size={22} />
              </Pressable>
              <Pressable
                accessibilityRole="button"
                onPress={() => {
                  setFeedback(null);
                  setEditor({ credential: null });
                }}
                style={({ pressed }) => [styles.addButton, { backgroundColor: palette.brandSecondary }, pressed && { backgroundColor: palette.brandPressed }]}
              >
                <MaterialCommunityIcons color={palette.surfaceOnBrand} name="plus" size={29} />
                <Text style={[styles.addText, { color: palette.surfaceOnBrand }]}>Añadir</Text>
              </Pressable>
            </View>
          </View>

          <View style={styles.credentials}>
            {snapshot.vault.credentials.length ? snapshot.vault.credentials.map((credential) => (
              <CredentialCard
                credential={credential}
                deleting={deleting && deleteTarget?.id === credential.id}
                key={credential.id}
                onDelete={() => setDeleteTarget(credential)}
                onEdit={() => {
                  hideCredential(credential.id);
                  setFeedback(null);
                  setEditor({ credential });
                }}
                onToggleReveal={() => toggleReveal(credential)}
                palette={palette}
                revealedPassword={revealed[credential.id]}
                revealing={revealingId === credential.id}
              />
            )) : (
              <View style={[styles.empty, { backgroundColor: palette.surface }]}> 
                <MaterialCommunityIcons color={palette.navigationMuted} name="key-plus" size={48} />
                <Text style={[styles.emptyTitle, { color: palette.text }]}>Tu bóveda está vacía</Text>
                <Text style={[styles.emptyText, { color: palette.textMuted }]}>Añade tu primera contraseña con el botón superior.</Text>
              </View>
            )}
          </View>
        </ScrollView>
      )}

      <AppBottomNavigation activeItem="Contraseñas" onSelect={selectDestination} palette={palette} />
      <MasterKeyHelpSheet
        email={session?.email}
        onChangeMasterKey={() => {
          setHelpVisible(false);
          setChangeVisible(true);
        }}
        onClose={() => setHelpVisible(false)}
        palette={palette}
        visible={helpVisible}
      />
      <ChangeMasterKeySheet
        onClose={() => setChangeVisible(false)}
        onSubmit={change}
        palette={palette}
        visible={changeVisible}
      />
      <CredentialFormSheet
        credential={editor?.credential ?? null}
        onClose={() => setEditor(null)}
        onSubmit={submitCredential}
        palette={palette}
        visible={Boolean(editor)}
      />
      <DeleteCredentialDialog
        credential={deleteTarget}
        deleting={deleting}
        onCancel={() => !deleting && setDeleteTarget(null)}
        onConfirm={confirmDelete}
        palette={palette}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  accessContent: {
    alignItems: 'center',
    flexGrow: 1,
    justifyContent: 'center',
    padding: 28,
  },
  addButton: {
    alignItems: 'center',
    borderRadius: 9,
    flexDirection: 'row',
    minHeight: 49,
    paddingHorizontal: 15,
  },
  addText: {
    fontSize: 16,
    fontWeight: '900',
    marginLeft: 4,
  },
  centerState: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 35,
  },
  credentials: {
    gap: 13,
    marginTop: 25,
  },
  empty: {
    alignItems: 'center',
    borderRadius: 15,
    justifyContent: 'center',
    minHeight: 210,
    padding: 24,
  },
  emptyText: {
    fontSize: 14,
    lineHeight: 20,
    marginTop: 7,
    textAlign: 'center',
  },
  emptyTitle: {
    fontSize: 19,
    fontWeight: '900',
    marginTop: 11,
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
  expiration: {
    fontSize: 11,
    marginTop: 3,
  },
  listActions: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 9,
  },
  listHeading: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  listTitle: {
    fontSize: 27,
    fontWeight: '900',
  },
  listTitleBlock: {
    flex: 1,
    marginRight: 10,
  },
  loadingText: {
    fontSize: 15,
    marginTop: 13,
    textAlign: 'center',
  },
  lockButton: {
    alignItems: 'center',
    borderRadius: 9,
    height: 49,
    justifyContent: 'center',
    width: 49,
  },
  pressed: {
    opacity: 0.62,
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
  screenFeedback: {
    borderRadius: 8,
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 14,
    padding: 9,
    textAlign: 'center',
    width: '100%',
  },
  vaultContent: {
    padding: 18,
    paddingBottom: 35,
  },
});
