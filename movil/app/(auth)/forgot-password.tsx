import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, KeyboardAvoidingView, Platform, ScrollView, Alert, Image } from 'react-native';
import { router } from 'expo-router';
import { Colors } from '../../src/constants/Colors';
import apiClient from '../../src/api/client';

export default function ForgotPasswordScreen() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const validateEmail = (email: string) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  };

  const handleSend = async () => {
    if (!email.trim()) {
      Alert.alert('Error', 'Ingrese su correo electrónico');
      return;
    }
    if (!validateEmail(email)) {
      Alert.alert('Error', 'Ingrese un correo válido');
      return;
    }
    setLoading(true);
    try {
      // Simulación de envío (endpoint real aún no existe en backend)
      // await apiClient.post('/api/auth/forgot-password', { email });
      await new Promise(resolve => setTimeout(resolve, 1500));
      setSent(true);
      Alert.alert('Éxito', 'Se han enviado instrucciones a su correo electrónico');
    } catch (err: any) {
      Alert.alert('Error', err.message || 'No se pudo enviar el correo');
    } finally {
      setLoading(false);
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
          <Text style={{ fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 12 }}>Recuperar contraseña</Text>
          <Text style={{ textAlign: 'center', color: Colors.textSecondary, marginBottom: 20 }}>
            Ingresa tu correo y te enviaremos instrucciones para restablecer tu contraseña
          </Text>

          <Text style={styles.label}>Correo Electrónico</Text>
          <TextInput
            style={styles.input}
            placeholder="correo@ejemplo.com"
            value={email}
            onChangeText={setEmail}
            autoCapitalize="none"
            keyboardType="email-address"
            editable={!loading && !sent}
          />

          <TouchableOpacity style={[styles.button, (loading || sent) && styles.buttonDisabled]} onPress={handleSend} disabled={loading || sent}>
            {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Enviar</Text>}
          </TouchableOpacity>

          <TouchableOpacity onPress={() => router.back()} style={{ marginTop: 20 }}>
            <Text style={{ textAlign: 'center', color: Colors.link }}>Volver al inicio de sesión</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = {
  label: { fontSize: 14, fontWeight: '500', marginBottom: 8, color: Colors.text },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 12, marginBottom: 16, backgroundColor: '#fff' },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonDisabled: { backgroundColor: Colors.textSecondary, opacity: 0.6 },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
};
