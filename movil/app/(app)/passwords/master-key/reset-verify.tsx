import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function ResetMasterKeyVerifyScreen() {
  const [code, setCode] = useState('');

  const handleVerify = () => {
    if (!code.trim()) {
      Alert.alert('Error', 'Ingrese el código');
      return;
    }
    router.push('/(app)/passwords/master-key/reset-new', { code });
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restablecer Contraseña Maestra</Text>
      <Text style={styles.text}>Verifiquemos que eres tú</Text>
      <TextInput style={styles.input} placeholder="Ingresa el código" value={code} onChangeText={setCode} />
      <TouchableOpacity onPress={() => Alert.alert('Reenviar', 'Código reenviado')}>
        <Text style={styles.link}>¿No recibiste el código? Reenviar</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.button} onPress={handleVerify}>
        <Text style={styles.buttonText}>Verificar</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background, justifyContent: 'center' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  text: { fontSize: 16, textAlign: 'center', marginBottom: 20 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: '#fff', textAlign: 'center', fontSize: 18 },
  link: { color: Colors.link, textAlign: 'center', marginBottom: 30, textDecorationLine: 'underline' },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
