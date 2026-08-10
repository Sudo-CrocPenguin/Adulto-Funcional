import { useState } from 'react';
import {
  Alert,
  Keyboard,
  Pressable,
  StyleSheet,
  Text,
} from 'react-native';

import { AUTH_ROUTES } from '../../../../navigation/routes';
import { colors } from '../../../../shared/theme/tokens';
import { AuthScreenLayout } from '../components/AuthScreenLayout';
import { FormField } from '../components/FormField';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function PasswordRecoveryScreen({ navigation }) {
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);

  function submit() {
    Keyboard.dismiss();
    const normalizedEmail = email.trim();

    if (!EMAIL_PATTERN.test(normalizedEmail)) {
      setError('Escribe un correo electrónico válido.');
      return;
    }

    setError(null);
    Alert.alert(
      'Recuperación no disponible',
      'El backend todavía no ofrece el envío de instrucciones para restablecer la contraseña.',
    );
  }

  return (
    <AuthScreenLayout cardStyle={styles.card}>
      <Text accessibilityRole="header" style={styles.title}>
        Recuperar contraseña
      </Text>
      <Text style={styles.description}>
        Ingresa tu correo y te enviaremos instrucciones para restablecer tu
        contraseña
      </Text>

      <FormField
        autoComplete="email"
        error={error}
        keyboardType="email-address"
        label="Correo Electrónico"
        labelStyle={styles.fieldLabel}
        onChangeText={(value) => {
          setEmail(value);
          setError(null);
        }}
        onSubmitEditing={submit}
        returnKeyType="send"
        textContentType="emailAddress"
        value={email}
      />

      <Pressable
        accessibilityRole="button"
        onPress={submit}
        style={({ pressed }) => [
          styles.submitButton,
          pressed && styles.submitButtonPressed,
        ]}
      >
        <Text style={styles.submitText}>Enviar</Text>
      </Pressable>

      <Pressable
        accessibilityRole="link"
        hitSlop={8}
        onPress={() => navigation.navigate(AUTH_ROUTES.login)}
        style={styles.backButton}
      >
        <Text style={styles.backText}>Volver al inicio de sesión</Text>
      </Pressable>
    </AuthScreenLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    paddingBottom: 38,
    paddingHorizontal: 28,
    paddingTop: 31,
  },
  title: {
    color: colors.brand,
    fontSize: 26,
    fontWeight: '800',
    lineHeight: 34,
    textAlign: 'center',
  },
  description: {
    color: colors.text,
    fontSize: 16,
    lineHeight: 23,
    marginBottom: 26,
    marginTop: 28,
    textAlign: 'center',
  },
  fieldLabel: {
    color: colors.brand,
  },
  submitButton: {
    alignItems: 'center',
    alignSelf: 'center',
    backgroundColor: colors.brand,
    borderRadius: 17,
    height: 53,
    justifyContent: 'center',
    marginTop: 14,
    width: '72%',
  },
  submitButtonPressed: {
    backgroundColor: colors.brandPressed,
  },
  submitText: {
    color: colors.surface,
    fontSize: 21,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  backButton: {
    alignSelf: 'center',
    marginTop: 27,
    padding: 4,
  },
  backText: {
    color: colors.brand,
    fontSize: 16,
    lineHeight: 23,
  },
});

