import { ActivityIndicator, Modal, Pressable, StyleSheet, Text, View } from 'react-native';

export function LogoutConfirmationDialog({
  loggingOut,
  onCancel,
  onConfirm,
  palette,
  visible,
}) {
  return (
    <Modal
      animationType="fade"
      onRequestClose={() => {
        if (!loggingOut) {
          onCancel();
        }
      }}
      statusBarTranslucent
      transparent
      visible={visible}
    >
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}>
        <Pressable
          accessibilityLabel="Cancelar cierre de sesión"
          disabled={loggingOut}
          onPress={onCancel}
          style={StyleSheet.absoluteFill}
        />
        <View style={[styles.dialog, { backgroundColor: palette.surface }]}>
          <Text style={[styles.title, { color: palette.text }]}>¿Cerrar sesión?</Text>
          <Text style={[styles.message, { color: palette.textMuted }]}>
            Saldrás de tu cuenta en este dispositivo. Podrás volver a ingresar con tu correo y contraseña.
          </Text>
          <View style={styles.actions}>
            <Pressable
              accessibilityLabel="Cancelar cierre de sesión"
              accessibilityRole="button"
              disabled={loggingOut}
              onPress={onCancel}
              style={({ pressed }) => [
                styles.button,
                { backgroundColor: palette.cardMuted },
                pressed && styles.pressed,
              ]}
            >
              <Text style={[styles.cancelText, { color: palette.text }]}>Cancelar</Text>
            </Pressable>
            <Pressable
              accessibilityLabel="Confirmar cierre de sesión"
              accessibilityRole="button"
              disabled={loggingOut}
              onPress={onConfirm}
              style={({ pressed }) => [
                styles.button,
                { backgroundColor: palette.error },
                pressed && styles.pressed,
                loggingOut && styles.disabled,
              ]}
            >
              {loggingOut ? (
                <ActivityIndicator color={palette.surfaceOnBrand} />
              ) : (
                <Text style={[styles.logoutText, { color: palette.surfaceOnBrand }]}>Salir</Text>
              )}
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  actions: {
    flexDirection: 'row',
    gap: 11,
    justifyContent: 'flex-end',
    marginTop: 22,
  },
  button: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 49,
    minWidth: 105,
    paddingHorizontal: 17,
  },
  cancelText: {
    fontSize: 16,
    fontWeight: '800',
  },
  dialog: {
    borderRadius: 18,
    maxWidth: 420,
    padding: 24,
    width: '88%',
  },
  disabled: {
    opacity: 0.58,
  },
  logoutText: {
    fontSize: 16,
    fontWeight: '900',
  },
  message: {
    fontSize: 14,
    lineHeight: 21,
    marginTop: 10,
  },
  overlay: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.62,
  },
  title: {
    fontSize: 22,
    fontWeight: '900',
  },
});
