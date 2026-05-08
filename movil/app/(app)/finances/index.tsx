import React, { useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useMovements } from '../../../src/hooks/useMovements';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function FinancesScreen() {
  const { movements, loading, error } = useMovements();
  const [filter, setFilter] = useState<'todos' | 'ingresos' | 'egresos'>('todos');

  const totalIngresos = movements.filter(m => m.movementType === 'INCOME').reduce((sum, m) => sum + m.amount, 0);
  const totalEgresos = movements.filter(m => m.movementType === 'EXPENSE').reduce((sum, m) => sum + m.amount, 0);
  const balance = totalIngresos - totalEgresos;

  const filteredMovements = movements.filter(m => {
    if (filter === 'ingresos') return m.movementType === 'INCOME';
    if (filter === 'egresos') return m.movementType === 'EXPENSE';
    return true;
  });

  const renderItem = ({ item }) => (
    <View style={styles.movementCard}>
      <View>
        <Text style={styles.movementTitle}>{item.description || 'Movimiento'}</Text>
        <Text style={styles.movementCategory}>{item.category || 'Sin categoría'}</Text>
        <Text style={styles.movementDate}>{new Date(item.movementDate).toLocaleDateString()}</Text>
      </View>
      <Text style={item.movementType === 'INCOME' ? styles.income : styles.expense}>
        {item.movementType === 'INCOME' ? '+' : '-'}${item.amount.toFixed(2)}
      </Text>
    </View>
  );

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <View style={styles.container}>
      <View style={styles.summaryCard}>
        <Text style={styles.summaryLabel}>TOTAL INGRESOS</Text>
        <Text style={styles.summaryIncome}>${totalIngresos.toFixed(2)}</Text>
        <Text style={styles.summaryLabel}>TOTAL EGRESOS</Text>
        <Text style={styles.summaryExpense}>${totalEgresos.toFixed(2)}</Text>
        <View style={styles.balanceRow}>
          <Text style={styles.balanceLabel}>SALDO ACTUAL</Text>
          <Text style={styles.balanceValue}>${balance.toFixed(2)}</Text>
        </View>
      </View>

      <View style={styles.filterRow}>
        {(['todos', 'ingresos', 'egresos'] as const).map(f => (
          <TouchableOpacity key={f} onPress={() => setFilter(f)} style={[styles.filterButton, filter === f && styles.filterActive]}>
            <Text style={[styles.filterText, filter === f && styles.filterTextActive]}>
              {f === 'todos' ? 'Todos' : f === 'ingresos' ? 'Ingresos' : 'Egresos'}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <FlatList
        data={filteredMovements}
        keyExtractor={item => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingBottom: 80 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay movimientos</Text>}
      />

      <TouchableOpacity style={styles.fab} onPress={() => router.push('/(app)/finances/new')}>
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>

      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  summaryCard: { backgroundColor: '#fff', margin: 16, padding: 16, borderRadius: 24, alignItems: 'center' },
  summaryLabel: { fontSize: 14, color: Colors.textSecondary, marginTop: 8 },
  summaryIncome: { fontSize: 24, fontWeight: 'bold', color: Colors.success },
  summaryExpense: { fontSize: 24, fontWeight: 'bold', color: Colors.error },
  balanceRow: { flexDirection: 'row', justifyContent: 'space-between', width: '100%', marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#eee' },
  balanceLabel: { fontSize: 16, fontWeight: 'bold' },
  balanceValue: { fontSize: 18, fontWeight: 'bold', color: Colors.primary },
  filterRow: { flexDirection: 'row', justifyContent: 'space-around', marginHorizontal: 16, marginBottom: 12 },
  filterButton: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 20, backgroundColor: '#eee' },
  filterActive: { backgroundColor: Colors.primary },
  filterText: { color: Colors.textSecondary },
  filterTextActive: { color: '#fff', fontWeight: 'bold' },
  movementCard: { flexDirection: 'row', justifyContent: 'space-between', backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 16 },
  movementTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  movementCategory: { fontSize: 12, color: Colors.textSecondary },
  movementDate: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  income: { fontSize: 16, fontWeight: 'bold', color: Colors.success },
  expense: { fontSize: 16, fontWeight: 'bold', color: Colors.error },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 5 },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
