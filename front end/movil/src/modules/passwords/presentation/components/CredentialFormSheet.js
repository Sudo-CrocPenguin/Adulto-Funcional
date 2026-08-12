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
import { CredentialValidationError } from '../../domain/CredentialDraft';
import { passwordStrength } from '../../domain/VaultCredential';
import { VaultField } from './VaultField';

function backendErrors(error) {
  return (error.fieldErrors ?? []).reduce((result, item) => {
    if (item?.field && item?.message) result[item.field] = item.message;
    return result;
  }, {});
}

function strengthColors(strength, palette) {
  if (strength.level === 3) return { background: palette.successSoft, text: palette.success };
  if (strength.level === 2) return { background: palette.warningSoft, text: palette.warningText };
  if (strength.level === 1) return { background: palette.errorSoft, text: palette.error };
  return { background: palette.cardMuted, text: palette.navigationMuted };
}

export function CredentialFormSheet({ credential, onClose, onSubmit, palette, visible }) {
  const editing = Boolean(credential);
  const [form, setForm] = useState({ applicationName: '', password: '' });
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const strength = passwordStrength(form.password);
  const colors = strengthColors(strength, palette);

  useEffect(() => {
    if (visible) {
      setForm({
        applicationName: credential?.applicationName ?? '',
        password: '',
      });
      setErrors({});
      setFeedback(null);
    }
  }, [credential, visible]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined, form: undefined }));
    setFeedback(null);
  }

  function close() {
    if (submitting) return;
    Keyboard.dismiss();
    setForm({ applicationName: '', password: '' });
    onClose();
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);
    try {
      await onSubmit(form);
      setForm({ applicationName: '', password: '' });
      onClose();
    } catch (error) {
      if (error instanceof CredentialValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendErrors(error));
        setFeedback(error.message);
      } else {
        setFeedback(`No fue posible ${editing ? 'actualizar' : 'guardar'} la contraseña.`);
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal animationType="slide" onRequestClose={close} statusBarTranslucent transparent visible={visible}>
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cerrar formulario de contraseña" onPress={close} style={StyleSheet.absoluteFill} />
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.keyboardArea}>
          <View style={[styles.sheet, { backgroundColor: palette.surface }]}> 
            <SafeAreaView edges={['bottom']}>
              <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
              <Text style={[styles.title, { color: palette.text }]}>{editing ? 'Editar Contraseña' : 'Nueva Contraseña'}</Text>
              <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
                {feedback || errors.form ? (
                  <Text style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}> 
                    {errors.form || feedback}
                  </Text>
                ) : null}
                <VaultField
                  autoCapitalize="words"
                  autoComplete="off"
                  error={errors.applicationName}
                  label="Nombre"
                  maxLength={35}
                  onChangeText={(value) => update('applicationName', value)}
                  palette={palette}
                  placeholder="Ej. Netflix"
                  value={form.applicationName}
                />
                <View style={styles.passwordHeading}>
                  <Text style={[styles.strengthLabel, { color: palette.textMuted }]}>Fortaleza estimada</Text>
                  <View style={[styles.strengthBadge, { backgroundColor: colors.background }]}> 
                    <Text style={[styles.strengthText, { color: colors.text }]}>{strength.label}</Text>
                  </View>
                </View>
                <VaultField
                  autoComplete="off"
                  error={errors.password}
                  label={editing ? 'Nueva contraseña (opcional)' : 'Contraseña a proteger'}
                  maxLength={2032}
                  onChangeText={(value) => update('password', value)}
                  onSubmitEditing={submit}
                  palette={palette}
                  placeholder={editing ? 'Déjala vacía para conservar la actual' : 'Escribe la contraseña'}
                  returnKeyType="done"
                  secure
                  value={form.password}
                />
                <Text style={[styles.securityNote, { color: palette.textMuted }]}> 
                  El secreto se envía únicamente al backend autenticado, donde se cifra antes de almacenarse.
                </Text>
                <View style={styles.actions}>
                  <Pressable
                    accessibilityRole="button"
                    disabled={submitting}
                    onPress={close}
                    style={({ pressed }) => [styles.cancelButton, { backgroundColor: palette.cardMuted }, pressed && styles.pressed]}
                  >
                    <Text style={[styles.cancelText, { color: palette.text }]}>Cancelar</Text>
                  </Pressable>
                  <Pressable
                    accessibilityRole="button"
                    disabled={submitting}
                    onPress={submit}
                    style={({ pressed }) => [styles.saveButton, { backgroundColor: palette.brandSecondary }, pressed && styles.pressed, submitting && styles.disabled]}
                  >
                    {submitting ? <ActivityIndicator color={palette.surfaceOnBrand} /> : (
                      <Text style={[styles.saveText, { color: palette.surfaceOnBrand }]}>{editing ? 'Actualizar' : 'Guardar'}</Text>
                    )}
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
  actions: {
    flexDirection: 'row',
    gap: 12,
    justifyContent: 'flex-end',
    marginTop: 10,
  },
  cancelButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 50,
    paddingHorizontal: 18,
  },
  cancelText: {
    fontSize: 17,
    fontWeight: '800',
  },
  disabled: {
    opacity: 0.58,
  },
  feedback: {
    borderRadius: 8,
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 14,
    padding: 9,
    textAlign: 'center',
  },
  handle: {
    alignSelf: 'center',
    borderRadius: 4,
    height: 7,
    marginBottom: 17,
    width: 90,
  },
  keyboardArea: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  overlay: {
    flex: 1,
  },
  passwordHeading: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  pressed: {
    opacity: 0.62,
  },
  saveButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 50,
    minWidth: 125,
    paddingHorizontal: 18,
  },
  saveText: {
    fontSize: 17,
    fontWeight: '900',
  },
  securityNote: {
    fontSize: 12,
    lineHeight: 18,
    marginTop: -4,
  },
  sheet: {
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    maxHeight: '91%',
    paddingHorizontal: 26,
    paddingTop: 14,
  },
  strengthBadge: {
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  strengthLabel: {
    fontSize: 12,
    fontWeight: '700',
  },
  strengthText: {
    fontSize: 12,
    fontWeight: '900',
  },
  title: {
    fontSize: 24,
    fontWeight: '900',
    marginBottom: 22,
    textAlign: 'center',
  },
});
