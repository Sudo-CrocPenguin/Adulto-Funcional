import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import { router } from 'expo-router';
import { useEvents } from '../../../src/hooks/useEvents';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';

export default function NewCompromisoScreen() {
  const { createEvent } = useEvents();
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [frequency, setFrequency] = useState('Única');
  const [priority, setPriority] = useState<'Alta' | 'Media' | 'Baja'>('Media');
  const [date, setDate] = useState('');
  const [reminder, setReminder] = useState('');

  const handleSave = async () => {
    if (!title.trim()) {
      Alert.alert('Error', 'El título es obligatorio');
      return;
    }
    if (!date.trim()) {
      Alert.alert('Error', 'La fecha es obligatoria');
      return;
    }
    await createEvent({
      title,
      category,
      frequency,
      priority,
      date,
      reminder,
      status: 'Pendiente',
    });
    router.back();
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nuevo Compromiso</Text>

      <Text style={styles.label}>Nombre</Text>
      <TextInput style={styles.input} value={title} onChangeText={setTitle} placeholder="Ej. Reunión" />

      <Text style={styles.label}>Categoría</Text>
      <TextInput style={styles.input} value={category} onChangeText={setCategory} placeholder="Trabajo, Personal, etc." />

      <Text style={styles.label}>Frecuencia</Text>
      <View style={styles.pickerContainer}>
        <Picker selectedValue={frequency} onValueChange={(itemValue) => setFrequency(itemValue)}>
          <Picker.Item label="Única" value="Única" />
          <Picker.Item label="Diaria" value="Diaria" />
          <Picker.Item label="Semanal" value="Semanal" />
          <Picker.Item label="Mensual" value="Mensual" />
        </Picker>
      </View>

      <Text style={styles.label}>Prioridad</Text>
      <View style={styles.priorityRow}>
        {(['Alta', 'Media', 'Baja'] as const).map(p => (
          <TouchableOpacity key={p} onPress={() => setPriority(p)} style={[styles.priorityButton, priority === p && styles.priorityActive]}>
            <Text style={styles.priorityText}>{p}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={styles.label}>Fecha (AAAA-MM-DD)</Text>
      <TextInput style={styles.input} value={date} onChangeText={setDate} placeholder="2026-05-20" />

      <Text style={styles.label}>Recordatorio (opcional)</Text>
      <TextInput style={styles.input} value={reminder} onChangeText={setReminder} placeholder="2026-05-19T10:00" />

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
  pickerContainer: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, backgroundColor: '#fff' },
  priorityRow: { flexDirection: 'row', justifyContent: 'space-between' },
  priorityButton: { flex: 1, paddingVertical: 10, marginHorizontal: 4, borderRadius: 12, backgroundColor: '#eee', alignItems: 'center' },
  priorityActive: { backgroundColor: Colors.primary },
  priorityText: { fontWeight: 'bold' },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 30 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
});
