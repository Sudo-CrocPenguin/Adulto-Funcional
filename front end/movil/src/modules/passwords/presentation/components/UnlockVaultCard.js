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

export function UnlockVaultCard({ onHelp, onUnlock, palette }) {
  const [masterKey, setMasterKey] = useState('');
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);
    try {
      await onUnlock({ masterKey });
      setMasterKey('');
    } catch (error) {
      if (error instanceof VaultValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendErrors(error));
        setFeedback(error.code === 'MASTER_KEY_INVALID'
          ? 'La Master Key es incorrecta. Tu sesión principal sigue activa.'
          : error.message);
      } else {
        setFeedback('No fue posible desbloquear la bóveda.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={[styles.card, { backgroundColor: palette.surface, shadowColor: palette.shadow }]}> 
      <MaterialCommunityIcons color={palette.brandDeep} name="lock" size={135} />
      <Text style={[styles.prompt, { color: palette.text }]}>Ingresa la Master Key</Text>
      {feedback ? (
        <Text style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}> 
          {feedback}
        </Text>
      ) : null}
      <View style={styles.field}>
        <VaultField
          autoComplete="off"
          error={errors.masterKey}
          label="Master Key"
          maxLength={128}
          onChangeText={(value) => {
            setMasterKey(value);
            setErrors({});
            setFeedback(null);
          }}
          onSubmitEditing={submit}
          palette={palette}
          returnKeyType="done"
          secure
          value={masterKey}
        />
      </View>
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
          <Text style={[styles.primaryText, { color: palette.surfaceOnBrand }]}>Ingresar</Text>
        )}
      </Pressable>
      <Pressable accessibilityRole="button" onPress={onHelp} style={({ pressed }) => pressed && styles.pressed}>
        <Text style={[styles.help, { color: palette.link }]}>¿Necesitas ayuda?</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    alignItems: 'center',
    borderRadius: 22,
    elevation: 3,
    maxWidth: 620,
    paddingBottom: 35,
    paddingHorizontal: 24,
    paddingTop: 35,
    shadowOffset: { height: 2, width: -3 },
    shadowOpacity: 0.13,
    shadowRadius: 5,
    width: '100%',
  },
  disabled: {
    opacity: 0.58,
  },
  feedback: {
    borderRadius: 8,
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 12,
    padding: 9,
    textAlign: 'center',
    width: '100%',
  },
  field: {
    width: '100%',
  },
  help: {
    fontSize: 16,
    marginTop: 18,
    textDecorationLine: 'underline',
  },
  pressed: {
    opacity: 0.62,
  },
  primaryButton: {
    alignItems: 'center',
    borderRadius: 22,
    justifyContent: 'center',
    minHeight: 48,
    minWidth: 150,
    paddingHorizontal: 25,
  },
  primaryText: {
    fontSize: 20,
    fontWeight: '800',
  },
  prompt: {
    fontSize: 21,
    marginBottom: 14,
    marginTop: 8,
  },
});
