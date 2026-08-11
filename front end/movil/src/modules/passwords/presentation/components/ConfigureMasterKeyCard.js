import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useState } from 'react';
import { ActivityIndicator, Keyboard, Pressable, StyleSheet, Text, View } from 'react-native';

import { ApiError } from '../../../../core/http/ApiError';
import { VaultValidationError } from '../../domain/VaultCommands';
import { VaultField } from './VaultField';

function backendErrors(error) {
  return (error.fieldErrors ?? []).reduce((result, item) => {
    if (item?.field && item?.message) result[item.field] = item.message;
    return result;
  }, {});
}

export function ConfigureMasterKeyCard({ onSubmit, palette }) {
  const [form, setForm] = useState({ confirmation: '', currentPassword: '', newMasterKey: '' });
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
    setFeedback(null);
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);
    try {
      await onSubmit(form);
      setForm({ confirmation: '', currentPassword: '', newMasterKey: '' });
    } catch (error) {
      if (error instanceof VaultValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendErrors(error));
        setFeedback(error.code === 'REAUTHENTICATION_FAILED'
          ? 'La contraseña actual de tu cuenta es incorrecta.'
          : error.message);
      } else {
        setFeedback('No fue posible crear la Master Key.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={[styles.card, { backgroundColor: palette.surface, shadowColor: palette.shadow }]}> 
      <View style={styles.heading}>
        <MaterialCommunityIcons color={palette.brandDeep} name="lock" size={65} />
        <Text style={[styles.title, { color: palette.brandDeep }]}>Crear Master Key</Text>
      </View>
      <Text style={[styles.description, { color: palette.textMuted }]}> 
        Protege tu bóveda con una frase independiente. Para crearla, el servidor debe confirmar primero la contraseña actual de tu cuenta.
      </Text>
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
        error={errors.newMasterKey}
        label="Master Key"
        maxLength={128}
        onChangeText={(value) => update('newMasterKey', value)}
        palette={palette}
        secure
        value={form.newMasterKey}
      />
      <Text style={[styles.requirement, { color: palette.textMuted }]}>Entre 15 y 128 caracteres.</Text>
      <VaultField
        autoComplete="off"
        error={errors.confirmation}
        label="Confirmar Master Key"
        maxLength={128}
        onChangeText={(value) => update('confirmation', value)}
        onSubmitEditing={submit}
        palette={palette}
        returnKeyType="done"
        secure
        value={form.confirmation}
      />
      <Text style={[styles.notice, { color: palette.text }]}> 
        La Master Key no se puede recuperar por correo. Guárdala en un lugar seguro y diferente de esta aplicación.
      </Text>
      <Pressable
        accessibilityRole="button"
        disabled={submitting}
        onPress={submit}
        style={({ pressed }) => [
          styles.primaryButton,
          { backgroundColor: palette.brandSecondary },
          pressed && { backgroundColor: palette.brandPressed },
          submitting && styles.disabled,
        ]}
      >
        {submitting ? (
          <ActivityIndicator color={palette.surfaceOnBrand} />
        ) : (
          <Text style={[styles.primaryText, { color: palette.surfaceOnBrand }]}>Crear</Text>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 22,
    elevation: 3,
    maxWidth: 650,
    padding: 25,
    shadowOffset: { height: 2, width: -3 },
    shadowOpacity: 0.13,
    shadowRadius: 5,
    width: '100%',
  },
  description: {
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 18,
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
  heading: {
    alignItems: 'center',
    flexDirection: 'row',
    marginBottom: 14,
  },
  notice: {
    fontSize: 13,
    lineHeight: 19,
    marginBottom: 20,
    textAlign: 'center',
  },
  primaryButton: {
    alignItems: 'center',
    alignSelf: 'center',
    borderRadius: 22,
    justifyContent: 'center',
    minHeight: 49,
    minWidth: 150,
    paddingHorizontal: 28,
  },
  primaryText: {
    fontSize: 20,
    fontWeight: '800',
  },
  requirement: {
    fontSize: 12,
    marginBottom: 11,
    marginTop: -11,
  },
  title: {
    flex: 1,
    fontSize: 25,
    fontWeight: '900',
    marginLeft: 10,
  },
});
