import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { useProfile } from '../../../src/hooks/useProfile';
import { Colors } from '../../../src/constants/Colors';

export default function EditProfileScreen() {
  const { profile, updateProfile } = useProfile();
  const [names, setNames] = useState(profile?.names || '');
  const [lastnames, setLastnames] = useState(profile?.lastnames || '');
  const [email, setEmail] = useState(profile?.email || '');
  const [phone, setPhone] = useState(profile?.phone || '');

  const handleSave = async () => {
    if (!names || !lastnames || !email || !phone) {
      Alert.alert('Error', 'Todos los campos son obligatorios');
      return;
    }
    await updateProfile({ names, lastnames, email, phone });
    router.back();
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Editar Perfil</Text>
      <Text style={styles.label}>Nombres</Text>
      <TextInput style={styles.input} value={names} onChangeText={setNames} />
      <Text style={styles.label}>Apellidos</Text>
      <TextInput style={styles.input} value={lastnames} onChangeText={setLastnames} />
      <Text style={styles.label}>Correo electrónico</Text>
      <TextInput style={styles.input} value={email} onChangeText={setEmail} autoCapitalize="none" keyboardType="email-address" />
      <Text style={styles.label}>Teléfono</Text>
      <TextInput style={styles.input} value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
      <TouchableOpacity style={styles.saveButton} onPress={handleSave}>
        <Text style={styles.saveText}>Guardar cambios</Text>
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
});
