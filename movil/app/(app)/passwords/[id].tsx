import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert, ActivityIndicator, SafeAreaView } from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';
import DateTimePicker from '@react-native-community/datetimepicker';

export default function EditPasswordScreen() {
  const insets = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id: string }>();
  const { passwords, fetchPasswords, updatePassword, deletePassword } = usePasswords();
  const [loading, setLoading] = useState(true);
  const [applicationName, setApplicationName] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [lastChangeDate, setLastChangeDate] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);

  useFocusEffect(
    React.useCallback(() => {
      const loadPassword = async () => {
        if (!id) return;
        await fetchPasswords();
        const found = passwords.find(p => p.id === id);
        if (found) {
          setApplicationName(found.applicationName);
          setLastChangeDate(new Date(found.lastChangeDate));
          setLoading(false);
        } else if (passwords.length > 0) {
          Alert.alert('Error', 'No se encontró la contraseña');
          router.back();
        }
      };
      loadPassword();
    }, [id, passwords])
  );

  const handleSave = async () => {
    if (!applicationName.trim()) {
      Alert.alert('Error', 'El nombre de la aplicación es obligatorio');
      return;
    }
    try {
      await updatePassword(id, {
        applicationName: applicationName.trim(),
        password: password.trim() || undefined,
        lastChangeDate: lastChangeDate.toISOString().split('T')[0],
      });
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
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <ScrollView style={styles.container}>
        <Text style={styles.title}>Editar Contraseña</Text>

        <Text style={styles.label}>Aplicación</Text>
        <TextInput style={styles.input} value={applicationName} onChangeText={setApplicationName} />

        <Text style={styles.label}>Contraseña (dejar vacío si no se modifica)</Text>
        <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: '#ccc', borderRadius: 12 }}>
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

        <Text style={styles.label}>Fecha de último cambio</Text>
        <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
          <Text style={styles.dateButtonText}>{lastChangeDate.toLocaleDateString()}</Text>
        </TouchableOpacity>
        {showDatePicker && (
          <DateTimePicker
            value={lastChangeDate}
            mode="date"
            display="default"
            onChange={(event, selectedDate) => {
              setShowDatePicker(false);
              if (selectedDate) setLastChangeDate(selectedDate);
            }}
          />
        )}

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
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeContainer: { flex: 1, backgroundColor: Colors.background },
  container: { padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  dateButton: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 10, alignItems: 'center' },
  dateButtonText: { fontSize: 16, color: Colors.text },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 20 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  deleteButton: { backgroundColor: Colors.error, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  deleteText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
});
