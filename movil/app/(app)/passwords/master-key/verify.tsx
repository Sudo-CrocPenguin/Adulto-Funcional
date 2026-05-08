import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function VerifyMasterKeyScreen() {
  const { verifyMasterKey, loading } = usePasswords();
  const [key, setKey] = useState('');

  const handleVerify = async () => {
    if (!key.trim()) {
      Alert.alert('Error', 'Ingrese la contraseña maestra');
      return;
    }
    try {
      await verifyMasterKey(key);
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Ingresa la contraseña maestra</Text>
      <TextInput
        style={styles.input}
        secureTextEntry
        value={key}
        onChangeText={setKey}
        placeholder="●●●●●"
        placeholderTextColor="#999"
      />
      <TouchableOpacity style={styles.button} onPress={handleVerify} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Ingresar</Text>}
      </TouchableOpacity>
      <TouchableOpacity onPress={() => router.push('/(app)/passwords/master-key/reset-request')}>
        <Text style={styles.link}>¿Necesitas ayuda?</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={() => router.push('/(app)/passwords/master-key/create')}>
        <Text style={styles.link}>¿No tienes contraseña maestra? Crear una</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 20, fontWeight: 'bold', marginBottom: 30, textAlign: 'center' },
  input: { width: '80%', borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, textAlign: 'center', fontSize: 24, letterSpacing: 4, backgroundColor: '#fff', marginBottom: 20 },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, paddingHorizontal: 40, borderRadius: 30, marginBottom: 20 },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  link: { color: Colors.link, marginTop: 15, textDecorationLine: 'underline' },
});
