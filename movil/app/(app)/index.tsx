import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Modal, Switch } from 'react-native';
import { useDashboard } from '../../src/hooks/useDashboard';
import { Colors } from '../../src/constants/Colors';

export default function HomeScreen() {
  const { data, loading, error } = useDashboard();
  const [showSettings, setShowSettings] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState({
    compromisos: true,
    finanzas: true,
    gastosFijos: true,
  });

  if (loading) return <View style={styles.centered}><Text>Cargando...</Text></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Notificaciones (solo en diseño #5) */}
      {data.notifications && data.notifications.length > 0 && (
        <View style={styles.notificationsCard}>
          <Text style={styles.notifTitle}>Notificaciones</Text>
          {data.notifications.map(n => (
            <View key={n.id} style={styles.notifItem}>
              <Text style={styles.notifType}>{n.type}</Text>
              <Text style={styles.notifMsg}>{n.message}</Text>
            </View>
          ))}
        </View>
      )}

      {/* Tarjetas de resumen (saldo, compromisos, gastos, contraseñas) */}
      <View style={styles.summaryGrid}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>SALDO ACTUAL</Text>
          <Text style={styles.summaryValue}>${data.balance.toFixed(2)}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>COMPROMISOS PENDIENTES</Text>
          <Text style={styles.summaryValue}>{data.pendingCommitments}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>PRÓXIMOS GASTOS</Text>
          <Text style={styles.summaryValue}>{data.upcomingExpensesCount}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryLabel}>CONTRASEÑAS</Text>
          <Text style={styles.summaryValue}>{data.passwordsCount}</Text>
        </View>
      </View>

      {/* Racha de Compromisos */}
      <View style={styles.streakCard}>
        <Text style={styles.streakTitle}>Racha de Compromisos</Text>
        <Text style={styles.streakNumber}>{data.streakDays} Días Activos</Text>
        <View style={styles.dotsRow}>
          {data.streakDots.map((day, i) => (
            <View key={i} style={styles.dotContainer}>
              <Text style={styles.dotNumber}>{day}</Text>
            </View>
          ))}
        </View>
      </View>

      {/* Gastos Fijos con botón Ver */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>GASTOS FIJOS</Text>
          <TouchableOpacity onPress={() => console.log('Ver todos gastos fijos')}>
            <Text style={styles.seeAll}>Ver</Text>
          </TouchableOpacity>
        </View>
        {data.fixedExpenses.slice(0, 2).map(item => (
          <View key={item.id} style={styles.expenseItem}>
            <Text style={styles.expenseName}>{item.name}</Text>
            <Text style={styles.expenseDate}>{item.dueDate}</Text>
            <TouchableOpacity onPress={() => console.log('Ver detalle', item.id)}>
              <Text style={styles.seeButton}>Ver</Text>
            </TouchableOpacity>
          </View>
        ))}
      </View>

      {/* Compromisos con botón Ver */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>COMPROMISOS</Text>
          <TouchableOpacity onPress={() => console.log('Ver todos compromisos')}>
            <Text style={styles.seeAll}>Ver</Text>
          </TouchableOpacity>
        </View>
        {data.commitments.slice(0, 2).map(item => (
          <View key={item.id} style={styles.commitmentItem}>
            <Text style={styles.commitmentTitle}>{item.title}</Text>
            <Text style={styles.commitmentDate}>{item.date}</Text>
            <TouchableOpacity onPress={() => console.log('Ver detalle', item.id)}>
              <Text style={styles.seeButton}>Ver</Text>
            </TouchableOpacity>
          </View>
        ))}
      </View>

      {/* Reporte estadístico */}
      <View style={styles.reportCard}>
        <Text style={styles.reportTitle}>Reporte estadístico</Text>
        <Text style={styles.reportPeriod}>Últimos 3 meses</Text>
        <View style={styles.legendRow}>
          <Text style={[styles.legendText, { color: Colors.success }]}>Ingresos</Text>
          <Text style={[styles.legendText, { color: Colors.error }]}>Egresos</Text>
          <Text style={[styles.legendText, { color: Colors.warning }]}>Osio</Text>
          <Text style={[styles.legendText, { color: Colors.primary }]}>Ahorros</Text>
        </View>
        <View style={styles.chartContainer}>
          {data.stats.monthlyData.map((value, idx) => (
            <View key={idx} style={styles.barWrapper}>
              <View style={[styles.bar, { height: Math.min(value / 35, 80) }]} />
              <Text style={styles.barLabel}>{value}</Text>
            </View>
          ))}
        </View>
        <Text style={styles.chartHint}>Ingresos  Egresos  Osio  Ahorros</Text>
      </View>

      {/* Botón de configuración (diseños #6 y #7) */}
      <TouchableOpacity style={styles.settingsButton} onPress={() => setShowSettings(!showSettings)}>
        <Text style={styles.settingsButtonText}>⚙️ Configuración</Text>
      </TouchableOpacity>

      {/* Modal de configuración (básico) */}
      <Modal visible={showSettings} animationType="slide" transparent>
        <View style={styles.modalContainer}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Configuración</Text>
            <View style={styles.settingItem}>
              <Text>Verificación en dos pasos</Text>
              <Switch value={false} onValueChange={() => {}} />
            </View>
            <View style={styles.settingItem}>
              <Text>Respaldo automático</Text>
              <Switch value={true} onValueChange={() => {}} />
            </View>
            <View style={styles.settingItem}>
              <Text>Notificaciones de Compromisos</Text>
              <Switch value={notificationsEnabled.compromisos} onValueChange={(val) => setNotificationsEnabled({...notificationsEnabled, compromisos: val})} />
            </View>
            <View style={styles.settingItem}>
              <Text>Notificaciones de Finanzas</Text>
              <Switch value={notificationsEnabled.finanzas} onValueChange={(val) => setNotificationsEnabled({...notificationsEnabled, finanzas: val})} />
            </View>
            <View style={styles.settingItem}>
              <Text>Notificaciones de Gastos Fijos</Text>
              <Switch value={notificationsEnabled.gastosFijos} onValueChange={(val) => setNotificationsEnabled({...notificationsEnabled, gastosFijos: val})} />
            </View>
            <TouchableOpacity style={styles.closeModal} onPress={() => setShowSettings(false)}>
              <Text style={styles.closeModalText}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, paddingHorizontal: 16 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  notificationsCard: { backgroundColor: '#fff', borderRadius: 20, padding: 16, marginVertical: 12 },
  notifTitle: { fontWeight: 'bold', fontSize: 16, marginBottom: 8 },
  notifItem: { marginBottom: 8 },
  notifType: { fontWeight: '600', fontSize: 14 },
  notifMsg: { fontSize: 12, color: Colors.textSecondary },
  summaryGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between', marginVertical: 12 },
  summaryCard: { backgroundColor: '#fff', borderRadius: 20, padding: 12, width: '48%', marginBottom: 12, alignItems: 'center' },
  summaryLabel: { fontSize: 12, color: Colors.textSecondary, textAlign: 'center' },
  summaryValue: { fontSize: 20, fontWeight: 'bold', color: Colors.primary, marginTop: 4 },
  streakCard: { backgroundColor: '#fff', borderRadius: 20, padding: 16, marginVertical: 12, alignItems: 'center' },
  streakTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  streakNumber: { fontSize: 28, fontWeight: 'bold', color: Colors.primary, marginVertical: 8 },
  dotsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 8 },
  dotContainer: { width: 40, alignItems: 'center' },
  dotNumber: { fontSize: 14, color: Colors.textSecondary },
  section: { marginVertical: 12 },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  sectionTitle: { fontSize: 16, fontWeight: 'bold', color: Colors.text },
  seeAll: { color: Colors.link, fontSize: 14 },
  expenseItem: { backgroundColor: '#fff', borderRadius: 16, padding: 12, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  expenseName: { fontWeight: 'bold', fontSize: 14 },
  expenseDate: { fontSize: 12, color: Colors.textSecondary },
  seeButton: { color: Colors.link, fontSize: 14 },
  commitmentItem: { backgroundColor: '#fff', borderRadius: 16, padding: 12, marginBottom: 8, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  commitmentTitle: { fontWeight: 'bold', fontSize: 14 },
  commitmentDate: { fontSize: 12, color: Colors.textSecondary },
  reportCard: { backgroundColor: '#fff', borderRadius: 20, padding: 16, marginVertical: 12, alignItems: 'center' },
  reportTitle: { fontSize: 16, fontWeight: 'bold', marginBottom: 4 },
  reportPeriod: { fontSize: 12, color: Colors.textSecondary, marginBottom: 16 },
  legendRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginBottom: 16 },
  legendText: { fontSize: 12, fontWeight: '500' },
  chartContainer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end', height: 100, marginVertical: 8 },
  barWrapper: { alignItems: 'center', width: 30 },
  bar: { width: 20, backgroundColor: Colors.primary, borderRadius: 4 },
  barLabel: { fontSize: 10, marginTop: 4 },
  chartHint: { fontSize: 10, color: Colors.textSecondary, marginTop: 8 },
  settingsButton: { backgroundColor: Colors.primary, borderRadius: 30, paddingVertical: 12, marginVertical: 16, alignItems: 'center' },
  settingsButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  modalContainer: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.5)' },
  modalContent: { backgroundColor: '#fff', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 20 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 16 },
  settingItem: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  closeModal: { backgroundColor: Colors.primary, borderRadius: 30, paddingVertical: 12, alignItems: 'center', marginTop: 16 },
  closeModalText: { color: '#fff', fontWeight: 'bold' },
});
