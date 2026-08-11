import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Keyboard,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '../../../../core/http/ApiError';
import { ProfileValidationError } from '../../domain/UpdateProfileDraft';
import { ProfileField } from './ProfileField';

function backendErrors(error) {
  return (error.fieldErrors ?? []).reduce((result, item) => {
    if (item?.field && item?.message) result[item.field] = item.message;
    return result;
  }, {});
}

export function EditProfileSheet({ onClose, onSubmit, palette, profile, visible }) {
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [form, setForm] = useState({ email: '', lastnames: '', names: '', phone: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!visible) return;
    setForm({
      email: profile.email,
      lastnames: profile.lastnames,
      names: profile.names,
      phone: profile.phone,
    });
    setErrors({});
    setFeedback(null);
  }, [profile, visible]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined, form: undefined }));
    setFeedback(null);
  }

  function close() {
    if (submitting) return;
    Keyboard.dismiss();
    onClose();
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);
    try {
      await onSubmit(form);
      onClose();
    } catch (error) {
      if (error instanceof ProfileValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendErrors(error));
        setFeedback(error.message);
      } else {
        setFeedback('No fue posible actualizar tu perfil.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal animationType="slide" onRequestClose={close} statusBarTranslucent transparent visible={visible}>
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cerrar edición de perfil" onPress={close} style={StyleSheet.absoluteFill} />
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.keyboardArea}>
          <View style={[styles.sheet, { backgroundColor: palette.surface }]}> 
            <SafeAreaView edges={['bottom']}>
              <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
              <Text style={[styles.title, { color: palette.text }]}>Editar perfil</Text>
              <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
                {feedback || errors.form ? (
                  <Text style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}> 
                    {errors.form || feedback}
                  </Text>
                ) : null}
                <ProfileField autoCapitalize="words" error={errors.names} label="Nombres" maxLength={50} onChangeText={(value) => update('names', value)} palette={palette} value={form.names} />
                <ProfileField autoCapitalize="words" error={errors.lastnames} label="Apellidos" maxLength={50} onChangeText={(value) => update('lastnames', value)} palette={palette} value={form.lastnames} />
                <ProfileField autoCapitalize="none" autoComplete="tel" error={errors.phone} keyboardType="phone-pad" label="Teléfono" maxLength={16} onChangeText={(value) => update('phone', value)} palette={palette} value={form.phone} />
                <ProfileField autoCapitalize="none" autoComplete="email" error={errors.email} keyboardType="email-address" label="Correo electrónico" maxLength={255} onChangeText={(value) => update('email', value)} palette={palette} value={form.email} />
                <Text style={[styles.note, { color: palette.textMuted }]}>El teléfono debe incluir código de país, por ejemplo +573001234567.</Text>
                <View style={styles.actions}>
                  <Pressable accessibilityRole="button" disabled={submitting} onPress={close} style={({ pressed }) => [styles.cancelButton, { backgroundColor: palette.cardMuted }, pressed && styles.pressed]}>
                    <Text style={[styles.cancelText, { color: palette.text }]}>Cancelar</Text>
                  </Pressable>
                  <Pressable accessibilityRole="button" disabled={submitting} onPress={submit} style={({ pressed }) => [styles.saveButton, { backgroundColor: palette.brandSecondary }, pressed && styles.pressed, submitting && styles.disabled]}>
                    {submitting ? <ActivityIndicator color={palette.surfaceOnBrand} /> : <Text style={[styles.saveText, { color: palette.surfaceOnBrand }]}>Guardar</Text>}
                  </Pressable>
                </View>
              </ScrollView>
            </SafeAreaView>
          </View>
        </KeyboardAvoidingView>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  actions: { flexDirection: 'row', gap: 12, justifyContent: 'flex-end', marginTop: 18 },
  cancelButton: { alignItems: 'center', borderRadius: 9, justifyContent: 'center', minHeight: 50, paddingHorizontal: 19 },
  cancelText: { fontSize: 16, fontWeight: '800' },
  disabled: { opacity: 0.58 },
  feedback: { borderRadius: 8, fontSize: 13, lineHeight: 18, marginBottom: 14, padding: 9, textAlign: 'center' },
  handle: { alignSelf: 'center', borderRadius: 4, height: 7, marginBottom: 17, width: 90 },
  keyboardArea: { flex: 1, justifyContent: 'flex-end' },
  note: { fontSize: 12, lineHeight: 18 },
  overlay: { flex: 1 },
  pressed: { opacity: 0.62 },
  saveButton: { alignItems: 'center', borderRadius: 9, justifyContent: 'center', minHeight: 50, minWidth: 120, paddingHorizontal: 19 },
  saveText: { fontSize: 16, fontWeight: '900' },
  sheet: { borderTopLeftRadius: 25, borderTopRightRadius: 25, maxHeight: '92%', paddingHorizontal: 25, paddingTop: 14 },
  title: { fontSize: 25, fontWeight: '900', marginBottom: 20, textAlign: 'center' },
});
