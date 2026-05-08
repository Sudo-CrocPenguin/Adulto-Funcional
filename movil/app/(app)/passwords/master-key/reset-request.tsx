import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function ResetMasterKeyRequestScreen() {
  const { resetMasterKeyRequest } = usePasswords();
  const [email, setEmail] = useState('');

  const handleSend = async () => {
    if (!email.trim()) {
      Alert.alert('Error', 'Ingrese su correo electrónico');
      return;
    }
    await resetMasterKeyRequest(email);
    router.push('/(app)/passwords/master-key/reset-verify');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restablecer Contraseña Maestra</Text>
      <Text style={styles.text}>Verifiquemos que eres tú</Text>
      <TextInput style={styles.input} placeholder="Ingresa tu correo electrónico" value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />
      <Text style={styles.note}>Te enviaremos un código de verificación</Text>
      <TouchableOpacity style={styles.button} onPress={handleSend}>
        <Text style={styles.buttonText}>Enviar</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background, justifyContent: 'center' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  text: { fontSize: 16, textAlign: 'center', marginBottom: 20 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: '#fff' },
  note: { fontSize: 14, textAlign: 'center', marginBottom: 30, color: Colors.textSecondary },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
