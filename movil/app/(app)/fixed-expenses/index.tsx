import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, SafeAreaView } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFixedExpenses } from '../../../src/hooks/useFixedExpenses';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';
import { formatCurrencyParts } from '../../../src/utils/currencyUtils';

type FilterType = 'Todos' | 'Próximos a vencer';

export default function FixedExpensesScreen() {
  const insets = useSafeAreaInsets();
  const { expenses, loading, error, fetchExpenses, markAsPaid } = useFixedExpenses();
  const [filter, setFilter] = useState<FilterType>('Todos');

  useFocusEffect(
    useCallback(() => {
      fetchExpenses();
    }, [fetchExpenses])
  );

  const filteredExpenses = filter === 'Próximos a vencer'
    ? expenses.filter(e => new Date(e.nextDueDate) >= new Date())
    : expenses;

  const getDaysLeft = (dateStr: string) => {
    const days = Math.ceil((new Date(dateStr).getTime() - new Date().getTime()) / (1000 * 3600 * 24));
    return days > 0 ? `${days} días` : 'hoy';
  };

  const handleMarkAsPaid = (item) => {
    Alert.alert(
      'Confirmar pago',
      `¿Registrar pago de ${item.name} por $${item.amount}?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        { text: 'Pagar', onPress: () => markAsPaid(item) }
      ]
    );
  };

  const renderItem = ({ item }) => {
    const parts = formatCurrencyParts(item.amount);
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.title}>{item.name}</Text>
          <Text style={styles.status}>{item.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}</Text>
        </View>
        <Text style={styles.category}>{item.category?.name || 'Sin categoría'}</Text>
        <Text style={styles.dueDate}>
          Próximo pago: {new Date(item.nextDueDate).toLocaleDateString()} ({getDaysLeft(item.nextDueDate)})
        </Text>
        <Text style={styles.amount}>
          <Text style={{ fontSize: 18, fontWeight: 'bold' }}>{parts.integer}</Text>
          <Text style={{ fontSize: 12 }}>.{parts.decimal}</Text>
        </Text>
        <Text style={styles.frequency}>{item.frequency}</Text>
        <View style={styles.buttonRow}>
          <TouchableOpacity style={styles.payButton} onPress={() => handleMarkAsPaid(item)}>
            <Text style={styles.payButtonText}>Pagar</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.editButton} onPress={() => router.push(`/(app)/fixed-expenses/${item.id}`)}>
            <Text style={styles.editButtonText}>Editar</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <View style={styles.filterRow}>
        <TouchableOpacity onPress={() => setFilter('Todos')} style={[styles.filterButton, filter === 'Todos' && styles.filterActive]}>
          <Text style={[styles.filterText, filter === 'Todos' && styles.filterTextActive]}>Todos</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => setFilter('Próximos a vencer')} style={[styles.filterButton, filter === 'Próximos a vencer' && styles.filterActive]}>
          <Text style={[styles.filterText, filter === 'Próximos a vencer' && styles.filterTextActive]}>Próximos a vencer</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={filteredExpenses}
        keyExtractor={item => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingBottom: 80 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay gastos fijos</Text>}
      />

      <TouchableOpacity style={styles.fab} onPress={() => router.push('/(app)/fixed-expenses/new')}>
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>

      <BottomNav />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeContainer: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  filterRow: { flexDirection: 'row', justifyContent: 'space-around', marginHorizontal: 16, marginVertical: 12 },
  filterButton: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 20, backgroundColor: '#eee' },
  filterActive: { backgroundColor: Colors.primary },
  filterText: { color: Colors.textSecondary },
  filterTextActive: { color: '#fff', fontWeight: 'bold' },
  card: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 16 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  title: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  status: { fontSize: 12, fontWeight: 'bold', color: Colors.primary },
  category: { fontSize: 14, color: Colors.textSecondary, marginTop: 4 },
  dueDate: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  amount: { fontSize: 18, fontWeight: 'bold', color: Colors.primary, marginTop: 8 },
  frequency: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  buttonRow: { flexDirection: 'row', marginTop: 12, justifyContent: 'flex-end' },
  payButton: { backgroundColor: Colors.success, paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20, marginRight: 8 },
  payButtonText: { color: '#fff', fontWeight: 'bold' },
  editButton: { backgroundColor: Colors.primary, paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20 },
  editButtonText: { color: '#fff', fontWeight: 'bold' },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center' },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
