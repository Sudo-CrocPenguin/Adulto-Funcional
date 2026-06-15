import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { Colors } from '../../../../src/constants/Colors';
import { usePasswords } from '../../../../src/hooks/usePasswords';

export default function CreateMasterKeyScreen() {
  const { createMasterKey, verifying } = usePasswords();
  const [masterKey, setMasterKey] = useState('');
  const [confirmMasterKey, setConfirmMasterKey] = useState('');

  const handleCreate = async () => {
    if (masterKey.length < 8) {
      Alert.alert('Error', 'La clave maestra debe tener al menos 8 caracteres');
      return;
    }
    if (masterKey !== confirmMasterKey) {
      Alert.alert('Error', 'Las claves maestras no coinciden');
      return;
    }

    try {
      await createMasterKey(masterKey);
      Alert.alert('Clave maestra creada', 'Ya puedes usar el gestor de contraseñas.');
      router.replace('/(app)/passwords');
    } catch (err: any) {
      Alert.alert('Error', err.response?.data?.message || err.message);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Clave Maestra</Text>
      <Text style={styles.message}>
        Crea una clave maestra para cifrar y desbloquear tus contraseñas guardadas.
      </Text>
      <TextInput
        style={styles.input}
        secureTextEntry
        value={masterKey}
        onChangeText={setMasterKey}
        placeholder="Clave maestra"
      />
      <TextInput
        style={styles.input}
        secureTextEntry
        value={confirmMasterKey}
        onChangeText={setConfirmMasterKey}
        placeholder="Confirmar clave maestra"
      />
      <TouchableOpacity style={styles.button} onPress={handleCreate} disabled={verifying}>
        {verifying ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Crear clave maestra</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20, backgroundColor: Colors.background },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 20 },
  message: { textAlign: 'center', marginBottom: 30, color: Colors.textSecondary },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: '#fff', width: '100%' },
  button: { backgroundColor: Colors.primary, paddingHorizontal: 20, paddingVertical: 12, borderRadius: 30 },
  buttonText: { color: '#fff', fontWeight: 'bold' },
});
