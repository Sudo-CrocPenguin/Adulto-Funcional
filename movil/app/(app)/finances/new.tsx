import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, ActivityIndicator, Modal } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useMovements } from '../../../src/hooks/useMovements';
import { useCategories } from '../../../src/hooks/useCategories';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import { BottomNav } from '../../../src/components/common/BottomNav';
import { getApiErrorMessage } from '../../../src/services/errorHandler';

export default function NewMovementScreen() {
  const insets = useSafeAreaInsets();
  const { createMovement } = useMovements();
  const { categories, loading: categoriesLoading, createCategory } = useCategories('FINANCES');
  const [type, setType] = useState<'INCOME' | 'EXPENSE'>('INCOME');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date());
  const [description, setDescription] = useState('');
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
    } catch (err: unknown) {
      Alert.alert('Error', getApiErrorMessage(err, 'No se pudo crear la categoría'));
    } finally {
      setCreating(false);
    }
  };

  const handleSave = async () => {
    if (!amount || parseFloat(amount) <= 0) {
      Alert.alert('Error', 'Ingrese un monto válido');
      return;
    }
    if (!selectedCategoryId) {
      Alert.alert('Error', 'Debe seleccionar una categoría');
      return;
    }
    const selectedCategory = categories.find(c => c.id === selectedCategoryId);
    if (!selectedCategory) {
      Alert.alert('Error', 'Categoría no válida');
      return;
    }
    try {
      await createMovement({
        movementType: type,
        amount: parseFloat(amount),
        movementDate: date.toISOString().split('T')[0],
        description: description.trim() || undefined,
        category: { id: selectedCategory.id, name: selectedCategory.name },
      });
      router.back();
    } catch (err: unknown) {
      Alert.alert('Error', getApiErrorMessage(err, 'No se pudo crear el movimiento'));
    }
  };

  if (categoriesLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      <ScrollView style={styles.scroll}>
        <Text style={styles.title}>Nuevo Movimiento</Text>

        <Text style={styles.label}>Tipo</Text>
        <View style={styles.pickerContainer}>
          <Picker selectedValue={type} onValueChange={setType}>
            <Picker.Item label="Ingreso" value="INCOME" />
            <Picker.Item label="Egreso" value="EXPENSE" />
          </Picker>
        </View>

        <Text style={styles.label}>Categoría</Text>
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

        <Text style={styles.label}>Monto *</Text>
        <TextInput
          style={styles.input}
          keyboardType="numeric"
          value={amount}
          onChangeText={setAmount}
          placeholder="0.00"
        />

        <Text style={styles.label}>Fecha</Text>
        <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
          <Text style={styles.dateButtonText}>{date.toLocaleDateString()}</Text>
        </TouchableOpacity>
        {showDatePicker && (
          <DateTimePicker value={date} mode="date" display="default" onChange={(e, d) => { setShowDatePicker(false); if (d) setDate(d); }} />
        )}

        <Text style={styles.label}>Descripción (opcional)</Text>
        <TextInput
          style={styles.input}
          value={description}
          onChangeText={setDescription}
          placeholder="Ej. Supermercado"
        />

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
  container: { flex: 1, backgroundColor: Colors.background },
  scroll: { padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  pickerContainer: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, backgroundColor: '#fff', marginBottom: 8 },
  linkButton: { alignItems: 'center', marginTop: 4 },
  linkText: { color: Colors.link, fontSize: 14, fontWeight: '500' },
  dateButton: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 10, alignItems: 'center' },
  dateButtonText: { fontSize: 16, color: Colors.text },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 30 },
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
