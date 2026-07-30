import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function ResetMasterKeyNewScreen() {
  const { changeMasterKey, verifying } = usePasswords();
  const [currentKey, setCurrentKey] = useState('');
  const [newKey, setNewKey] = useState('');
  const [confirmKey, setConfirmKey] = useState('');

  const handleReset = async () => {
    if (!currentKey) {
      Alert.alert('Error', 'Ingrese la clave maestra actual');
      return;
    }
    if (!newKey || newKey.length < 12 || newKey.length > 24) {
      Alert.alert('Error', 'La nueva clave maestra debe tener entre 12 y 24 caracteres');
      return;
    }
    if (newKey !== confirmKey) {
      Alert.alert('Error', 'No coinciden');
      return;
    }
    try {
      await changeMasterKey(currentKey, newKey);
      Alert.alert('Clave maestra actualizada', 'Tus contraseñas fueron recifradas con la nueva clave.');
      router.replace('/(app)/passwords');
    } catch (err: any) {
      Alert.alert('Error', err.response?.data?.message || err.message);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restablecer Contraseña Maestra</Text>
      <Text style={styles.label}>Clave maestra actual</Text>
      <TextInput style={styles.input} secureTextEntry value={currentKey} onChangeText={setCurrentKey} placeholder="Clave actual" />
      <Text style={styles.label}>Nueva contraseña maestra</Text>
      <TextInput style={styles.input} secureTextEntry value={newKey} onChangeText={setNewKey} placeholder="●●●●●" />
      <Text style={styles.label}>Confirmar contraseña maestra</Text>
      <TextInput style={styles.input} secureTextEntry value={confirmKey} onChangeText={setConfirmKey} placeholder="●●●●●" />
      <TouchableOpacity style={styles.button} onPress={handleReset} disabled={verifying}>
        {verifying ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Actualizar</Text>}
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
