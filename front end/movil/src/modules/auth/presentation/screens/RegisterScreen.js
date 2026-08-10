import { useRef, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Keyboard,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';

import { ApiError } from '../../../../core/http/ApiError';
import {
  SessionPersistenceError,
} from '../../application/RegisterAccountUseCase';
import {
  RegistrationValidationError,
} from '../../domain/RegistrationCommand';
import { colors } from '../../../../shared/theme/tokens';
import { BrandHero } from '../components/BrandHero';
import { FormField } from '../components/FormField';

const INITIAL_FORM = Object.freeze({
  names: '',
  lastnames: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
});

function backendFieldErrors(error) {
  return error.fieldErrors.reduce((result, fieldError) => {
    if (fieldError?.field && fieldError?.message) {
      result[fieldError.field] = fieldError.message;
    }
    return result;
  }, {});
}

export function RegisterScreen({ registerAccount }) {
  const { width } = useWindowDimensions();
  const [form, setForm] = useState(INITIAL_FORM);
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [isSubmitting, setSubmitting] = useState(false);
  const lastnamesRef = useRef(null);
  const phoneRef = useRef(null);
  const emailRef = useRef(null);
  const passwordRef = useRef(null);
  const confirmationRef = useRef(null);

  const cardWidth = Math.min(width - 36, 520);

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
      const session = await registerAccount.execute(form);
      setForm(INITIAL_FORM);
      setFeedback({
        type: 'success',
        message: `Cuenta creada. ¡Bienvenido, ${session.names}!`,
      });
    } catch (error) {
      if (error instanceof RegistrationValidationError) {
        setErrors(error.fieldErrors);
        setFeedback({ type: 'error', message: error.message });
      } else if (error instanceof ApiError) {
        setErrors(backendFieldErrors(error));
        setFeedback({ type: 'error', message: error.message });
      } else if (error instanceof SessionPersistenceError) {
        setForm(INITIAL_FORM);
        setFeedback({
          type: 'error',
          message:
            'Tu cuenta fue creada, pero la sesión no pudo guardarse. Inicia sesión nuevamente.',
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

  function explainPendingLogin() {
    Alert.alert(
      'Inicio de sesión',
      'Esta será la siguiente pantalla del flujo de autenticación.',
    );
  }

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={styles.keyboardView}
    >
      <ScrollView
        bounces={false}
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <BrandHero />

        <View style={styles.formRegion}>
          <View style={[styles.card, { width: cardWidth }]}>
            <Text accessibilityRole="header" style={styles.formTitle}>
              Registrarse
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
              autoCapitalize="words"
              autoComplete="given-name"
              blurOnSubmit={false}
              error={errors.names}
              label="Nombres"
              onChangeText={(value) => updateField('names', value)}
              onSubmitEditing={() => lastnamesRef.current?.focus()}
              returnKeyType="next"
              textContentType="givenName"
              value={form.names}
            />
            <FormField
              ref={lastnamesRef}
              autoCapitalize="words"
              autoComplete="family-name"
              blurOnSubmit={false}
              error={errors.lastnames}
              label="Apellidos"
              onChangeText={(value) => updateField('lastnames', value)}
              onSubmitEditing={() => phoneRef.current?.focus()}
              returnKeyType="next"
              textContentType="familyName"
              value={form.lastnames}
            />
            <FormField
              ref={phoneRef}
              autoComplete="tel"
              blurOnSubmit={false}
              error={errors.phone}
              keyboardType="phone-pad"
              label="Teléfono"
              onChangeText={(value) => updateField('phone', value)}
              onSubmitEditing={() => emailRef.current?.focus()}
              returnKeyType="next"
              textContentType="telephoneNumber"
              value={form.phone}
            />
            <FormField
              ref={emailRef}
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
              autoComplete="new-password"
              blurOnSubmit={false}
              error={errors.password}
              label="Contraseña"
              onChangeText={(value) => updateField('password', value)}
              onSubmitEditing={() => confirmationRef.current?.focus()}
              returnKeyType="next"
              secure
              textContentType="newPassword"
              value={form.password}
            />
            <FormField
              ref={confirmationRef}
              autoComplete="new-password"
              error={errors.confirmPassword}
              label="Confirmar contraseña"
              onChangeText={(value) => updateField('confirmPassword', value)}
              onSubmitEditing={submit}
              returnKeyType="done"
              secure
              textContentType="newPassword"
              value={form.confirmPassword}
            />

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
                <Text style={styles.submitText}>Crear Cuenta</Text>
              )}
            </Pressable>

            <View style={styles.loginRow}>
              <Text style={styles.loginQuestion}>¿Ya tienes cuenta? </Text>
              <Pressable
                accessibilityRole="link"
                hitSlop={8}
                onPress={explainPendingLogin}
              >
                <Text style={styles.loginLink}>Iniciar Sesión</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  keyboardView: {
    backgroundColor: colors.background,
    flex: 1,
  },
  scrollContent: {
    backgroundColor: colors.background,
    flexGrow: 1,
  },
  formRegion: {
    alignItems: 'center',
    paddingBottom: 42,
    paddingTop: 36,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: 22,
    elevation: 8,
    paddingBottom: 24,
    paddingHorizontal: 24,
    paddingTop: 23,
    shadowColor: '#A9B3C1',
    shadowOffset: { width: -5, height: 2 },
    shadowOpacity: 0.35,
    shadowRadius: 7,
  },
  formTitle: {
    color: colors.text,
    fontSize: 29,
    fontWeight: '800',
    lineHeight: 36,
    marginBottom: 18,
    textAlign: 'center',
  },
  feedback: {
    borderRadius: 12,
    marginBottom: 15,
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
  submitButton: {
    alignItems: 'center',
    backgroundColor: colors.brand,
    borderRadius: 15,
    height: 57,
    justifyContent: 'center',
    marginHorizontal: 10,
    marginTop: 14,
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
  loginRow: {
    alignItems: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    marginTop: 18,
  },
  loginQuestion: {
    color: colors.text,
    fontSize: 16,
    lineHeight: 23,
  },
  loginLink: {
    color: colors.link,
    fontSize: 16,
    lineHeight: 23,
    textDecorationLine: 'underline',
  },
});
