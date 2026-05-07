// app/(auth)/register.tsx

/**
 * Pantalla de registro de nuevo usuario.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Pantalla que permite crear una nueva cuenta en la aplicación.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Captura nombres, apellidos, teléfono, email, contraseña y clave maestra opcional.
 * Valida cada campo (formato, longitud, coincidencia de contraseñas) y llama al
 * contexto de autenticación para registrar al usuario.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Usa estados locales para los campos y errores. Al enviar, valida y luego llama
 * a la función `register` del contexto. Tras éxito, redirige al home.
 *
 * @author Miguel Angel Blandon Montes
 */

import { router } from 'expo-router';
import React, { useState } from 'react';
import { ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';
import { GlobalStyles } from '../../src/constants/Styles';
import { useAuth } from '../../src/contexts/AuthContext';

// TODO: Agregar validación de fortaleza de contraseña (mayúscula, número, símbolo)
// TODO: Agregar campo de "Clave Maestra" opcional con validación de mínimo 8 caracteres

export default function RegisterScreen() {
  const { register, isLoading, error: authError } = useAuth();
  const [form, setForm] = useState({
    names: '',
    lastnames: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    masterKey: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [showMasterKey, setShowMasterKey] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);

  const validateForm = (): boolean => {
    // Nombres
    if (!form.names.trim()) {
      setLocalError('El nombre es obligatorio');
      return false;
    }
    if (form.names.length > 50) {
      setLocalError('El nombre no puede exceder 50 caracteres');
      return false;
    }
    // Apellidos
    if (!form.lastnames.trim()) {
      setLocalError('Los apellidos son obligatorios');
      return false;
    }
    if (form.lastnames.length > 50) {
      setLocalError('Los apellidos no pueden exceder 50 caracteres');
      return false;
    }
    // Teléfono
    if (!form.phone.trim()) {
      setLocalError('El teléfono es obligatorio');
      return false;
    }
    // Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
      setLocalError('Ingrese un correo electrónico válido');
      return false;
    }
    // Contraseña
    if (form.password.length < 8) {
      setLocalError('La contraseña debe tener al menos 8 caracteres');
      return false;
    }
    if (form.password !== form.confirmPassword) {
      setLocalError('Las contraseñas no coinciden');
      return false;
    }
    // Clave maestra (si se ingresó)
    if (form.masterKey && form.masterKey.length > 0 && form.masterKey.length < 8) {
      setLocalError('La clave maestra debe tener al menos 8 caracteres');
      return false;
    }
    setLocalError(null);
    return true;
  };

  const handleRegister = async () => {
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
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <KeyboardAvoidingView style={GlobalStyles.mainContainer} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={{ flexGrow: 1 }}>
        <View style={GlobalStyles.header}>
          <View style={{ width: 50, height: 50, backgroundColor: '#fff', borderRadius: 12, marginBottom: 10 }} />
          <Text style={GlobalStyles.headerTitle}>Adulto Funcional</Text>
          <Text style={GlobalStyles.headerSubtitle}>Crea tu cuenta para empezar</Text>
        </View>

        <View style={[GlobalStyles.card, { marginBottom: 30 }]}>
          <Text style={GlobalStyles.title}>Registrarse</Text>

          {(localError || authError) && (
            <View style={{ backgroundColor: '#fee', padding: 12, borderRadius: 8, marginBottom: 16 }}>
              <Text style={{ color: Colors.error, textAlign: 'center' }}>{localError || authError}</Text>
            </View>
          )}

          {/* Campo Nombres */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Nombres</Text>
            <TextInput style={GlobalStyles.input} value={form.names} onChangeText={(t) => setForm({ ...form, names: t })} />
          </View>

          {/* Campo Apellidos */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Apellidos</Text>
            <TextInput style={GlobalStyles.input} value={form.lastnames} onChangeText={(t) => setForm({ ...form, lastnames: t })} />
          </View>

          {/* Campo Teléfono */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Teléfono</Text>
            <TextInput style={GlobalStyles.input} keyboardType="phone-pad" value={form.phone} onChangeText={(t) => setForm({ ...form, phone: t })} />
          </View>

          {/* Campo Email */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Correo electrónico</Text>
            <TextInput style={GlobalStyles.input} autoCapitalize="none" keyboardType="email-address" value={form.email} onChangeText={(t) => setForm({ ...form, email: t })} />
          </View>

          {/* Campo Contraseña */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Contraseña</Text>
            <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12 }}>
              <TextInput style={{ flex: 1, padding: 12 }} secureTextEntry={!showPassword} value={form.password} onChangeText={(t) => setForm({ ...form, password: t })} />
              <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={{ paddingHorizontal: 12 }}>
                <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Campo Confirmar Contraseña */}
          <View style={GlobalStyles.inputContainer}>
            <Text style={GlobalStyles.label}>Confirmar contraseña</Text>
            <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12 }}>
              <TextInput style={{ flex: 1, padding: 12 }} secureTextEntry={!showConfirmPassword} value={form.confirmPassword} onChangeText={(t) => setForm({ ...form, confirmPassword: t })} />
              <TouchableOpacity onPress={() => setShowConfirmPassword(!showConfirmPassword)} style={{ paddingHorizontal: 12 }}>
                <Text>{showConfirmPassword ? '👁️' : '👁️‍🗨️'}</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Sección opcional: Clave Maestra */}
          <TouchableOpacity onPress={() => setShowMasterKey(!showMasterKey)} style={{ marginVertical: 10 }}>
            <Text style={{ color: Colors.link, textAlign: 'center' }}>{showMasterKey ? 'Ocultar' : 'Configurar'} clave maestra (opcional)</Text>
          </TouchableOpacity>

          {showMasterKey && (
            <View style={GlobalStyles.inputContainer}>
              <Text style={GlobalStyles.label}>Clave Maestra</Text>
              <TextInput style={GlobalStyles.input} secureTextEntry value={form.masterKey} onChangeText={(t) => setForm({ ...form, masterKey: t })} placeholder="Mínimo 8 caracteres" />
            </View>
          )}

          <TouchableOpacity style={[GlobalStyles.button, isLoading && GlobalStyles.buttonDisabled]} onPress={handleRegister} disabled={isLoading}>
            {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={GlobalStyles.buttonText}>Crear Cuenta</Text>}
          </TouchableOpacity>

          <TouchableOpacity onPress={() => router.push('/(auth)/login')} style={{ marginTop: 20 }}>
            <Text style={{ textAlign: 'center', color: Colors.textSecondary }}>
              ¿Ya tienes cuenta? <Text style={GlobalStyles.link}>Iniciar Sesión</Text>
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}