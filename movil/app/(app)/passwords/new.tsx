import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { Colors } from '../../../src/constants/Colors';

export default function NewPasswordScreen() {
  const { createPassword } = usePasswords();
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [category, setCategory] = useState('');

  const handleSave = async () => {
    if (!name || !password) {
      Alert.alert('Error', 'Nombre y contraseña son obligatorios');
      return;
    }
    await createPassword({
      name,
      username,
      password,
      category,
      lastChangeDate: new Date().toISOString().split('T')[0],
    });
    router.back();
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nueva Contraseña</Text>
      <Text style={styles.label}>Nombre</Text>
      <TextInput style={styles.input} value={name} onChangeText={setName} placeholder="Ej. Netflix" />
      <Text style={styles.label}>Usuario (opcional)</Text>
      <TextInput style={styles.input} value={username} onChangeText={setUsername} placeholder="correo@ejemplo.com" />
      <Text style={styles.label}>Contraseña</Text>
      <TextInput style={styles.input} secureTextEntry value={password} onChangeText={setPassword} placeholder="********" />
      <Text style={styles.label}>Categoría (opcional)</Text>
      <TextInput style={styles.input} value={category} onChangeText={setCategory} placeholder="Entretenimiento" />
      <TouchableOpacity style={styles.saveButton} onPress={handleSave}>
        <Text style={styles.saveText}>Guardar</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.cancelButton} onPress={() => router.back()}>
        <Text style={styles.cancelText}>Cancelar</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 30 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
});
