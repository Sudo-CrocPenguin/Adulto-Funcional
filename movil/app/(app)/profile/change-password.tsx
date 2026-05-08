import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useProfile } from '../../../src/hooks/useProfile';
import { Colors } from '../../../src/constants/Colors';

export default function ChangePasswordScreen() {
  const { changePassword, loading } = useProfile();
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleChange = async () => {
    if (!oldPassword || !newPassword) {
      Alert.alert('Error', 'Complete todos los campos');
      return;
    }
    if (newPassword !== confirmPassword) {
      Alert.alert('Error', 'Las contraseñas nuevas no coinciden');
      return;
    }
    await changePassword(oldPassword, newPassword);
    router.back();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Cambiar contraseña</Text>
      <TextInput style={styles.input} secureTextEntry placeholder="Contraseña actual" value={oldPassword} onChangeText={setOldPassword} />
      <TextInput style={styles.input} secureTextEntry placeholder="Nueva contraseña" value={newPassword} onChangeText={setNewPassword} />
      <TextInput style={styles.input} secureTextEntry placeholder="Confirmar nueva contraseña" value={confirmPassword} onChangeText={setConfirmPassword} />
      <TouchableOpacity style={styles.button} onPress={handleChange} disabled={loading}>
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Actualizar contraseña</Text>}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background, justifyContent: 'center' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 30, textAlign: 'center' },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: '#fff' },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
