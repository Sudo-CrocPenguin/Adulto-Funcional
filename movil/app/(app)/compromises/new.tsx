import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert, Platform, ActivityIndicator, Modal } from 'react-native';
import { router } from 'expo-router';
import { useEvents } from '../../../src/hooks/useEvents';
import { useCategories } from '../../../src/hooks/useCategories';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';

export default function NewCompromisoScreen() {
  const { createEvent } = useEvents();
  const { categories, loading: categoriesLoading, createCategory } = useCategories('AGENDA');
  const [title, setTitle] = useState('');
  const [frequency, setFrequency] = useState(0);
  const [priority, setPriority] = useState<'ALTA' | 'MEDIA' | 'BAJA'>('MEDIA');
  const [date, setDate] = useState(new Date());
  const [startHour, setStartHour] = useState(new Date());
  const [endHour, setEndHour] = useState(new Date(new Date().getTime() + 3600000));
  const [selectedCategoryId, setSelectedCategoryId] = useState<string | null>(null);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showStartPicker, setShowStartPicker] = useState(false);
  const [showEndPicker, setShowEndPicker] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [creating, setCreating] = useState(false);

  // Seleccionar primera categoría si existe
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
    // Validaciones
    if (!title.trim()) {
      Alert.alert('Error', 'El título es obligatorio');
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
      await createEvent({
        title,
        priority,
        eventDate: date.toISOString().split('T')[0],
        frequency,
        reminder: new Date(date.getTime() - 86400000).toISOString(),
        startHour: startHour.toISOString(),
        endHour: endHour.toISOString(),
        description: '',
        status: 'PENDIENTE',
        category: { id: selectedCategory.id, name: selectedCategory.name },
      });
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  if (categoriesLoading) {
    return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Nuevo Compromiso</Text>

      <Text style={styles.label}>Título *</Text>
      <TextInput style={styles.input} value={title} onChangeText={setTitle} placeholder="Ej. Reunión con equipo" />

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
          <Picker.Item label="Única" value={0} />
          <Picker.Item label="Diaria" value={1} />
          <Picker.Item label="Semanal" value={7} />
          <Picker.Item label="Mensual" value={30} />
          <Picker.Item label="Anual" value={365} />
        </Picker>
      </View>

      <Text style={styles.label}>Prioridad</Text>
      <View style={styles.priorityRow}>
        {(['ALTA', 'MEDIA', 'BAJA'] as const).map(p => (
          <TouchableOpacity key={p} onPress={() => setPriority(p)} style={[styles.priorityButton, priority === p && styles.priorityActive]}>
            <Text style={styles.priorityText}>{p}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={styles.label}>Fecha del evento</Text>
      <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
        <Text style={styles.dateButtonText}>{date.toLocaleDateString()}</Text>
      </TouchableOpacity>
      {showDatePicker && (
        <DateTimePicker value={date} mode="date" display="default" onChange={(e, d) => { setShowDatePicker(false); if (d) setDate(d); }} />
      )}

      <Text style={styles.label}>Hora de inicio</Text>
      <TouchableOpacity style={styles.dateButton} onPress={() => setShowStartPicker(true)}>
        <Text style={styles.dateButtonText}>{startHour.toLocaleTimeString()}</Text>
      </TouchableOpacity>
      {showStartPicker && (
        <DateTimePicker value={startHour} mode="time" display="default" onChange={(e, d) => { setShowStartPicker(false); if (d) setStartHour(d); }} />
      )}

      <Text style={styles.label}>Hora de fin</Text>
      <TouchableOpacity style={styles.dateButton} onPress={() => setShowEndPicker(true)}>
        <Text style={styles.dateButtonText}>{endHour.toLocaleTimeString()}</Text>
      </TouchableOpacity>
      {showEndPicker && (
        <DateTimePicker value={endHour} mode="time" display="default" onChange={(e, d) => { setShowEndPicker(false); if (d) setEndHour(d); }} />
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
            <Text style={styles.modalTitle}>Nueva categoría (AGENDA)</Text>
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
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  label: { fontSize: 16, fontWeight: '500', marginTop: 16, marginBottom: 6, color: Colors.text },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, backgroundColor: '#fff' },
  pickerContainer: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, backgroundColor: '#fff', marginBottom: 8 },
  linkButton: { alignItems: 'center', marginTop: 4 },
  linkText: { color: Colors.link, fontSize: 14, fontWeight: '500' },
  priorityRow: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 8 },
  priorityButton: { flex: 1, paddingVertical: 10, marginHorizontal: 4, borderRadius: 12, backgroundColor: '#eee', alignItems: 'center' },
  priorityActive: { backgroundColor: Colors.primary },
  priorityText: { fontWeight: 'bold' },
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
