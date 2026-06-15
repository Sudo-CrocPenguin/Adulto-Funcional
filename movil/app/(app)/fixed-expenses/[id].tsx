import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFixedExpenses } from '../../../src/hooks/useFixedExpenses';
import { useCategories } from '../../../src/hooks/useCategories';
import { Colors } from '../../../src/constants/Colors';
import type { FixedExpense, FixedExpenseFrequency, FixedExpenseStatus } from '../../../src/api/financesApi';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import { BottomNav } from '../../../src/components/common/BottomNav';

const frequencyOptions: Array<{ label: string; value: FixedExpenseFrequency }> = [
  { label: 'Semanal', value: 'WEEKLY' },
  { label: 'Quincenal', value: 'BIWEEKLY' },
  { label: 'Mensual', value: 'MONTHLY' },
  { label: 'Trimestral', value: 'QUARTERLY' },
  { label: 'Semestral', value: 'SEMIANNUAL' },
  { label: 'Anual', value: 'ANNUAL' },
];

const statusOptions: Array<{ label: string; value: FixedExpenseStatus }> = [
  { label: 'Activo', value: 'ACTIVE' },
  { label: 'Inactivo', value: 'INACTIVE' },
];

export default function EditFixedExpenseScreen() {
  const insets = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id: string }>();
  const { expenses, fetchExpenses, updateExpense, deleteExpense } = useFixedExpenses();
  const { categories, loading: categoriesLoading } = useCategories('FINANCES');

  const [loading, setLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(true);
  const [expense, setExpense] = useState<FixedExpense | null>(null);
  const [name, setName] = useState('');
  const [frequency, setFrequency] = useState<FixedExpenseFrequency>('MONTHLY');
  const [amount, setAmount] = useState('');
  const [status, setStatus] = useState<FixedExpenseStatus>('ACTIVE');
  const [nextDueDate, setNextDueDate] = useState(new Date());
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);

  // Cargar lista completa al enfocar la pantalla
  useFocusEffect(
    useCallback(() => {
      const loadData = async () => {
        setLoading(true);
        await fetchExpenses();
        setLoading(false);
      };
      loadData();
    }, [fetchExpenses])
  );

  // Cuando la lista cambie, buscar el gasto por ID
  React.useEffect(() => {
    if (!loading && expenses.length > 0 && id) {
      const found = expenses.find(e => e.id === id);
      if (found) {
        setExpense(found);
        setName(found.name);
        setFrequency(found.frequency);
        setAmount(found.amount.toString());
        setStatus(found.status);
        setNextDueDate(new Date(found.nextDueDate));
        setSelectedCategoryId(found.category?.id || null);
        setFormLoading(false);
      } else if (!loading && expenses.length > 0) {
        Alert.alert('Error', 'No se encontró el gasto fijo');
        router.back();
      }
    } else if (!loading && expenses.length === 0) {
      // No hay gastos, esperar a la siguiente carga
      setFormLoading(true);
    }
  }, [expenses, loading, id]);

  const handleSave = async () => {
    if (!name.trim()) {
      Alert.alert('Error', 'El nombre es obligatorio');
      return;
    }
    const amountNum = parseFloat(amount);
    if (isNaN(amountNum) || amountNum <= 0) {
      Alert.alert('Error', 'Ingrese un monto válido');
      return;
    }
    if (!selectedCategoryId) {
      Alert.alert('Error', 'Debe seleccionar una categoría');
      return;
    }
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (nextDueDate <= today) {
      Alert.alert('Error', 'La próxima fecha de pago debe ser mayor a hoy');
      return;
    }
    try {
      await updateExpense(id, {
        name: name.trim(),
        frequency,
        amount: amountNum,
        status,
        nextDueDate: nextDueDate.toISOString().split('T')[0],
        category: { id: selectedCategoryId, name: '' },
      });
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      'Eliminar gasto fijo',
      '¿Estás seguro? Esta acción no se puede deshacer.',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Eliminar', style: 'destructive', onPress: async () => {
          await deleteExpense(id);
          router.back();
        }},
      ]
    );
  };

  if (loading || formLoading || categoriesLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  if (!expense) {
    return (
      <View style={styles.centered}>
        <Text>No se pudo cargar el gasto fijo</Text>
      </View>
    );
  }

  return (
    <View style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <ScrollView style={styles.container}>
        <Text style={styles.title}>Editar Gasto Fijo</Text>

        <Text style={styles.label}>Nombre</Text>
        <TextInput style={styles.input} value={name} onChangeText={setName} />

        <Text style={styles.label}>Categoría</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={selectedCategoryId} onValueChange={setSelectedCategoryId}>
            {categories.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
          </Picker>
        </View>

        <Text style={styles.label}>Frecuencia</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={frequency} onValueChange={setFrequency}>
            {frequencyOptions.map(opt => <Picker.Item key={opt.value} label={opt.label} value={opt.value} />)}
          </Picker>
        </View>

        <Text style={styles.label}>Monto</Text>
        <TextInput style={styles.input} keyboardType="numeric" value={amount} onChangeText={setAmount} />

        <Text style={styles.label}>Estado</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={status} onValueChange={setStatus}>
            {statusOptions.map(opt => <Picker.Item key={opt.value} label={opt.label} value={opt.value} />)}
          </Picker>
        </View>

        <Text style={styles.label}>Próxima fecha de pago</Text>
        <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
          <Text style={styles.dateButtonText}>{nextDueDate.toLocaleDateString()}</Text>
        </TouchableOpacity>
        {showDatePicker && (
          <DateTimePicker
            value={nextDueDate}
            mode="date"
            display="default"
            minimumDate={new Date()}
            onChange={(event, selectedDate) => {
              setShowDatePicker(false);
              if (selectedDate) setNextDueDate(selectedDate);
            }}
          />
        )}

        <TouchableOpacity style={styles.saveButton} onPress={handleSave}>
          <Text style={styles.saveText}>Guardar cambios</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.deleteButton} onPress={handleDelete}>
          <Text style={styles.deleteText}>Eliminar gasto fijo</Text>
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
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  pickerContainer: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, backgroundColor: '#fff', marginBottom: 8 },
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
