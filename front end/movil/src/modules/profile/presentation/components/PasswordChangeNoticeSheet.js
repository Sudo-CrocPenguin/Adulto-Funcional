import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export function PasswordChangeNoticeSheet({ onClose, palette, visible }) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} statusBarTranslucent transparent visible={visible}>
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cerrar información" onPress={onClose} style={StyleSheet.absoluteFill} />
        <View style={[styles.sheet, { backgroundColor: palette.surface }]}> 
          <SafeAreaView edges={['bottom']}>
            <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
            <MaterialCommunityIcons color={palette.brandDeep} name="lock-clock" size={48} style={styles.icon} />
            <Text style={[styles.title, { color: palette.text }]}>Cambio de contraseña</Text>
            <Text style={[styles.body, { color: palette.textMuted }]}> 
              El backend actual todavía no ofrece una operación segura para cambiar la contraseña de acceso. Esta versión no simula el cambio ni modifica tu sesión local.
            </Text>
            <Text style={[styles.tip, { backgroundColor: palette.brandSoft, color: palette.brandDeep }]}> 
              La Master Key de la bóveda se administra por separado desde la sección Contraseñas.
            </Text>
            <Pressable accessibilityRole="button" onPress={onClose} style={({ pressed }) => [styles.button, { backgroundColor: palette.brandSecondary }, pressed && styles.pressed]}>
              <Text style={[styles.buttonText, { color: palette.surfaceOnBrand }]}>Entendido</Text>
            </Pressable>
          </SafeAreaView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  body: { fontSize: 15, lineHeight: 23, marginTop: 13, textAlign: 'center' },
  button: { alignItems: 'center', borderRadius: 9, justifyContent: 'center', marginTop: 20, minHeight: 52 },
  buttonText: { fontSize: 16, fontWeight: '900' },
  handle: { alignSelf: 'center', borderRadius: 4, height: 7, marginBottom: 18, width: 90 },
  icon: { alignSelf: 'center' },
  overlay: { flex: 1, justifyContent: 'flex-end' },
  pressed: { opacity: 0.62 },
  sheet: { borderTopLeftRadius: 25, borderTopRightRadius: 25, paddingHorizontal: 25, paddingTop: 14 },
  tip: { borderRadius: 10, fontSize: 13, fontWeight: '700', lineHeight: 20, marginTop: 18, padding: 13, textAlign: 'center' },
  title: { fontSize: 24, fontWeight: '900', marginTop: 10, textAlign: 'center' },
});
