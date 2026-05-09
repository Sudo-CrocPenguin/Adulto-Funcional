import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Modal, Dimensions } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useDashboard } from '../../src/hooks/useDashboard';
import { Colors } from '../../src/constants/Colors';
import { useAuth } from '../../src/contexts/AuthContext';
import { BottomNav } from '../../src/components/common/BottomNav';
import { LineChart } from 'react-native-chart-kit';

const screenWidth = Dimensions.get('window').width;

export default function HomeScreen() {
  const insets = useSafeAreaInsets();
  const { data, loading, error } = useDashboard();
  const { user } = useAuth();
  const [showNotifications, setShowNotifications] = useState(false);

  if (loading) return <View style={styles.centered}><Text>Cargando...</Text></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  const chartData = {
    labels: data.chartData.labels,
    datasets: [
      { data: data.chartData.incomes, color: () => Colors.success, strokeWidth: 2 },
      { data: data.chartData.expenses, color: () => Colors.error, strokeWidth: 2 },
    ],
    legend: ['Ingresos', 'Egresos'],
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <View style={styles.headerTop}>
            <Text style={styles.welcome}>Hola, {user?.names || 'Usuario'}</Text>
            <TouchableOpacity onPress={() => router.push('/(app)/profile/settings')}>
              <Text style={styles.settingsIcon}>⚙️</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.balanceCard}>
            <Text style={styles.balanceLabel}>SALDO ACTUAL</Text>
            <Text style={styles.balanceValue}>${data.balance.toFixed(2)}</Text>
          </View>
          <View style={styles.statsRow}>
            <View style={styles.statBox}>
              <Text style={styles.statNumber}>{data.upcomingFixedExpenses.length}</Text>
              <Text style={styles.statLabel}>PRÓXIMOS GASTOS</Text>
            </View>
            <View style={styles.statBox}>
              <Text style={styles.statNumber}>{data.upcomingEvents.length}</Text>
              <Text style={styles.statLabel}>COMPROMISOS PENDIENTES</Text>
            </View>
            <View style={styles.statBox}>
              <Text style={styles.statNumber}>12</Text>
              <Text style={styles.statLabel}>CONTRASEÑAS</Text>
            </View>
          </View>
        </View>

        <TouchableOpacity style={styles.notifButton} onPress={() => setShowNotifications(true)}>
          <Text style={styles.notifText}>🔔 Notificaciones</Text>
        </TouchableOpacity>

        <View style={styles.streakCard}>
          <Text style={styles.streakTitle}>Racha de Compromisos</Text>
          <Text style={styles.streakNumber}>{data.streak} días activos</Text>
          <View style={styles.dotsRow}>
            {[7, 15, 23, 30].map(day => <Text key={day} style={styles.dot}>{day}</Text>)}
          </View>
        </View>

        <View style={styles.row}>
          <View style={styles.halfCard}>
            <Text style={styles.cardTitle}>GASTOS FIJOS</Text>
            {data.upcomingFixedExpenses.slice(0, 1).map(item => (
              <View key={item.id}>
                <Text style={styles.itemName}>{item.name}</Text>
                <Text style={styles.itemDate}>{new Date(item.nextDueDate).toLocaleDateString()}</Text>
              </View>
            ))}
            <TouchableOpacity onPress={() => router.push('/(app)/fixed-expenses')}><Text style={styles.ver}>Ver ➔</Text></TouchableOpacity>
          </View>
          <View style={styles.halfCard}>
            <Text style={styles.cardTitle}>COMPROMISOS</Text>
            {data.upcomingEvents.slice(0, 1).map(item => (
              <View key={item.id}>
                <Text style={styles.itemName}>{item.title}</Text>
                <Text style={styles.itemDate}>{new Date(item.eventDate).toLocaleDateString()}</Text>
              </View>
            ))}
            <TouchableOpacity onPress={() => router.push('/(app)/compromises')}><Text style={styles.ver}>Ver ➔</Text></TouchableOpacity>
          </View>
        </View>

        <View style={styles.reportCard}>
          <Text style={styles.reportTitle}>Reporte estadístico</Text>
          <View style={styles.legend}>
            <Text style={styles.legendIncome}>Ingresos</Text>
            <Text style={styles.legendExpense}>Egresos</Text>
            <Text style={styles.legendLeisure}>Osio</Text>
            <Text style={styles.legendSaving}>Ahorros</Text>
          </View>
          <LineChart
            data={chartData}
            width={screenWidth - 40}
            height={200}
            chartConfig={{
              backgroundColor: '#fff',
              backgroundGradientFrom: '#fff',
              backgroundGradientTo: '#fff',
              decimalPlaces: 0,
              color: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
              style: { borderRadius: 16 },
            }}
            bezier
            style={{ marginVertical: 8, borderRadius: 16 }}
          />
          <Text style={styles.reportPeriod}>Últimos 3 meses</Text>
        </View>
      </ScrollView>

      <Modal visible={showNotifications} transparent animationType="slide">
        <View style={styles.modalContainer}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Notificaciones</Text>
            <Text>• Gastos Fijos: Tienes una notificación</Text>
            <Text>• Finanzas: Tienes una notificación</Text>
            <TouchableOpacity onPress={() => setShowNotifications(false)} style={styles.closeModal}><Text>Cerrar</Text></TouchableOpacity>
          </View>
        </View>
      </Modal>

      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { backgroundColor: Colors.primary, paddingTop: 20, paddingBottom: 20, borderBottomLeftRadius: 30, borderBottomRightRadius: 30 },
  headerTop: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 20, marginBottom: 20 },
  welcome: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
  settingsIcon: { fontSize: 24, color: '#fff' },
  balanceCard: { alignItems: 'center', marginBottom: 20 },
  balanceLabel: { color: '#fff', fontSize: 14 },
  balanceValue: { color: '#fff', fontSize: 36, fontWeight: 'bold' },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around' },
  statBox: { alignItems: 'center' },
  statNumber: { color: '#fff', fontSize: 22, fontWeight: 'bold' },
  statLabel: { color: '#fff', fontSize: 12, textAlign: 'center' },
  notifButton: { backgroundColor: '#fff', marginHorizontal: 16, marginTop: 16, padding: 12, borderRadius: 20, alignItems: 'center', shadowOpacity: 0.05 },
  notifText: { fontWeight: 'bold', color: Colors.primary },
  streakCard: { backgroundColor: '#fff', margin: 16, padding: 16, borderRadius: 24, alignItems: 'center', shadowOpacity: 0.05 },
  streakTitle: { fontSize: 16, fontWeight: 'bold' },
  streakNumber: { fontSize: 28, fontWeight: 'bold', color: Colors.primary, marginVertical: 8 },
  dotsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%' },
  dot: { fontSize: 14, color: Colors.textSecondary },
  row: { flexDirection: 'row', marginHorizontal: 16, marginBottom: 16 },
  halfCard: { flex: 1, backgroundColor: '#fff', borderRadius: 24, padding: 12, marginHorizontal: 4, shadowOpacity: 0.05 },
  cardTitle: { fontWeight: 'bold', fontSize: 14, marginBottom: 8 },
  itemName: { fontSize: 14, fontWeight: '500' },
  itemDate: { fontSize: 12, color: Colors.textSecondary },
  ver: { color: Colors.link, marginTop: 8, fontSize: 12, textAlign: 'right' },
  reportCard: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 100, padding: 16, borderRadius: 24 },
  reportTitle: { fontSize: 16, fontWeight: 'bold', marginBottom: 12 },
  legend: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 16 },
  legendIncome: { color: Colors.success },
  legendExpense: { color: Colors.error },
  legendLeisure: { color: Colors.warning },
  legendSaving: { color: Colors.primary },
  reportPeriod: { textAlign: 'center', marginTop: 12, fontSize: 12, color: Colors.textSecondary },
  modalContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.5)' },
  modalContent: { backgroundColor: '#fff', padding: 20, borderRadius: 20, width: '80%' },
  modalTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 12 },
  closeModal: { marginTop: 20, alignSelf: 'center', padding: 10, backgroundColor: Colors.primary, borderRadius: 10, width: '50%', alignItems: 'center' },
});
