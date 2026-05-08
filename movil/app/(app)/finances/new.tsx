import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { useMovements } from '../../../src/hooks/useMovements';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';

export default function NewMovementScreen() {
  const { createMovement } = useMovements();
  const [type, setType] = useState<'INGRESO' | 'EGRESO'>('INGRESO');
  const [amount, setAmount] = useState('');
  const [category, setCategory] = useState('');
  const [date, setDate] = useState('');
  const [description, setDescription] = useState('');

  const handleSave = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      Alert.alert('Error', 'Monto válido requerido');
      return;
    }
    if (!category) {
      Alert.alert('Error', 'Seleccione una categoría');
      return;
    }
    await createMovement({
      type,
      amount: parseFloat(amount),
      category,
      description,
      date: date || new Date().toISOString().split('T')[0],
    });
    router.back();
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nuevo Movimiento</Text>

      <Text style={styles.label}>Movimiento</Text>
      <View style={styles.pickerContainer}>
        <Picker selectedValue={type} onValueChange={(itemValue) => setType(itemValue)}>
          <Picker.Item label="Ingreso" value="INGRESO" />
          <Picker.Item label="Egreso" value="EGRESO" />
        </Picker>
      </View>

      <Text style={styles.label}>Monto</Text>
      <TextInput style={styles.input} keyboardType="numeric" value={amount} onChangeText={setAmount} placeholder="0.00" />

      <Text style={styles.label}>Clasificación</Text>
      <TextInput style={styles.input} value={category} onChangeText={setCategory} placeholder="Ej. Trabajo, Comida" />

      <Text style={styles.label}>Fecha (YYYY-MM-DD)</Text>
      <TextInput style={styles.input} value={date} onChangeText={setDate} placeholder={new Date().toISOString().split('T')[0]} />

      <Text style={styles.label}>Descripción (opcional)</Text>
      <TextInput style={styles.input} value={description} onChangeText={setDescription} placeholder="Opcional" />

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
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 30 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
});
