import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { THEME_MODES, useAppTheme } from '../../../theme/AppThemeContext';

export function ThemeSettingsSheet({ onClose, visible }) {
  const { mode, palette, selectMode } = useAppTheme();

  return (
    <Modal
      animationType="slide"
      onRequestClose={onClose}
      statusBarTranslucent
      transparent
      visible={visible}
    >
      <Pressable
        accessibilityLabel="Cerrar configuración"
        onPress={onClose}
        style={[styles.overlay, { backgroundColor: palette.overlay }]}
      >
        <Pressable
          accessible={false}
          onPress={(event) => event.stopPropagation()}
          style={[styles.sheet, { backgroundColor: palette.surface }]}
        >
          <SafeAreaView edges={['bottom']}>
            <View
              style={[
                styles.handle,
                { backgroundColor: palette.navigationMuted },
              ]}
            />
            <Text style={[styles.title, { color: palette.brandSecondary }]}>
              Configuración
            </Text>
            <Text style={[styles.sectionLabel, { color: palette.text }]}>Tema</Text>
            <Text style={[styles.description, { color: palette.textMuted }]}>
              Elige cómo quieres ver Adulto Funcional.
            </Text>
            <View style={styles.modeRow}>
              <ThemeButton
                active={mode === THEME_MODES.light}
                label="Claro"
                mode={THEME_MODES.light}
                palette={palette}
                selectMode={selectMode}
              />
              <ThemeButton
                active={mode === THEME_MODES.dark}
                label="Oscuro"
                mode={THEME_MODES.dark}
                palette={palette}
                selectMode={selectMode}
              />
            </View>
          </SafeAreaView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

function ThemeButton({ active, label, mode, palette, selectMode }) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected: active }}
      onPress={() => selectMode(mode)}
      style={({ pressed }) => [
        styles.modeButton,
        {
          backgroundColor: active ? palette.brandSecondary : palette.cardMuted,
          borderColor: active ? palette.brandSecondary : palette.border,
        },
        pressed && styles.pressed,
      ]}
    >
      <Text
        style={[
          styles.modeText,
          { color: active ? palette.surfaceOnBrand : palette.text },
        ]}
      >
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  description: {
    fontSize: 14,
    lineHeight: 20,
    marginTop: 5,
  },
  handle: {
    alignSelf: 'center',
    borderRadius: 4,
    height: 6,
    marginBottom: 23,
    opacity: 0.75,
    width: 78,
  },
  modeButton: {
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 1,
    flex: 1,
    justifyContent: 'center',
    minHeight: 58,
  },
  modeRow: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 18,
  },
  modeText: {
    fontSize: 21,
    fontWeight: '800',
  },
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  pressed: {
    opacity: 0.75,
  },
  sectionLabel: {
    fontSize: 19,
    fontWeight: '800',
    marginTop: 24,
  },
  sheet: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 28,
    paddingTop: 15,
  },
  title: {
    fontSize: 27,
    fontWeight: '800',
  },
});
