import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function ResetMasterKeyNewScreen() {
  const { resetMasterKeyVerify, loading } = usePasswords();
  const { code } = useLocalSearchParams<{ code: string }>();
  const [newKey, setNewKey] = useState('');
  const [confirmKey, setConfirmKey] = useState('');

  const handleReset = async () => {
    if (!newKey || newKey.length < 8) {
      Alert.alert('Error', 'Mínimo 8 caracteres');
      return;
    }
    if (newKey !== confirmKey) {
      Alert.alert('Error', 'No coinciden');
      return;
    }
    await resetMasterKeyVerify(code!, newKey);
    router.back();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restablecer Contraseña Maestra</Text>
      <Text style={styles.label}>Nueva contraseña maestra</Text>
      <TextInput style={styles.input} secureTextEntry value={newKey} onChangeText={setNewKey} placeholder="●●●●●" />
      <Text style={styles.label}>Confirmar contraseña maestra</Text>
      <TextInput style={styles.input} secureTextEntry value={confirmKey} onChangeText={setConfirmKey} placeholder="●●●●●" />
      <TouchableOpacity style={styles.button} onPress={handleReset} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Restablecer</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background, justifyContent: 'center' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 30, textAlign: 'center' },
  label: { fontSize: 16, marginBottom: 8, color: Colors.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 20, backgroundColor: '#fff' },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
