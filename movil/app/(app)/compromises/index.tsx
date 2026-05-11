import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useEvents, Event } from '../../../src/hooks/useEvents';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

type FilterType = 'Todas' | 'Pendientes' | 'Completadas';

export default function CompromisesScreen() {
  const { events, loading, error, fetchEvents, deleteEvent, completeEvent } = useEvents();
  const [filter, setFilter] = useState<FilterType>('Todas');

  useFocusEffect(
    useCallback(() => {
      fetchEvents();
    }, [fetchEvents])
  );

  const filteredEvents = events.filter(event => {
    if (filter === 'Pendientes') return event.status === 'Pendiente';
    if (filter === 'Completadas') return event.status === 'Completado';
    return true;
  });

  const getPriorityStyle = (priority: string) => {
    switch (priority) {
      case 'Alta': return styles.priorityHigh;
      case 'Media': return styles.priorityMedium;
      default: return styles.priorityLow;
    }
  };

  const handleComplete = async (event: Event) => {
    Alert.alert(
      'Completar compromiso',
      `¿Marcar "${event.title}" como completado? Esto aumentará su racha.`,
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Completar', onPress: async () => {
          try {
            await completeEvent(event.id);
            // La lista se actualizará automáticamente con el nuevo estado y fecha
          } catch (err: any) {
            Alert.alert('Error', err.message);
          }
        }}
      ]
    );
  };

  const renderItem = ({ item }: { item: Event }) => (
    <TouchableOpacity style={styles.card} onPress={() => router.push(`/(app)/compromises/${item.id}`)}>
      <View style={styles.cardHeader}>
        <Text style={styles.title}>{item.title}</Text>
        <View style={[styles.priorityBadge, getPriorityStyle(item.priority)]}>
          <Text style={styles.priorityText}>{item.priority}</Text>
        </View>
      </View>
      <Text style={styles.category}>{item.category?.name || 'Sin categoría'}</Text>
      <Text style={styles.frequency}>
        {item.frequency === 0 ? 'Única' : item.frequency === 1 ? 'Diaria' : item.frequency === 7 ? 'Semanal' : item.frequency === 30 ? 'Mensual' : 'Anual'}
      </Text>
      <Text style={styles.date}>{new Date(item.eventDate).toLocaleDateString()}</Text>
      {item.frequency > 0 && item.streak !== undefined && (
        <Text style={styles.streak}>🔥 Racha: {item.streak}</Text>
      )}
      <View style={styles.statusRow}>
        <Text style={[styles.status, item.status === 'Completado' ? styles.statusCompleted : styles.statusPending]}>
          {item.status}
        </Text>
        <View style={styles.actionButtons}>
          {item.status !== 'Completado' && (
            <TouchableOpacity onPress={() => handleComplete(item)} style={styles.completeButton}>
              <Text style={styles.completeText}>✔️</Text>
            </TouchableOpacity>
          )}
          <TouchableOpacity onPress={() => Alert.alert('Eliminar', '¿Eliminar compromiso?', [
            { text: 'Cancelar', style: 'cancel' },
            { text: 'Eliminar', style: 'destructive', onPress: () => deleteEvent(item.id) }
          ])}>
            <Text style={styles.delete}>🗑️</Text>
          </TouchableOpacity>
        </View>
      </View>
    </TouchableOpacity>
  );

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <View style={styles.container}>
      <View style={styles.streakContainer}>
        <Text style={styles.streakTitle}>Racha de Compromisos</Text>
        <Text style={styles.streakNumber}>7 días activos</Text>
        <View style={styles.streakDays}>
          {[7, 15, 23, 30].map(day => <Text key={day} style={styles.streakDay}>{day}</Text>)}
        </View>
      </View>

      <View style={styles.filterRow}>
        {(['Todas', 'Pendientes', 'Completadas'] as FilterType[]).map(f => (
          <TouchableOpacity key={f} onPress={() => setFilter(f)} style={[styles.filterButton, filter === f && styles.filterActive]}>
            <Text style={[styles.filterText, filter === f && styles.filterTextActive]}>{f}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filteredEvents}
        keyExtractor={item => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingBottom: 80 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay compromisos</Text>}
      />

      <TouchableOpacity style={styles.fab} onPress={() => router.push('/(app)/compromises/new')}>
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>

      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  streakContainer: { backgroundColor: '#fff', margin: 16, padding: 16, borderRadius: 24, alignItems: 'center' },
  streakTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  streakNumber: { fontSize: 28, fontWeight: 'bold', color: Colors.primary, marginVertical: 8 },
  streakDays: { flexDirection: 'row', justifyContent: 'space-around', width: '100%' },
  streakDay: { fontSize: 14, color: Colors.textSecondary },
  filterRow: { flexDirection: 'row', justifyContent: 'space-around', marginHorizontal: 16, marginBottom: 12 },
  filterButton: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 20, backgroundColor: '#eee' },
  filterActive: { backgroundColor: Colors.primary },
  filterText: { color: Colors.textSecondary },
  filterTextActive: { color: '#fff', fontWeight: 'bold' },
  card: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 20 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  title: { fontSize: 16, fontWeight: 'bold', color: Colors.text, flex: 1 },
  priorityBadge: { paddingHorizontal: 10, paddingVertical: 2, borderRadius: 12, alignSelf: 'flex-start' },
  priorityHigh: { backgroundColor: '#FDD' },
  priorityMedium: { backgroundColor: '#FFD' },
  priorityLow: { backgroundColor: '#DFD' },
  priorityText: { fontSize: 12, fontWeight: 'bold' },
  category: { fontSize: 14, color: Colors.textSecondary, marginBottom: 4 },
  frequency: { fontSize: 12, color: Colors.textSecondary },
  date: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  streak: { fontSize: 12, fontWeight: 'bold', color: Colors.primary, marginTop: 4 },
  statusRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 },
  status: { fontSize: 12, fontWeight: 'bold' },
  statusPending: { color: Colors.error },
  statusCompleted: { color: Colors.success },
  actionButtons: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  completeButton: { marginRight: 12 },
  completeText: { fontSize: 16 },
  delete: { fontSize: 16, color: Colors.error },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 5 },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
