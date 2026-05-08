import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../../src/hooks/usePasswords';
import { Colors } from '../../../../src/constants/Colors';

export default function CreateMasterKeyScreen() {
  const { createMasterKey, loading } = usePasswords();
  const [key, setKey] = useState('');
  const [confirmKey, setConfirmKey] = useState('');

  const handleCreate = async () => {
    if (!key || key.length < 8) {
      Alert.alert('Error', 'Mínimo 8 caracteres, una mayúscula, una minúscula y un carácter especial');
      return;
    }
    if (key !== confirmKey) {
      Alert.alert('Error', 'Las contraseñas no coinciden');
      return;
    }
    await createMasterKey(key);
    router.back();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Crear Contraseña Maestra</Text>
      <Text style={styles.subtitle}>
        Mínimo 8 caracteres, una(s) mayúscula(s), una(s) minúscula(s), un carácter especial
      </Text>
      <TextInput style={styles.input} secureTextEntry placeholder="Contraseña Maestra" value={key} onChangeText={setKey} />
      <TextInput style={styles.input} secureTextEntry placeholder="Confirmar contraseña maestra" value={confirmKey} onChangeText={setConfirmKey} />
      <Text style={styles.note}>Recibirás una notificación de actualización de contraseña maestra cada dos meses</Text>
      <TouchableOpacity style={styles.button} onPress={handleCreate} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Crear</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  subtitle: { fontSize: 14, color: Colors.textSecondary, marginBottom: 20, textAlign: 'center' },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: '#fff' },
  note: { fontSize: 12, color: Colors.textSecondary, textAlign: 'center', marginVertical: 20 },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
