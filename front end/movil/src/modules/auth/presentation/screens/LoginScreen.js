import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRef, useState } from 'react';
import {
  ActivityIndicator,
  Keyboard,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { useAppDependencies } from '../../../../composition/AppDependenciesContext';
import { ApiError } from '../../../../core/http/ApiError';
import { AUTH_ROUTES } from '../../../../navigation/routes';
import { colors } from '../../../../shared/theme/tokens';
import { SessionPersistenceError } from '../../application/SessionPersistenceError';
import { LoginValidationError } from '../../domain/LoginCommand';
import { AuthScreenLayout } from '../components/AuthScreenLayout';
import { FormField } from '../components/FormField';

const INITIAL_FORM = Object.freeze({
  email: '',
  password: '',
  rememberMe: false,
});

function backendFieldErrors(error) {
  return error.fieldErrors.reduce((result, fieldError) => {
    if (fieldError?.field && fieldError?.message) {
      result[fieldError.field] = fieldError.message;
    }
    return result;
  }, {});
}

export function LoginScreen({ navigation }) {
  const { loginAccount } = useAppDependencies();
  const [form, setForm] = useState(INITIAL_FORM);
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [isSubmitting, setSubmitting] = useState(false);
  const passwordRef = useRef(null);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => {
      if (!current[field]) {
        return current;
      }
      const next = { ...current };
      delete next[field];
      return next;
    });
    setFeedback(null);
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);

    try {
      const session = await loginAccount.execute(form);
      setForm((current) => ({ ...current, password: '' }));
      setFeedback({
        type: 'success',
        message: `Sesión iniciada. ¡Hola, ${session.names}!`,
      });
    } catch (error) {
      if (error instanceof LoginValidationError) {
        setErrors(error.fieldErrors);
        setFeedback({ type: 'error', message: error.message });
      } else if (error instanceof ApiError) {
        setErrors(backendFieldErrors(error));
        setFeedback({ type: 'error', message: error.message });
      } else if (error instanceof SessionPersistenceError) {
        setForm((current) => ({ ...current, password: '' }));
        setFeedback({
          type: 'error',
          message:
            'Ingresaste correctamente, pero no pudimos actualizar la sesión segura.',
        });
      } else {
        setFeedback({
          type: 'error',
          message: 'Ocurrió un error inesperado. Inténtalo nuevamente.',
        });
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthScreenLayout cardStyle={styles.card}>
      <Text accessibilityRole="header" style={styles.title}>
        Iniciar Sesión
      </Text>

      {feedback ? (
        <View
          accessibilityLiveRegion="polite"
          style={[
            styles.feedback,
            feedback.type === 'success'
              ? styles.feedbackSuccess
              : styles.feedbackError,
          ]}
        >
          <Text
            style={[
              styles.feedbackText,
              feedback.type === 'success'
                ? styles.feedbackTextSuccess
                : styles.feedbackTextError,
            ]}
          >
            {feedback.message}
          </Text>
        </View>
      ) : null}

      <FormField
        autoComplete="email"
        blurOnSubmit={false}
        error={errors.email}
        keyboardType="email-address"
        label="Correo electrónico"
        onChangeText={(value) => updateField('email', value)}
        onSubmitEditing={() => passwordRef.current?.focus()}
        returnKeyType="next"
        textContentType="emailAddress"
        value={form.email}
      />
      <FormField
        ref={passwordRef}
        autoComplete="password"
        error={errors.password}
        label="Contraseña"
        onChangeText={(value) => updateField('password', value)}
        onSubmitEditing={submit}
        returnKeyType="done"
        secure
        textContentType="password"
        value={form.password}
      />

      <Pressable
        accessibilityRole="checkbox"
        accessibilityState={{ checked: form.rememberMe }}
        hitSlop={6}
        onPress={() => updateField('rememberMe', !form.rememberMe)}
        style={styles.rememberRow}
      >
        <View style={[styles.checkbox, form.rememberMe && styles.checkboxActive]}>
          {form.rememberMe ? (
            <MaterialCommunityIcons
              color={colors.surface}
              name="check"
              size={20}
            />
          ) : null}
        </View>
        <Text style={styles.rememberLabel}>Recuérdame</Text>
      </Pressable>

      <Pressable
        accessibilityRole="button"
        disabled={isSubmitting}
        onPress={submit}
        style={({ pressed }) => [
          styles.submitButton,
          pressed && styles.submitButtonPressed,
          isSubmitting && styles.submitButtonDisabled,
        ]}
      >
        {isSubmitting ? (
          <ActivityIndicator color={colors.surface} size="small" />
        ) : (
          <Text style={styles.submitText}>Iniciar Sesión</Text>
        )}
      </Pressable>

      <Pressable
        accessibilityRole="link"
        hitSlop={8}
        onPress={() => navigation.navigate(AUTH_ROUTES.passwordRecovery)}
        style={styles.recoveryLinkButton}
      >
        <Text style={styles.recoveryLink}>¿Olvidaste tu contraseña?</Text>
      </Pressable>

      <View style={styles.registerRow}>
        <Text style={styles.registerQuestion}>¿No tienes cuenta? </Text>
        <Pressable
          accessibilityRole="link"
          hitSlop={8}
          onPress={() => navigation.navigate(AUTH_ROUTES.register)}
        >
          <Text style={styles.registerLink}>Registrar</Text>
        </Pressable>
      </View>
    </AuthScreenLayout>
  );
}

const styles = StyleSheet.create({
  card: {
    paddingBottom: 27,
    paddingHorizontal: 26,
    paddingTop: 30,
  },
  title: {
    color: colors.text,
    fontSize: 29,
    fontWeight: '800',
    lineHeight: 36,
    marginBottom: 38,
    textAlign: 'center',
  },
  feedback: {
    borderRadius: 12,
    marginBottom: 17,
    paddingHorizontal: 13,
    paddingVertical: 10,
  },
  feedbackError: {
    backgroundColor: colors.errorSoft,
  },
  feedbackSuccess: {
    backgroundColor: colors.successSoft,
  },
  feedbackText: {
    fontSize: 14,
    lineHeight: 19,
    textAlign: 'center',
  },
  feedbackTextError: {
    color: colors.error,
  },
  feedbackTextSuccess: {
    color: colors.success,
  },
  rememberRow: {
    alignItems: 'center',
    alignSelf: 'flex-start',
    flexDirection: 'row',
    marginBottom: 20,
    marginLeft: 14,
    marginTop: 3,
    minHeight: 44,
  },
  checkbox: {
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderColor: '#9CA3AF',
    borderRadius: 4,
    borderWidth: 1.5,
    height: 27,
    justifyContent: 'center',
    width: 27,
  },
  checkboxActive: {
    backgroundColor: colors.brand,
    borderColor: colors.brand,
  },
  rememberLabel: {
    color: colors.text,
    fontSize: 17,
    marginLeft: 9,
  },
  submitButton: {
    alignItems: 'center',
    backgroundColor: colors.brand,
    borderRadius: 14,
    height: 57,
    justifyContent: 'center',
    marginHorizontal: 2,
  },
  submitButtonPressed: {
    backgroundColor: colors.brandPressed,
  },
  submitButtonDisabled: {
    opacity: 0.7,
  },
  submitText: {
    color: colors.surface,
    fontSize: 25,
    fontWeight: '800',
  },
  recoveryLinkButton: {
    alignSelf: 'center',
    marginTop: 18,
    paddingVertical: 3,
  },
  recoveryLink: {
    color: '#EF4444',
    fontSize: 16,
    lineHeight: 23,
    textDecorationLine: 'underline',
  },
  registerRow: {
    alignItems: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginTop: 23,
  },
  registerQuestion: {
    color: colors.text,
    fontSize: 16,
    lineHeight: 23,
  },
  registerLink: {
    color: colors.link,
    fontSize: 16,
    lineHeight: 23,
    textDecorationLine: 'underline',
  },
});

