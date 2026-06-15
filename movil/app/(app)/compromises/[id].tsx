import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert, Platform, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useEvents } from '../../../src/hooks/useEvents';
import { useCategories } from '../../../src/hooks/useCategories';
import { Colors } from '../../../src/constants/Colors';
import { Picker } from '@react-native-picker/picker';
import DateTimePicker from '@react-native-community/datetimepicker';
import type { Event, EventPriority } from '../../../src/api/agendaApi';

export default function EditCompromisoScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { events, fetchEvents, updateEvent, deleteEvent } = useEvents();
  const { categories, loading: categoriesLoading } = useCategories('AGENDA');

  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [frequency, setFrequency] = useState(0);
  const [priority, setPriority] = useState<EventPriority>('Media');
  const [eventDate, setEventDate] = useState(new Date());
  const [startHour, setStartHour] = useState(new Date());
  const [endHour, setEndHour] = useState(new Date());
  const [reminder, setReminder] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showStartPicker, setShowStartPicker] = useState(false);
  const [showEndPicker, setShowEndPicker] = useState(false);

  useEffect(() => {
    const loadEvent = async () => {
      if (!id) return;
      try {
        setLoading(true);
        await fetchEvents();
      } catch (err) {
        Alert.alert('Error', 'No se pudo cargar el compromiso');
        router.back();
      }
    };
    loadEvent();
  }, [id]);

  // Efecto que se ejecuta cuando 'events' cambia (después de fetchEvents)
  useEffect(() => {
    if (events.length > 0 && id && loading) {
      const event = events.find(e => e.id === id);
      if (event) {
        fillForm(event);
        setLoading(false);
      } else {
        Alert.alert('Error', 'No se encontró el compromiso');
        router.back();
      }
    }
  }, [events, id]);

  const fillForm = (event: Event) => {
    setTitle(event.title);
    setCategoryId(event.category?.id || '');
    setFrequency(event.frequency);
    setPriority(event.priority);
    setEventDate(new Date(event.eventDate));
    setStartHour(new Date(event.startHour));
    setEndHour(new Date(event.endHour));
    setReminder(new Date(event.reminder));
  };

  const handleSave = async () => {
    if (!title.trim()) {
      Alert.alert('Error', 'El título es obligatorio');
      return;
    }
    if (!categoryId) {
      Alert.alert('Error', 'Debe seleccionar una categoría');
      return;
    }
    const selectedCategory = categories.find(c => c.id === categoryId);
    if (!selectedCategory) {
      Alert.alert('Error', 'Categoría no válida');
      return;
    }
    try {
      await updateEvent(id, {
        title: title.trim(),
        priority,
        eventDate: eventDate.toISOString().split('T')[0],
        frequency,
        reminder: reminder.toISOString(),
        startHour: startHour.toISOString(),
        endHour: endHour.toISOString(),
        category: { id: categoryId, name: selectedCategory.name },
      });
      router.back();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  const handleComplete = async () => {
    Alert.alert(
      'Completar compromiso',
      '¿Marcar este compromiso como completado?',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Completar', onPress: async () => {
          try {
            await updateEvent(id, { status: 'Completado' });
            router.back();
          } catch (err: any) {
            Alert.alert('Error', err.message);
          }
        }}
      ]
    );
  };

  const handleDelete = () => {
    Alert.alert(
      'Eliminar compromiso',
      '¿Estás seguro? Esta acción no se puede deshacer.',
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Eliminar', style: 'destructive', onPress: async () => {
          await deleteEvent(id);
          router.back();
        }},
      ]
    );
  };

  if (loading || categoriesLoading) {
    return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Editar Compromiso</Text>

      <Text style={styles.label}>Título *</Text>
      <TextInput style={styles.input} value={title} onChangeText={setTitle} />

      <Text style={styles.label}>Categoría *</Text>
      <View style={styles.pickerContainer}>
        <Picker selectedValue={categoryId} onValueChange={setCategoryId}>
          {categories.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
        </Picker>
      </View>

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
        {(['Alta', 'Media', 'Baja'] as const).map(p => (
          <TouchableOpacity key={p} onPress={() => setPriority(p)} style={[styles.priorityButton, priority === p && styles.priorityActive]}>
            <Text style={styles.priorityText}>{p}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={styles.label}>Fecha del evento</Text>
      <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
        <Text style={styles.dateButtonText}>{eventDate.toLocaleDateString()}</Text>
      </TouchableOpacity>
      {showDatePicker && (
        <DateTimePicker value={eventDate} mode="date" display="default" onChange={(e, d) => { setShowDatePicker(false); if (d) setEventDate(d); }} />
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
        <Text style={styles.saveText}>Guardar cambios</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.completeButton} onPress={handleComplete}>
        <Text style={styles.completeText}>Marcar como completado</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.deleteButton} onPress={handleDelete}>
        <Text style={styles.deleteText}>Eliminar compromiso</Text>
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
  pickerContainer: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, backgroundColor: '#fff', marginBottom: 8 },
  priorityRow: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 8 },
  priorityButton: { flex: 1, paddingVertical: 10, marginHorizontal: 4, borderRadius: 12, backgroundColor: '#eee', alignItems: 'center' },
  priorityActive: { backgroundColor: Colors.primary },
  priorityText: { fontWeight: 'bold' },
  dateButton: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#ccc', borderRadius: 12, padding: 12, marginBottom: 10, alignItems: 'center' },
  dateButtonText: { fontSize: 16, color: Colors.text },
  saveButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 20 },
  saveText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  completeButton: { backgroundColor: Colors.success, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  completeText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  deleteButton: { backgroundColor: Colors.error, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  deleteText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  cancelButton: { backgroundColor: '#ccc', paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 12 },
  cancelText: { color: '#333', fontWeight: 'bold' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
});
