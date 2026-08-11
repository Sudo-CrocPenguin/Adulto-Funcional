import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { UPDATE_PHASES } from '../application/EnsureLatestUpdateUseCase';

const COPY = Object.freeze({
  [UPDATE_PHASES.checking]: Object.freeze({
    icon: 'cloud-search-outline',
    title: 'Comprobando actualización',
    message: 'Estamos verificando que tengas la versión más reciente y segura de Adulto Funcional.',
  }),
  [UPDATE_PHASES.downloading]: Object.freeze({
    icon: 'cloud-download-outline',
    title: 'Actualización obligatoria',
    message: 'Encontramos una versión nueva. Debe descargarse antes de continuar.',
  }),
  [UPDATE_PHASES.restarting]: Object.freeze({
    icon: 'restart',
    title: 'Aplicando actualización',
    message: 'La descarga terminó. La aplicación se reiniciará automáticamente.',
  }),
  blocked: Object.freeze({
    icon: 'cloud-alert-outline',
    title: 'Necesitas actualizar',
    message: 'No pudimos comprobar o descargar la actualización. Conéctate a internet e inténtalo nuevamente.',
  }),
});

export function MandatoryUpdateScreen({ onRetry, palette, phase }) {
  const content = COPY[phase] ?? COPY[UPDATE_PHASES.checking];
  const blocked = phase === 'blocked';

  return (
    <SafeAreaView style={[styles.safeArea, { backgroundColor: palette.brandDeep }]}> 
      <StatusBar backgroundColor={palette.brandDeep} style="light" />
      <View style={styles.content}>
        <View style={[styles.iconShell, { backgroundColor: palette.surface }]}> 
          <MaterialCommunityIcons color={blocked ? palette.error : palette.brandDeep} name={content.icon} size={72} />
        </View>
        <Text accessibilityRole="header" style={[styles.title, { color: palette.surfaceOnBrand }]}>{content.title}</Text>
        <Text style={styles.message}>{content.message}</Text>
        {blocked ? (
          <Pressable accessibilityRole="button" onPress={onRetry} style={({ pressed }) => [styles.retryButton, { backgroundColor: palette.surface }, pressed && styles.pressed]}>
            <MaterialCommunityIcons color={palette.brandDeep} name="refresh" size={23} />
            <Text style={[styles.retryText, { color: palette.brandDeep }]}>Reintentar actualización</Text>
          </Pressable>
        ) : (
          <View accessibilityLabel="Actualización en curso" style={styles.progress}>
            <ActivityIndicator color={palette.surfaceOnBrand} size="large" />
            <Text style={styles.progressText}>No cierres la aplicación</Text>
          </View>
        )}
      </View>
      <Text style={styles.footer}>Adulto Funcional · Actualizaciones seguras</Text>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  content: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  footer: {
    color: '#D4E0EF',
    fontSize: 12,
    paddingBottom: 22,
    textAlign: 'center',
  },
  iconShell: {
    alignItems: 'center',
    borderRadius: 62,
    height: 124,
    justifyContent: 'center',
    width: 124,
  },
  message: {
    color: '#E7EEF8',
    fontSize: 16,
    lineHeight: 24,
    marginTop: 14,
    maxWidth: 430,
    textAlign: 'center',
  },
  pressed: {
    opacity: 0.65,
  },
  progress: {
    alignItems: 'center',
    marginTop: 34,
  },
  progressText: {
    color: '#D4E0EF',
    fontSize: 13,
    marginTop: 12,
  },
  retryButton: {
    alignItems: 'center',
    borderRadius: 12,
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 30,
    minHeight: 56,
    paddingHorizontal: 22,
  },
  retryText: {
    fontSize: 16,
    fontWeight: '900',
    marginLeft: 8,
  },
  safeArea: {
    flex: 1,
  },
  title: {
    fontSize: 27,
    fontWeight: '900',
    marginTop: 30,
    textAlign: 'center',
  },
});
