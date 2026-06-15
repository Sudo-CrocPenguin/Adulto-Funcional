import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, ActivityIndicator, Modal } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFixedExpenses } from '../../../src/hooks/useFixedExpenses';
import { useCategories } from '../../../src/hooks/useCategories';
import { Colors } from '../../../src/constants/Colors';
import type { FixedExpenseFrequency, FixedExpenseStatus } from '../../../src/api/financesApi';
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

export default function NewFixedExpenseScreen() {
  const insets = useSafeAreaInsets();
  const { createExpense } = useFixedExpenses();
  const { categories, loading: categoriesLoading, createCategory } = useCategories('FINANCES');
  const [name, setName] = useState('');
  const [frequency, setFrequency] = useState<FixedExpenseFrequency>('MONTHLY');
  const [amount, setAmount] = useState('');
  const [status, setStatus] = useState<FixedExpenseStatus>('ACTIVE');
  const [nextDueDate, setNextDueDate] = useState(() => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow;
  });
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [creating, setCreating] = useState(false);

  React.useEffect(() => {
    if (categories.length > 0 && !selectedCategoryId) {
      setSelectedCategoryId(categories[0].id);
    }
  }, [categories]);

  const handleCreateCategory = async () => {
    if (!newCategoryName.trim()) {
      Alert.alert('Error', 'El nombre de la categoría es obligatorio');
      return;
    }
    setCreating(true);
    try {
      const newCat = await createCategory(newCategoryName.trim());
      setSelectedCategoryId(newCat.id);
      setModalVisible(false);
      setNewCategoryName('');
    } catch (err: any) {
      Alert.alert('Error', err.response?.data?.message || err.message);
    } finally {
      setCreating(false);
    }
  };

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
    // Validar fecha futura
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (nextDueDate <= today) {
      Alert.alert('Error', 'La próxima fecha de pago debe ser mayor a hoy');
      return;
    }
    const selectedCategory = categories.find(c => c.id === selectedCategoryId);
    if (!selectedCategory) {
      Alert.alert('Error', 'Categoría no válida');
      return;
    }
    try {
      await createExpense({
        name: name.trim(),
        frequency,
        amount: amountNum,
        status,
        nextDueDate: nextDueDate.toISOString().split('T')[0],
        category: { id: selectedCategory.id, name: selectedCategory.name },
      });
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.response?.data?.message || err.message);
    }
  };

  if (categoriesLoading) {
    return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  }

  return (
    <View style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <ScrollView style={styles.container}>
        <Text style={styles.title}>Nuevo Gasto Fijo</Text>

        <Text style={styles.label}>Nombre *</Text>
        <TextInput style={styles.input} value={name} onChangeText={setName} placeholder="Ej. Netflix" />

        <Text style={styles.label}>Categoría *</Text>
        <View style={styles.pickerContainer}>
          {categories.length === 0 ? (
            <Text style={styles.emptyText}>No hay categorías. Crea una.</Text>
          ) : (
            <Picker selectedValue={selectedCategoryId} onValueChange={setSelectedCategoryId}>
              {categories.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
            </Picker>
          )}
        </View>
        <TouchableOpacity style={styles.linkButton} onPress={() => setModalVisible(true)}>
          <Text style={styles.linkText}>+ Nueva categoría</Text>
        </TouchableOpacity>

        <Text style={styles.label}>Frecuencia</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={frequency} onValueChange={setFrequency}>
            {frequencyOptions.map(opt => <Picker.Item key={opt.value} label={opt.label} value={opt.value} />)}
          </Picker>
        </View>

        <Text style={styles.label}>Monto *</Text>
        <TextInput style={styles.input} keyboardType="numeric" value={amount} onChangeText={setAmount} placeholder="0.00" />

        <Text style={styles.label}>Estado</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={status} onValueChange={setStatus}>
            {statusOptions.map(opt => <Picker.Item key={opt.value} label={opt.label} value={opt.value} />)}
          </Picker>
        </View>

        <Text style={styles.label}>Próxima fecha de pago *</Text>
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
          <Text style={styles.saveText}>Guardar</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.cancelButton} onPress={() => router.back()}>
          <Text style={styles.cancelText}>Cancelar</Text>
        </TouchableOpacity>

        <Modal visible={modalVisible} transparent animationType="slide">
          <View style={styles.modalContainer}>
            <View style={styles.modalContent}>
              <Text style={styles.modalTitle}>Nueva categoría (FINANZAS)</Text>
              <TextInput style={styles.input} placeholder="Nombre" value={newCategoryName} onChangeText={setNewCategoryName} />
              <View style={styles.modalButtons}>
                <TouchableOpacity style={styles.modalButton} onPress={handleCreateCategory} disabled={creating}>
                  <Text style={styles.modalButtonText}>{creating ? 'Creando...' : 'Crear'}</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.modalButtonCancel} onPress={() => setModalVisible(false)}>
                  <Text style={styles.modalButtonText}>Cancelar</Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        </Modal>
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
  linkButton: { alignItems: 'center', marginTop: 4 },
  linkText: { color: Colors.link, fontSize: 14, fontWeight: '500' },
  dateButton: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 10, alignItems: 'center' },
  dateButtonText: { fontSize: 16, color: Colors.text },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 20 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  emptyText: { textAlign: 'center', color: Colors.textSecondary, padding: 8 },
  modalContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.5)' },
  modalContent: { backgroundColor: '#fff', borderRadius: 20, padding: 20, width: '80%' },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 16, textAlign: 'center' },
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 20 },
  modalButton: { backgroundColor: Colors.primary, padding: 10, borderRadius: 10, flex: 1, marginRight: 8, alignItems: 'center' },
  modalButtonCancel: { backgroundColor: '#ccc', padding: 10, borderRadius: 10, flex: 1, marginLeft: 8, alignItems: 'center' },
  modalButtonText: { color: '#fff', fontWeight: 'bold' },
});
