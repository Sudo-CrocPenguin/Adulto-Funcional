import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { useFixedExpenses } from '../../../src/hooks/useFixedExpenses';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';

export default function NewFixedExpenseScreen() {
  const { createExpense } = useFixedExpenses();
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [frequency, setFrequency] = useState('Mensual');
  const [amount, setAmount] = useState('');
  const [nextDueDate, setNextDueDate] = useState('');
  const [status, setStatus] = useState('Activo');

  const handleSave = async () => {
    if (!name || !amount || parseFloat(amount) <= 0) {
      Alert.alert('Error', 'Nombre y monto válido requeridos');
      return;
    }
    await createExpense({
      name,
      category,
      frequency: frequency as any,
      amount: parseFloat(amount),
      nextDueDate: nextDueDate || new Date().toISOString().split('T')[0],
      status: status as any,
    });
    router.back();
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nuevo Gasto Fijo</Text>

      <Text style={styles.label}>Nombre</Text>
      <TextInput style={styles.input} value={name} onChangeText={setName} placeholder="Ej. Netflix" />

      <Text style={styles.label}>Clasificación</Text>
      <TextInput style={styles.input} value={category} onChangeText={setCategory} placeholder="Ej. Suscripción" />

      <Text style={styles.label}>Frecuencia</Text>
      <View style={styles.pickerContainer}>
        <Picker selectedValue={frequency} onValueChange={setFrequency}>
          <Picker.Item label="Diario" value="Diario" />
          <Picker.Item label="Semanal" value="Semanal" />
          <Picker.Item label="Mensual" value="Mensual" />
          <Picker.Item label="Anual" value="Anual" />
        </Picker>
      </View>

      <Text style={styles.label}>Monto</Text>
      <TextInput style={styles.input} keyboardType="numeric" value={amount} onChangeText={setAmount} placeholder="0.00" />

      <Text style={styles.label}>Próxima fecha de pago (YYYY-MM-DD)</Text>
      <TextInput style={styles.input} value={nextDueDate} onChangeText={setNextDueDate} placeholder={new Date().toISOString().split('T')[0]} />

      <Text style={styles.label}>Estado</Text>
      <View style={styles.pickerContainer}>
        <Picker selectedValue={status} onValueChange={setStatus}>
          <Picker.Item label="Activo" value="Activo" />
          <Picker.Item label="Inactivo" value="Inactivo" />
        </Picker>
      </View>

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
