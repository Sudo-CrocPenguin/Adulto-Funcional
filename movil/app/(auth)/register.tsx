/**
 * @file register.tsx
 * @description Pantalla de registro de nuevos usuarios para la aplicación Adulto Funcional.
 *              Permite crear una cuenta proporcionando datos personales, credenciales
 *              y opcionalmente una clave maestra de seguridad.
 *
 * @module app/(auth)/register
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Este componente maneja:
 * - Validación local del formulario antes de enviar al servidor.
 * - Integración con el contexto de autenticación {@link useAuth} para el registro.
 * - Visibilidad dinámica de los campos de contraseña.
 * - Configuración opcional de una clave maestra de al menos 8 caracteres.
 * - Redirección automática a la pantalla principal tras un registro exitoso.
 *
 * @example
 * // Esta pantalla es montada automáticamente por expo-router en la ruta:
 * // /(auth)/register
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
 * @interface RegisterForm
 * @description Estructura del formulario de registro de usuario.
 */
interface RegisterForm {
  /** Nombres del usuario */
  names: string;
  /** Apellidos del usuario */
  lastnames: string;
  /** Número de teléfono del usuario */
  phone: string;
  /** Correo electrónico del usuario */
  email: string;
  /** Contraseña elegida por el usuario */
  password: string;
  /** Confirmación de la contraseña para validación local */
  confirmPassword: string;
  /** Clave maestra opcional para funciones de seguridad adicionales */
  masterKey: string;
}

/**
 * @function RegisterScreen
 * @description Componente principal de la pantalla de registro.
 *              Renderiza un formulario completo de creación de cuenta con validaciones
 *              en tiempo real y retroalimentación visual de errores.
 *
 * @returns {JSX.Element} Pantalla de registro con formulario scrolleable y evitador de teclado.
 *
 * @example
 * // Expo Router lo monta automáticamente. No se usa directamente.
 * <RegisterScreen />
 */
export default function RegisterScreen() {
  const { register, isLoading, error: authError } = useAuth();

  /**
   * @type {RegisterForm}
   * @description Estado del formulario con todos los campos inicializados vacíos.
   */
  const [form, setForm] = useState<RegisterForm>({
    names: '',
    lastnames: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    masterKey: '',
  });

  /** @description Controla la visibilidad del campo de contraseña */
  const [showPassword, setShowPassword] = useState(false);

  /** @description Controla la visibilidad del campo de confirmación de contraseña */
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  /** @description Controla si el campo de clave maestra es visible en el formulario */
  const [showMasterKey, setShowMasterKey] = useState(false);

  /** @description Error local de validación del formulario, independiente del error del servidor */
  const [localError, setLocalError] = useState<string | null>(null);

  /**
   * @function validateForm
   * @description Valida todos los campos del formulario antes de intentar el registro.
   *              Actualiza `localError` con el primer error encontrado.
   *
   * @returns {boolean} `true` si el formulario es válido, `false` en caso contrario.
   *
   * @remarks
   * Validaciones aplicadas en orden:
   * 1. `names` no debe estar vacío.
   * 2. `lastnames` no debe estar vacío.
   * 3. `phone` no debe estar vacío.
   * 4. `email` debe tener formato válido (regex).
   * 5. `password` debe tener al menos 8 caracteres.
   * 6. `password` y `confirmPassword` deben coincidir.
   * 7. Si `masterKey` se proporciona, debe tener al menos 8 caracteres.
   */
  const validateForm = (): boolean => {
    if (!form.names.trim()) {
      setLocalError('El nombre es obligatorio');
      return false;
    }
    if (!form.lastnames.trim()) {
      setLocalError('Los apellidos son obligatorios');
      return false;
    }
    if (!form.phone.trim()) {
      setLocalError('El teléfono es obligatorio');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
      setLocalError('Email inválido');
      return false;
    }
    if (form.password.length < 8) {
      setLocalError('La contraseña debe tener al menos 8 caracteres');
      return false;
    }
    if (form.password !== form.confirmPassword) {
      setLocalError('Las contraseñas no coinciden');
      return false;
    }
    if (form.masterKey && form.masterKey.length < 8) {
      setLocalError('La clave maestra debe tener al menos 8 caracteres');
      return false;
    }

    setLocalError(null);
    return true;
  };

  /**
   * @function handleRegister
   * @description Manejador del botón "Crear Cuenta". Valida el formulario y,
   *              si es válido, llama a `register` del contexto de autenticación.
   *              En caso de éxito redirige a `/(app)`. En caso de error muestra
   *              una alerta nativa con el mensaje recibido.
   *
   * @returns {Promise<void>}
   *
   * @throws Captura y muestra cualquier error lanzado por `register`.
   */
  const handleRegister = async (): Promise<void> => {
    if (!validateForm()) return;

    try {
      await register({
        names: form.names.trim(),
        lastnames: form.lastnames.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        password: form.password,
        masterKey: form.masterKey.trim() || undefined,
      });
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
              width: 50,
              height: 50,
              backgroundColor: '#fff',
              borderRadius: 12,
              marginBottom: 10,
            }}
          />
          <Text style={GlobalStyles.headerTitle}>Adulto Funcional</Text>
          <Text style={GlobalStyles.headerSubtitle}>Crea tu cuenta para empezar</Text>
        </View>

        {/* ── Tarjeta principal del formulario ── */}
        <View style={[GlobalStyles.card, { marginBottom: 30 }]}>
          <Text style={GlobalStyles.title}>Registrarse</Text>

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

          {/* ── Campo: Nombres ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Nombres</Text>
            <TextInput
              style={GlobalStyles.input}
              value={form.names}
              onChangeText={(t) => setForm({ ...form, names: t })}
            />
          </View>

          {/* ── Campo: Apellidos ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Apellidos</Text>
            <TextInput
              style={GlobalStyles.input}
              value={form.lastnames}
              onChangeText={(t) => setForm({ ...form, lastnames: t })}
            />
          </View>

          {/* ── Campo: Teléfono ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Teléfono</Text>
            <TextInput
              style={GlobalStyles.input}
              keyboardType="phone-pad"
              value={form.phone}
              onChangeText={(t) => setForm({ ...form, phone: t })}
            />
          </View>

          {/* ── Campo: Correo electrónico ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Correo electrónico</Text>
            <TextInput
              style={GlobalStyles.input}
              autoCapitalize="none"
              keyboardType="email-address"
              value={form.email}
              onChangeText={(t) => setForm({ ...form, email: t })}
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
                secureTextEntry={!showPassword}
                value={form.password}
                onChangeText={(t) => setForm({ ...form, password: t })}
              />
              <TouchableOpacity
                onPress={() => setShowPassword(!showPassword)}
                style={{ paddingHorizontal: 12 }}
              >
                <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* ── Campo: Confirmar contraseña con toggle de visibilidad ── */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Confirmar contraseña</Text>
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
                secureTextEntry={!showConfirmPassword}
                value={form.confirmPassword}
                onChangeText={(t) => setForm({ ...form, confirmPassword: t })}
              />
              <TouchableOpacity
                onPress={() => setShowConfirmPassword(!showConfirmPassword)}
                style={{ paddingHorizontal: 12 }}
              >
                <Text>{showConfirmPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* ── Toggle para mostrar/ocultar el campo de clave maestra ── */}
          <TouchableOpacity
            onPress={() => setShowMasterKey(!showMasterKey)}
            style={{ marginVertical: 10 }}
          >
            <Text style={{ color: Colors.link, textAlign: 'center' }}>
              {showMasterKey ? 'Ocultar' : 'Configurar'} clave maestra (opcional)
            </Text>
          </TouchableOpacity>

          {/* ── Campo: Clave maestra (condicional) ── */}
          {showMasterKey && (
            <View style={GlobalStyles.inputContainer}>
              <Text style={GlobalStyles.label}>Clave Maestra</Text>
              <TextInput
                style={GlobalStyles.input}
                secureTextEntry
                value={form.masterKey}
                onChangeText={(t) => setForm({ ...form, masterKey: t })}
                placeholder="Mínimo 8 caracteres"
              />
            </View>
          )}

          {/* ── Botón principal de registro ── */}
          <TouchableOpacity
            style={[GlobalStyles.button, isLoading && GlobalStyles.buttonDisabled]}
            onPress={handleRegister}
            disabled={isLoading}
          >
            {isLoading
              ? <ActivityIndicator color="#fff" />
              : <Text style={GlobalStyles.buttonText}>Crear Cuenta</Text>
            }
          </TouchableOpacity>

          {/* ── Enlace a inicio de sesión ── */}
          <TouchableOpacity
            onPress={() => router.push('/(auth)/login')}
            style={{ marginTop: 20 }}
          >
            <Text style={{ textAlign: 'center', color: Colors.textSecondary }}>
              ¿Ya tienes cuenta?{' '}
              <Text style={GlobalStyles.link}>Iniciar Sesión</Text>
            </Text>
          </TouchableOpacity>

        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}