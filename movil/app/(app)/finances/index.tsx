import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useMovements } from '../../../src/hooks/useMovements';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';
import { formatCurrency, formatCurrencyParts } from '../../../src/utils/currencyUtils';
import type { Movement } from '../../../src/api/financesApi';

export default function FinancesScreen() {
  const insets = useSafeAreaInsets();
  const { movements, loading, error, fetchMovements } = useMovements();
  const [filter, setFilter] = useState<'todos' | 'ingresos' | 'egresos'>('todos');

  useFocusEffect(
    useCallback(() => {
      fetchMovements();
    }, [fetchMovements])
  );

  const totalIngresos = movements.filter(m => m.movementType === 'INCOME').reduce((sum, m) => sum + m.amount, 0);
  const totalEgresos = movements.filter(m => m.movementType === 'EXPENSE').reduce((sum, m) => sum + m.amount, 0);
  const balance = totalIngresos - totalEgresos;

  const filteredMovements = movements.filter(m => {
    if (filter === 'ingresos') return m.movementType === 'INCOME';
    if (filter === 'egresos') return m.movementType === 'EXPENSE';
    return true;
  });

  const renderItem = ({ item }: { item: Movement }) => {
    const parts = formatCurrencyParts(item.amount);
    return (
      <View style={styles.movementCard}>
        <View>
          <Text style={styles.movementTitle}>{item.description || 'Movimiento'}</Text>
          <Text style={styles.movementCategory}>{item.category?.name || 'Sin categoría'}</Text>
          <Text style={styles.movementDate}>{new Date(item.movementDate).toLocaleDateString()}</Text>
        </View>
        <Text style={item.movementType === 'INCOME' ? styles.income : styles.expense}>
          {item.movementType === 'INCOME' ? '+' : '-'}
          <Text style={{ fontSize: 16 }}>{parts.integer}</Text>
          <Text style={{ fontSize: 12 }}>.{parts.decimal}</Text>
        </Text>
      </View>
    );
  };

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <View style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>TOTAL INGRESOS</Text>
          <Text style={styles.summaryIncome}>{formatCurrency(totalIngresos)}</Text>
          <Text style={styles.summaryLabel}>TOTAL EGRESOS</Text>
          <Text style={styles.summaryExpense}>{formatCurrency(totalEgresos)}</Text>
          <View style={styles.balanceRow}>
            <Text style={styles.balanceLabel}>SALDO ACTUAL</Text>
            <Text style={styles.balanceValue}>{formatCurrency(balance)}</Text>
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
          scrollEnabled={false}
          ListEmptyComponent={<Text style={styles.empty}>No hay movimientos</Text>}
        />
      </ScrollView>

      <TouchableOpacity style={styles.fab} onPress={() => router.push('/(app)/finances/new')}>
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>

      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  safeContainer: { flex: 1, backgroundColor: Colors.background },
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
