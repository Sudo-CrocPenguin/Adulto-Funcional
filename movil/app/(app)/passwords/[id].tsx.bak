import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { securityApi } from '../../../src/api/securityApi';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function EditPasswordScreen() {
  const insets = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id: string }>();
  const { passwords, updatePassword, deletePassword } = usePasswords();
  const [loading, setLoading] = useState(true);
  const [applicationName, setApplicationName] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [originalPassword, setOriginalPassword] = useState('');

  useEffect(() => {
    const loadData = async () => {
      if (!id) return;
      // Primero intentar obtener la contraseña del listado (no contiene el password descifrado)
      const fromList = passwords.find(p => p.id === id);
      if (fromList) {
        setApplicationName(fromList.applicationName);
      }
      // Luego obtener la individual para tener la contraseña descifrada
      try {
        const response = await securityApi.getPassword(id);
        const data = response.data.data;
        setPassword(data.password || '');
        setOriginalPassword(data.password || '');
        if (!fromList) setApplicationName(data.applicationName);
      } catch (err) {
        console.warn('No se pudo obtener la contraseña descifrada', err);
        Alert.alert('Error', 'No se pudo cargar la contraseña');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [id]);

  const handleSave = async () => {
    if (!applicationName.trim()) {
      Alert.alert('Error', 'El nombre de la aplicación es obligatorio');
      return;
    }
    try {
      const updateData: any = { applicationName: applicationName.trim() };
      if (password.trim() && password !== originalPassword) {
        updateData.password = password.trim();
      }
      await updatePassword(id, updateData);
      Alert.alert('Éxito', 'Contraseña actualizada');
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      'Eliminar contraseña',
      '¿Estás seguro?',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Eliminar', style: 'destructive', onPress: async () => {
          await deletePassword(id);
          router.back();
        }}
      ]
    );
  };

  if (loading) {
    return (
      <View style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>
        <BottomNav />
      </View>
    );
  }

  return (
    <View style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <ScrollView style={styles.container}>
        <Text style={styles.title}>Editar Contraseña</Text>

        <Text style={styles.label}>Aplicación</Text>
        <TextInput style={styles.input} value={applicationName} onChangeText={setApplicationName} />

        <Text style={styles.label}>Contraseña</Text>
        <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: Colors.border, borderRadius: 12 }}>
          <TextInput
            style={{ flex: 1, padding: 12 }}
            secureTextEntry={!showPassword}
            value={password}
            onChangeText={setPassword}
            placeholder="Nueva contraseña"
          />
          <TouchableOpacity onPress={() => setShowPassword(!showPassword)} style={{ paddingHorizontal: 12 }}>
            <Text>{showPassword ? '👁️' : '👁️‍🗨️'}</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity style={styles.saveButton} onPress={handleSave}>
          <Text style={styles.saveText}>Guardar cambios</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.deleteButton} onPress={handleDelete}>
          <Text style={styles.deleteText}>Eliminar contraseña</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.cancelButton} onPress={() => router.back()}>
          <Text style={styles.cancelText}>Cancelar</Text>
        </TouchableOpacity>
      </ScrollView>
      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  safeContainer: { flex: 1, backgroundColor: Colors.background },
  container: { padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 20 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  deleteButton: { backgroundColor: Colors.error, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  deleteText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
});
