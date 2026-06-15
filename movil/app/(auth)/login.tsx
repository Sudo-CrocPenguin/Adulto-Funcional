import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Image, StyleSheet } from 'react-native';
import { useAuth } from '../../src/contexts/AuthContext';
import { router } from 'expo-router';
import { Colors } from '../../src/constants/Colors';

export default function LoginScreen() {
  const { login, isLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');

  const validateEmail = (email: string) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  };

  const handleLogin = async () => {
    if (!email.trim()) {
      setError('El correo electrónico es obligatorio');
      return;
    }
    if (!validateEmail(email)) {
      setError('Ingrese un correo electrónico válido');
      return;
    }
    if (!password) {
      setError('La contraseña es obligatoria');
      return;
    }
    if (password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres');
      return;
    }
    setError('');
    try {
      await login({ email: email.trim(), password });
      router.replace('/(app)');
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: Colors.background }} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={{ flexGrow: 1, justifyContent: 'center' }}>
        <View style={{ alignItems: 'center', marginTop: 60, marginBottom: 40 }}>
          <Image 
            source={require('../../assets/images/icon.png')} 
            style={{ width: 80, height: 80, borderRadius: 40, marginBottom: 16 }}
            resizeMode="contain"
          />
          <Text style={{ fontSize: 28, fontWeight: 'bold', color: Colors.text }}>Adulto Funcional</Text>
          <Text style={{ fontSize: 14, color: Colors.textSecondary, textAlign: 'center', marginHorizontal: 40 }}>Organiza tu vida con control y seguridad</Text>
        </View>

        <View style={{ backgroundColor: '#fff', marginHorizontal: 24, borderRadius: 32, padding: 24, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 8, elevation: 5 }}>
          <Text style={{ fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 24 }}>Iniciar Sesión</Text>

          {error ? <Text style={{ color: Colors.error, textAlign: 'center', marginBottom: 16 }}>{error}</Text> : null}

          <Text style={styles.label}>Correo electrónico</Text>
          <TextInput
            style={styles.input}
            placeholder="correo@ejemplo.com"
            value={email}
            onChangeText={setEmail}
            autoCapitalize="none"
            keyboardType="email-address"
          />

          <Text style={styles.label}>Contraseña</Text>
          <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12, backgroundColor: '#fff' }}>
            <TextInput
              style={{ flex: 1, paddingVertical: 12, paddingHorizontal: 12 }}
              placeholder="********"
              secureTextEntry={!showPassword}
              value={password}
              onChangeText={setPassword}
            />
            <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={{ paddingHorizontal: 12 }}>
              <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
            </TouchableOpacity>
          </View>

          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginVertical: 16 }}>
            <TouchableOpacity onPress={() => setRememberMe(!rememberMe)} style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={{ width: 20, height: 20, borderWidth: 1, borderColor: Colors.primary, borderRadius: 4, marginRight: 8, backgroundColor: rememberMe ? Colors.primary : '#fff' }} />
              <Text>Recuérdame</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity style={{ backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' }} onPress={handleLogin} disabled={isLoading}>
            {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={{ color: '#fff', fontWeight: 'bold', fontSize: 16 }}>Iniciar Sesión</Text>}
          </TouchableOpacity>

          <TouchableOpacity onPress={() => router.push('/(auth)/forgot-password')} style={{ marginTop: 15 }}>
            <Text style={{ color: Colors.error, textAlign: 'center' }}>¿Olvidaste tu contraseña?</Text>
          </TouchableOpacity>

          <View style={{ flexDirection: 'row', justifyContent: 'center', marginTop: 24 }}>
            <Text style={{ color: Colors.textSecondary }}>¿No tienes cuenta? </Text>
            <TouchableOpacity onPress={() => router.push('/(auth)/register')}>
              <Text style={{ color: Colors.link, fontWeight: 'bold' }}>Registrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  label: { fontSize: 14, fontWeight: '500', marginBottom: 8, color: Colors.text },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 12, marginBottom: 16, backgroundColor: '#fff' },
});
