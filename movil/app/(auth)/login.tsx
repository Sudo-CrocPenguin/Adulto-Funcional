/**
 * @file login.tsx
 * @description Pantalla de inicio de sesión para la aplicación Adulto Funcional.
 *              Permite a usuarios existentes autenticarse mediante correo electrónico
 *              y contraseña, con validación local antes de consultar el servidor.
 *
 * @module app/(auth)/login
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Este componente maneja:
 * - Validación local del formulario (correo y contraseña) antes de llamar al servidor.
 * - Integración con el contexto de autenticación {@link useAuth} para el inicio de sesión.
 * - Visibilidad dinámica del campo de contraseña.
 * - Redirección automática a la pantalla principal tras una autenticación exitosa.
 * - Navegación hacia las pantallas de registro y recuperación de contraseña.
 *
 * @example
 * // Esta pantalla es montada automáticamente por expo-router en la ruta:
 * // /(auth)/login
 */

import { router } from 'expo-router';
import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { Colors } from '../../src/constants/Colors';
import { GlobalStyles } from '../../src/constants/Styles';
import { useAuth } from '../../src/contexts/AuthContext';

/**
 * @function LoginScreen
 * @description Componente principal de la pantalla de inicio de sesión.
 *              Renderiza un formulario con campos de correo y contraseña,
 *              manejo de errores y enlaces a registro y recuperación de contraseña.
 *
 * @returns {JSX.Element} Pantalla de login con formulario scrolleable y evitador de teclado.
 */
export default function LoginScreen() {
  const { login, isLoading, error: authError } = useAuth();

  /** @description Valor actual del campo de correo electrónico */
  const [email, setEmail] = useState('');

  /** @description Valor actual del campo de contraseña */
  const [password, setPassword] = useState('');

  /** @description Controla la visibilidad del texto en el campo de contraseña */
  const [showPassword, setShowPassword] = useState(false);

  /** @description Error de validación local del formulario, independiente del error del servidor */
  const [localError, setLocalError] = useState<string | null>(null);

  /**
   * @function validateForm
   * @description Valida los campos del formulario antes de intentar el inicio de sesión.
   *              Actualiza `localError` con el primer error encontrado.
   *
   * @returns {boolean} `true` si el formulario es válido, `false` en caso contrario.
   *
   * @remarks
   * Validaciones aplicadas en orden:
   * 1. `email` no debe estar vacío.
   * 2. `email` debe tener formato válido (regex).
   * 3. `password` no debe estar vacío.
   */
  const validateForm = (): boolean => {
    if (!email.trim()) {
      setLocalError('Correo obligatorio');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setLocalError('Email inválido');
      return false;
    }

    if (!password.trim()) {
      setLocalError('Contraseña obligatoria');
      return false;
    }

    setLocalError(null);
    return true;
  };

  /**
   * @function handleLogin
   * @description Manejador del botón "Iniciar Sesión". Valida el formulario y,
   *              si es válido, llama a `login` del contexto de autenticación.
   *              En caso de éxito redirige a `/(app)`. En caso de error muestra
   *              una alerta nativa con el mensaje recibido.
   *
   * @returns {Promise<void>}
   *
   * @throws Captura y muestra cualquier error lanzado por `login`.
   */
  const handleLogin = async (): Promise<void> => {
    if (!validateForm()) return;

    try {
      await login({ email: email.trim(), password });
      router.replace('/(app)');
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  return (
    <KeyboardAvoidingView
      style={GlobalStyles.mainContainer}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={{ flexGrow: 1 }}>

        {/* ── Encabezado con logo y título ── */}
        <View style={GlobalStyles.header}>
          <View
            style={{
              width: 60,
              height: 60,
              backgroundColor: '#fff',
              borderRadius: 30,
              marginBottom: 10,
            }}
          />
          <Text style={GlobalStyles.headerTitle}>Adulto Funcional</Text>
          <Text style={GlobalStyles.headerSubtitle}>
            Organiza tu vida con control y seguridad
          </Text>
        </View>

        {/* ── Tarjeta principal del formulario ── */}
        <View style={GlobalStyles.card}>
          <Text style={GlobalStyles.title}>Iniciar Sesión</Text>

          {/* ── Mensaje de error (local o del servidor) ── */}
          {(localError || authError) && (
            <View
              style={{
                backgroundColor: '#fee',
                padding: 12,
                borderRadius: 8,
                marginBottom: 16,
              }}
            >
              <Text style={{ color: Colors.error, textAlign: 'center' }}>
                {localError || authError}
              </Text>
            </View>
          )}

          {/* ── Campo: Correo electrónico ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Correo electrónico</Text>
            <TextInput
              style={GlobalStyles.input}
              placeholder="correo@ejemplo.com"
              placeholderTextColor={Colors.textSecondary}
              value={email}
              onChangeText={setEmail}
              autoCapitalize="none"
              keyboardType="email-address"
              editable={!isLoading}
            />
          </View>

          {/* ── Campo: Contraseña con toggle de visibilidad ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Contraseña</Text>
            <View
              style={{
                flexDirection: 'row',
                alignItems: 'center',
                borderWidth: 1,
                borderColor: Colors.border,
                borderRadius: 12,
              }}
            >
              <TextInput
                style={{ flex: 1, padding: 12 }}
                placeholder="********"
                placeholderTextColor={Colors.textSecondary}
                secureTextEntry={!showPassword}
                value={password}
                onChangeText={setPassword}
                editable={!isLoading}
              />
              <TouchableOpacity
                onPress={() => setShowPassword(!showPassword)}
                style={{ paddingHorizontal: 12 }}
              >
                <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* ── Botón principal de inicio de sesión ── */}
          <TouchableOpacity
            style={[GlobalStyles.button, isLoading && GlobalStyles.buttonDisabled]}
            onPress={handleLogin}
            disabled={isLoading}
          >
            {isLoading
              ? <ActivityIndicator color="#fff" />
              : <Text style={GlobalStyles.buttonText}>Iniciar Sesión</Text>
            }
          </TouchableOpacity>

          {/* ── Enlace a recuperación de contraseña ── */}
          <TouchableOpacity onPress={() => router.push('/(auth)/forgot-password')}>
            <Text style={{ color: Colors.error, textAlign: 'center', marginTop: 15 }}>
              ¿Olvidaste tu contraseña?
            </Text>
          </TouchableOpacity>

          {/* ── Enlace a registro de nueva cuenta ── */}
          <TouchableOpacity
            onPress={() => router.push('/(auth)/register')}
            style={{ marginTop: 15 }}
          >
            <Text style={{ textAlign: 'center', color: Colors.textSecondary }}>
              ¿No tienes cuenta?{' '}
              <Text style={GlobalStyles.link}>Registrar</Text>
            </Text>
          </TouchableOpacity>

        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}