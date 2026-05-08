import React, { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useFixedExpenses } from '../../../src/hooks/useFixedExpenses';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

type FilterType = 'Todos' | 'Próximos a vencer';

export default function FixedExpensesScreen() {
  const { expenses, loading, error } = useFixedExpenses();
  const [filter, setFilter] = useState<FilterType>('Todos');

  const filteredExpenses = filter === 'Próximos a vencer'
    ? expenses.filter(e => new Date(e.nextDueDate) >= new Date())
    : expenses;

  const getDaysLeft = (dateStr: string) => {
    const days = Math.ceil((new Date(dateStr).getTime() - new Date().getTime()) / (1000 * 3600 * 24));
    return days > 0 ? `${days} días` : 'hoy';
  };

  const renderItem = ({ item }) => (
    <TouchableOpacity style={styles.card} onPress={() => router.push(`/(app)/fixed-expenses/${item.id}`)}>
      <View style={styles.cardHeader}>
        <Text style={styles.title}>{item.name}</Text>
        <Text style={styles.status}>{item.status}</Text>
      </View>
      <Text style={styles.category}>{item.category}</Text>
      <Text style={styles.dueDate}>
        Próximo pago: {new Date(item.nextDueDate).toLocaleDateString()} ({getDaysLeft(item.nextDueDate)})
      </Text>
      <Text style={styles.amount}>${item.amount.toFixed(2)}</Text>
      <Text style={styles.frequency}>{item.frequency}</Text>
    </TouchableOpacity>
  );

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <View style={styles.container}>
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
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
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center' },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
