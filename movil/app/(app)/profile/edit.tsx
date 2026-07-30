import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useProfile } from '../../../src/hooks/useProfile';
import { Colors } from '../../../src/constants/Colors';
import { isValidColombianPhone, isValidEmail } from '../../../src/utils/validators';
import { useAuth } from '../../../src/contexts/AuthContext';

export default function EditProfileScreen() {
  const { profile, loading, updateProfile } = useProfile();
  const { refreshUser } = useAuth();
  const [names, setNames] = useState('');
  const [lastnames, setLastnames] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (profile) {
      setNames(profile.names);
      setLastnames(profile.lastnames);
      setPhone(profile.phone);
      setEmail(profile.email);
    }
  }, [profile]);

  const validate = () => {
    if (!names.trim()) return 'El nombre es obligatorio';
    if (names.length > 50) return 'El nombre no puede exceder 50 caracteres';
    if (!lastnames.trim()) return 'Los apellidos son obligatorios';
    if (lastnames.length > 50) return 'Los apellidos no pueden exceder 50 caracteres';
    if (!phone.trim()) return 'El teléfono es obligatorio';
    if (!isValidColombianPhone(phone)) return 'Teléfono colombiano inválido (ej: 3001234567)';
    if (!email.trim()) return 'El correo es obligatorio';
    if (!isValidEmail(email)) return 'Formato de correo inválido';
    return null;
  };

  const handleSave = async () => {
    const errMsg = validate();
    if (errMsg) {
      setError(errMsg);
      return;
    }
    setSaving(true);
    try {
      await updateProfile({
        names: names.trim(),
        lastnames: lastnames.trim(),
        phone: phone.trim(),
        email: email.trim(),
      });
      await refreshUser();
      Alert.alert('Éxito', 'Perfil actualizado correctamente');
      router.back();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al actualizar';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Editar Perfil</Text>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <Text style={styles.label}>Nombres</Text>
      <TextInput style={styles.input} value={names} onChangeText={(text) => { setNames(text); setError(''); }} />
      <Text style={styles.label}>Apellidos</Text>
      <TextInput style={styles.input} value={lastnames} onChangeText={(text) => { setLastnames(text); setError(''); }} />
      <Text style={styles.label}>Teléfono (ej: 3001234567)</Text>
      <TextInput style={styles.input} value={phone} onChangeText={(text) => { setPhone(text); setError(''); }} keyboardType="phone-pad" />
      <Text style={styles.label}>Correo electrónico</Text>
      <TextInput style={styles.input} value={email} onChangeText={(text) => { setEmail(text); setError(''); }} autoCapitalize="none" keyboardType="email-address" />
      <TouchableOpacity style={styles.saveButton} onPress={handleSave} disabled={saving}>
        {saving ? <ActivityIndicator color="#fff" /> : <Text style={styles.saveText}>Guardar cambios</Text>}
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 30 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  error: { color: Colors.error, textAlign: 'center', marginBottom: 16 },
});
