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
import { VaultValidationError } from '../../domain/VaultCommands';
import { VaultField } from './VaultField';

const EMPTY_FORM = Object.freeze({
  confirmation: '',
  currentMasterKey: '',
  currentPassword: '',
  newMasterKey: '',
});

function backendErrors(error) {
  return (error.fieldErrors ?? []).reduce((result, item) => {
    if (item?.field && item?.message) result[item.field] = item.message;
    return result;
  }, {});
}

export function ChangeMasterKeySheet({ onClose, onSubmit, palette, visible }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (visible) {
      setForm(EMPTY_FORM);
      setErrors({});
      setFeedback(null);
    }
  }, [visible]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
    setFeedback(null);
  }

  function close() {
    if (submitting) return;
    Keyboard.dismiss();
    setForm(EMPTY_FORM);
    onClose();
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);
    try {
      await onSubmit(form);
      setForm(EMPTY_FORM);
      onClose();
    } catch (error) {
      if (error instanceof VaultValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendErrors(error));
        if (error.code === 'REAUTHENTICATION_FAILED') {
          setFeedback('La contraseña actual de tu cuenta es incorrecta.');
        } else if (error.code === 'MASTER_KEY_INVALID') {
          setFeedback('La Master Key actual es incorrecta.');
        } else {
          setFeedback(error.message);
        }
      } else {
        setFeedback('No fue posible cambiar la Master Key.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal animationType="slide" onRequestClose={close} statusBarTranslucent transparent visible={visible}>
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}> 
        <Pressable accessibilityLabel="Cerrar cambio de Master Key" onPress={close} style={StyleSheet.absoluteFill} />
        <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.keyboardArea}>
          <View style={[styles.sheet, { backgroundColor: palette.surface }]}> 
            <SafeAreaView edges={['bottom']}>
              <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
              <Text style={[styles.title, { color: palette.brandDeep }]}>Cambiar Master Key</Text>
              <Text style={[styles.description, { color: palette.textMuted }]}> 
                Todas las credenciales se recifrarán de forma transaccional. Al terminar, la bóveda quedará bloqueada en todas tus sesiones.
              </Text>
              <ScrollView keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
                {feedback ? (
                  <Text style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}>{feedback}</Text>
                ) : null}
                <VaultField
                  autoComplete="current-password"
                  error={errors.currentPassword}
                  label="Contraseña actual de la cuenta"
                  maxLength={128}
                  onChangeText={(value) => update('currentPassword', value)}
                  palette={palette}
                  secure
                  value={form.currentPassword}
                />
                <VaultField
                  autoComplete="off"
                  error={errors.currentMasterKey}
                  label="Master Key actual"
                  maxLength={128}
                  onChangeText={(value) => update('currentMasterKey', value)}
                  palette={palette}
                  secure
                  value={form.currentMasterKey}
                />
                <VaultField
                  autoComplete="off"
                  error={errors.newMasterKey}
                  label="Nueva Master Key"
                  maxLength={128}
                  onChangeText={(value) => update('newMasterKey', value)}
                  palette={palette}
                  secure
                  value={form.newMasterKey}
                />
                <VaultField
                  autoComplete="off"
                  error={errors.confirmation}
                  label="Confirmar nueva Master Key"
                  maxLength={128}
                  onChangeText={(value) => update('confirmation', value)}
                  onSubmitEditing={submit}
                  palette={palette}
                  secure
                  value={form.confirmation}
                />
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
                      <Text style={[styles.saveText, { color: palette.surfaceOnBrand }]}>Cambiar</Text>
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
    marginTop: 7,
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
  description: {
    fontSize: 13,
    lineHeight: 19,
    marginBottom: 17,
    textAlign: 'center',
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
  sheet: {
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    maxHeight: '94%',
    paddingHorizontal: 25,
    paddingTop: 14,
  },
  title: {
    fontSize: 25,
    fontWeight: '900',
    marginBottom: 8,
    textAlign: 'center',
  },
});
