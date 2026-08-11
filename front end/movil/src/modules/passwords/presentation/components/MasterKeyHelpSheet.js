import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export function MasterKeyHelpSheet({ email, onChangeMasterKey, onClose, palette, visible }) {
  return (
    <Modal
      animationType="slide"
      onRequestClose={onClose}
      statusBarTranslucent
      transparent
      visible={visible}
    >
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cerrar ayuda" onPress={onClose} style={StyleSheet.absoluteFill} />
        <View style={[styles.sheet, { backgroundColor: palette.surface }]}> 
          <SafeAreaView edges={['bottom']}>
            <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
            <View style={styles.heading}>
              <MaterialCommunityIcons color={palette.brandDeep} name="shield-lock-outline" size={43} />
              <Text style={[styles.title, { color: palette.brandDeep }]}>Ayuda con tu Master Key</Text>
            </View>
            <Text style={[styles.lead, { color: palette.text }]}>La Master Key no puede restablecerse por correo ni mediante un código.</Text>
            <Text style={[styles.body, { color: palette.textMuted }]}> 
              El servidor conserva solamente un hash para verificarla y utiliza la clave original para descifrar tu bóveda. Enviar una clave nueva sin conocer la anterior haría inaccesibles las credenciales existentes.
            </Text>
            <View style={[styles.emailBox, { backgroundColor: palette.cardMuted }]}> 
              <MaterialCommunityIcons color={palette.navigationMuted} name="email-lock-outline" size={23} />
              <Text numberOfLines={1} style={[styles.email, { color: palette.textMuted }]}>{email || 'Cuenta autenticada'}</Text>
            </View>
            <Text style={[styles.tip, { color: palette.text }]}> 
              Si todavía recuerdas la Master Key actual, puedes cambiarla de forma segura. El backend recifrará todas tus credenciales en una sola transacción.
            </Text>
            <View style={styles.actions}>
              <Pressable
                accessibilityRole="button"
                onPress={onClose}
                style={({ pressed }) => [styles.secondaryButton, { backgroundColor: palette.cardMuted }, pressed && styles.pressed]}
              >
                <Text style={[styles.secondaryText, { color: palette.text }]}>Cerrar</Text>
              </Pressable>
              <Pressable
                accessibilityRole="button"
                onPress={onChangeMasterKey}
                style={({ pressed }) => [styles.primaryButton, { backgroundColor: palette.brandSecondary }, pressed && styles.pressed]}
              >
                <Text style={[styles.primaryText, { color: palette.surfaceOnBrand }]}>Cambiar Master Key</Text>
              </Pressable>
            </View>
          </SafeAreaView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  actions: {
    flexDirection: 'row',
    gap: 10,
    justifyContent: 'flex-end',
    marginTop: 20,
  },
  body: {
    fontSize: 14,
    lineHeight: 21,
    marginTop: 10,
  },
  email: {
    flex: 1,
    fontSize: 14,
    marginLeft: 9,
  },
  emailBox: {
    alignItems: 'center',
    borderRadius: 9,
    flexDirection: 'row',
    marginTop: 17,
    minHeight: 50,
    paddingHorizontal: 13,
  },
  handle: {
    alignSelf: 'center',
    borderRadius: 4,
    height: 7,
    marginBottom: 20,
    width: 90,
  },
  heading: {
    alignItems: 'center',
    flexDirection: 'row',
  },
  lead: {
    fontSize: 17,
    fontWeight: '800',
    lineHeight: 23,
    marginTop: 20,
  },
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  pressed: {
    opacity: 0.62,
  },
  primaryButton: {
    alignItems: 'center',
    borderRadius: 8,
    flex: 1,
    justifyContent: 'center',
    minHeight: 50,
    paddingHorizontal: 12,
  },
  primaryText: {
    fontSize: 14,
    fontWeight: '900',
    textAlign: 'center',
  },
  secondaryButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 50,
    paddingHorizontal: 18,
  },
  secondaryText: {
    fontSize: 15,
    fontWeight: '800',
  },
  sheet: {
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    paddingHorizontal: 24,
    paddingTop: 15,
  },
  tip: {
    fontSize: 14,
    lineHeight: 21,
    marginTop: 17,
  },
  title: {
    flex: 1,
    fontSize: 23,
    fontWeight: '900',
    marginLeft: 10,
  },
});
