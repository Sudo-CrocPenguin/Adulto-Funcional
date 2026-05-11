import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, KeyboardAvoidingView, Platform, Alert, Image } from 'react-native';
import { useAuth } from '../../src/contexts/AuthContext';
import { router } from 'expo-router';
import { Colors } from '../../src/constants/Colors';

export default function RegisterScreen() {
  const { register, isLoading } = useAuth();
  const [form, setForm] = useState({
    names: '', lastnames: '', phone: '', email: '', password: '', confirmPassword: '', masterKey: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [showMasterKey, setShowMasterKey] = useState(false);
  const [error, setError] = useState('');

  const validateEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const validateColombianPhone = (phone: string) => {
    // Acepta formatos: 3001234567, +573001234567
    const regex = /^(\+57)?[1-9]\d{9}$/;
    return regex.test(phone.replace(/\s/g, ''));
  };

  const handleRegister = async () => {
    if (!form.names.trim()) { setError('El nombre es obligatorio'); return; }
    if (!form.lastnames.trim()) { setError('Los apellidos son obligatorios'); return; }
    if (!form.phone.trim()) { setError('El teléfono es obligatorio'); return; }
    if (!validateColombianPhone(form.phone)) { setError('Teléfono colombiano inválido (ej: 3001234567 o +573001234567)'); return; }
    if (!form.email.trim()) { setError('El correo es obligatorio'); return; }
    if (!validateEmail(form.email)) { setError('Correo electrónico inválido'); return; }
    if (!form.password) { setError('La contraseña es obligatoria'); return; }
    if (form.password.length < 8) { setError('La contraseña debe tener al menos 8 caracteres'); return; }
    if (form.password !== form.confirmPassword) { setError('Las contraseñas no coinciden'); return; }
    if (form.masterKey && form.masterKey.length < 8) { setError('La clave maestra debe tener al menos 8 caracteres'); return; }
    setError('');
    try {
      await register({
        names: form.names.trim(),
        lastnames: form.lastnames.trim(),
        phone: form.phone.trim(),
        email: form.email.trim(),
        password: form.password,
        masterKey: form.masterKey || undefined,
      });
      router.replace('/(app)');
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: Colors.background }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={{ paddingVertical: 40 }}>
        <View style={{ alignItems: 'center' }}>
          <Image 
            source={require('../../assets/images/icon.png')} 
            style={{ width: 60, height: 60, borderRadius: 12, marginBottom: 10 }}
            resizeMode="contain"
          />
          <Text style={{ fontSize: 26, fontWeight: 'bold', color: Colors.text }}>Adulto Funcional</Text>
          <Text style={{ fontSize: 14, color: Colors.textSecondary, textAlign: 'center' }}>Crea tu cuenta para empezar</Text>
        </View>

        <View style={{ backgroundColor: '#fff', marginHorizontal: 24, borderRadius: 32, padding: 24, marginTop: 24, shadowOpacity: 0.1, shadowRadius: 8, elevation: 5 }}>
          <Text style={{ fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 20 }}>Registrarse</Text>

          {error ? <Text style={{ color: Colors.error, textAlign: 'center', marginBottom: 16 }}>{error}</Text> : null}

          <Text style={styles.label}>Nombres</Text>
          <TextInput style={styles.input} value={form.names} onChangeText={(t) => setForm({ ...form, names: t })} />

          <Text style={styles.label}>Apellidos</Text>
          <TextInput style={styles.input} value={form.lastnames} onChangeText={(t) => setForm({ ...form, lastnames: t })} />

          <Text style={styles.label}>Teléfono</Text>
          <TextInput style={styles.input} keyboardType="phone-pad" value={form.phone} onChangeText={(t) => setForm({ ...form, phone: t })} placeholder="3001234567" />

          <Text style={styles.label}>Correo electrónico</Text>
          <TextInput style={styles.input} autoCapitalize="none" keyboardType="email-address" value={form.email} onChangeText={(t) => setForm({ ...form, email: t })} />

          <Text style={styles.label}>Contraseña</Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12 }}>
            <TextInput style={{ flex: 1, paddingVertical: 12, paddingHorizontal: 12 }} secureTextEntry={!showPassword} value={form.password} onChangeText={(t) => setForm({ ...form, password: t })} />
            <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={{ paddingHorizontal: 12 }}><Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text></TouchableOpacity>
          </View>

          <Text style={styles.label}>Confirmar contraseña</Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12 }}>
            <TextInput style={{ flex: 1, paddingVertical: 12, paddingHorizontal: 12 }} secureTextEntry={!showConfirm} value={form.confirmPassword} onChangeText={(t) => setForm({ ...form, confirmPassword: t })} />
            <TouchableOpacity onPress={() => setShowConfirm(!showConfirm)} style={{ paddingHorizontal: 12 }}><Text>{showConfirm ? '👁️' : '👁️‍🗨️'}</Text></TouchableOpacity>
          </View>

          <TouchableOpacity onPress={() => setShowMasterKey(!showMasterKey)} style={{ marginVertical: 12 }}>
            <Text style={{ color: Colors.link, textAlign: 'center' }}>{showMasterKey ? 'Ocultar' : 'Configurar'} clave maestra (opcional)</Text>
          </TouchableOpacity>

          {showMasterKey && (
            <>
              <Text style={styles.label}>Clave Maestra</Text>
              <TextInput style={styles.input} secureTextEntry value={form.masterKey} onChangeText={(t) => setForm({ ...form, masterKey: t })} placeholder="Mínimo 8 caracteres" />
            </>
          )}

          <TouchableOpacity style={{ backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 16 }} onPress={handleRegister} disabled={isLoading}>
            {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={{ color: '#fff', fontWeight: 'bold', fontSize: 16 }}>Crear Cuenta</Text>}
          </TouchableOpacity>

          <View style={{ flexDirection: 'row', justifyContent: 'center', marginTop: 20 }}>
            <Text style={{ color: Colors.textSecondary }}>¿Ya tienes cuenta? </Text>
            <TouchableOpacity onPress={() => router.push('/(auth)/login')}><Text style={{ color: Colors.link, fontWeight: 'bold' }}>Iniciar Sesión</Text></TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = {
  label: { fontSize: 14, fontWeight: '500', marginTop: 12, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, marginBottom: 8, backgroundColor: '#fff' },
};
