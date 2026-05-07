// app/(auth)/login.tsx

/**
 * Pantalla de inicio de sesión.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Pantalla que permite al usuario autenticarse en la aplicación
 * proporcionando su email y contraseña.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Captura las credenciales, las valida localmente y llama al contexto
 * de autenticación para iniciar sesión. En caso de éxito, redirige a la
 * pantalla principal (Home). En caso de error, muestra el mensaje del backend.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Utiliza el hook {@link useAuth} del contexto global. Al enviar el formulario,
 * muestra un indicador de carga y deshabilita el botón. Los errores se muestran
 * dentro de la tarjeta.
 *
 * @author Miguel Angel Blandon Montes
 */

import { router } from 'expo-router';
import React, { useState } from 'react';
import { ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';
import { GlobalStyles } from '../../src/constants/Styles';
import { useAuth } from '../../src/contexts/AuthContext';

// TODO: Agregar "Olvidé mi contraseña" (enlace a pantalla de recuperación)
// TODO: Implementar persistencia de "Recuérdame" usando SecureStore

export default function LoginScreen() {
  const { login, isLoading, error: authError } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);

  // Validación local antes de llamar al contexto
  const validateForm = (): boolean => {
    if (!email.trim()) {
      setLocalError('El correo electrónico es obligatorio');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setLocalError('Ingrese un correo electrónico válido');
      return false;
    }
    if (!password.trim()) {
      setLocalError('La contraseña es obligatoria');
      return false;
    }
    setLocalError(null);
    return true;
  };

  const handleLogin = async () => {
    if (!validateForm()) return;

    try {
      await login({ email: email.trim(), password });
      // Redirigir al home después de login exitoso
      router.replace('/(app)');
    } catch (err) {
      // El error ya está en authError, pero también lo manejamos localmente
      console.error(err);
    }
  };

  return (
    <KeyboardAvoidingView style={GlobalStyles.mainContainer} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={{ flexGrow: 1 }}>
        {/* Header azul */}
        <View style={GlobalStyles.header}>
          <View style={{ width: 60, height: 60, backgroundColor: '#fff', borderRadius: 30, marginBottom: 10 }} />
          <Text style={GlobalStyles.headerTitle}>Adulto Funcional</Text>
          <Text style={GlobalStyles.headerSubtitle}>Organiza tu vida con control y seguridad</Text>
        </View>

        {/* Tarjeta blanca */}
        <View style={GlobalStyles.card}>
          <Text style={GlobalStyles.title}>Iniciar Sesión</Text>

          {/* Mostrar errores */}
          {(localError || authError) && (
            <View style={{ backgroundColor: '#fee', padding: 12, borderRadius: 8, marginBottom: 16 }}>
              <Text style={{ color: Colors.error, textAlign: 'center' }}>{localError || authError}</Text>
            </View>
          )}

          {/* Campo email */}
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

          {/* Campo contraseña con mostrar/ocultar */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Contraseña</Text>
            <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12, backgroundColor: Colors.cardBackground }}>
              <TextInput
                style={{ flex: 1, paddingHorizontal: 12, paddingVertical: 12, fontSize: 16 }}
                placeholder="********"
                placeholderTextColor={Colors.textSecondary}
                secureTextEntry={!showPassword}
                value={password}
                onChangeText={setPassword}
                editable={!isLoading}
              />
              <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={{ paddingHorizontal: 12 }}>
                <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Checkbox "Recuérdame" */}
          <TouchableOpacity style={GlobalStyles.checkboxContainer} onPress={() => setRememberMe(!rememberMe)}>
            <View style={[GlobalStyles.checkbox, rememberMe && GlobalStyles.checkboxChecked]} />
            <Text style={GlobalStyles.checkboxText}>Recuérdame</Text>
          </TouchableOpacity>

          {/* Botón de inicio de sesión */}
          <TouchableOpacity
            style={[GlobalStyles.button, isLoading && GlobalStyles.buttonDisabled]}
            onPress={handleLogin}
            disabled={isLoading}
          >
            {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={GlobalStyles.buttonText}>Iniciar Sesión</Text>}
          </TouchableOpacity>

          {/* Enlace a recuperación de contraseña */}
          <TouchableOpacity onPress={() => router.push('/(auth)/forgot-password')}>
            <Text style={{ color: Colors.error, textAlign: 'center', marginTop: 15, fontWeight: '500' }}>
              ¿Olvidaste tu contraseña?
            </Text>
          </TouchableOpacity>

          {/* Enlace a registro */}
          <TouchableOpacity onPress={() => router.push('/(auth)/register')} style={{ marginTop: 15 }}>
            <Text style={{ textAlign: 'center', color: Colors.textSecondary }}>
              ¿No tienes cuenta? <Text style={GlobalStyles.link}>Registrar</Text>
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}