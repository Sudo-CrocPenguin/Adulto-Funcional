import { ActivityIndicator, Modal, Pressable, StyleSheet, Text, View } from 'react-native';

export function DeleteCredentialDialog({ credential, deleting, onCancel, onConfirm, palette }) {
  return (
    <Modal animationType="fade" onRequestClose={onCancel} statusBarTranslucent transparent visible={Boolean(credential)}>
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cancelar eliminación" onPress={onCancel} style={StyleSheet.absoluteFill} />
        <View style={[styles.dialog, { backgroundColor: palette.surface }]}> 
          <Text style={[styles.title, { color: palette.text }]}>Eliminar contraseña</Text>
          <Text style={[styles.message, { color: palette.textMuted }]}> 
            Se eliminará permanentemente la credencial de {credential?.applicationName}. Esta acción no se puede deshacer.
          </Text>
          <View style={styles.actions}>
            <Pressable
              accessibilityRole="button"
              disabled={deleting}
              onPress={onCancel}
              style={({ pressed }) => [styles.button, { backgroundColor: palette.cardMuted }, pressed && styles.pressed]}
            >
              <Text style={[styles.cancelText, { color: palette.text }]}>Cancelar</Text>
            </Pressable>
            <Pressable
              accessibilityRole="button"
              disabled={deleting}
              onPress={onConfirm}
              style={({ pressed }) => [styles.button, { backgroundColor: palette.error }, pressed && styles.pressed, deleting && styles.disabled]}
            >
              {deleting ? <ActivityIndicator color={palette.surfaceOnBrand} /> : (
                <Text style={[styles.deleteText, { color: palette.surfaceOnBrand }]}>Eliminar</Text>
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
  deleteText: {
    fontSize: 16,
    fontWeight: '900',
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
