import React, { useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Switch, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { Colors } from '../../../src/constants/Colors';

export default function SettingsScreen() {
  const [notifications, setNotifications] = useState({
    compromisos: true,
    finanzas: true,
    gastosFijos: true,
  });
  const [darkMode, setDarkMode] = useState(false);
  const [twoFactor, setTwoFactor] = useState(false);
  const [autoBackup, setAutoBackup] = useState(false);

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Configuración</Text>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>General</Text>
        <TouchableOpacity style={styles.option} onPress={() => router.push('/(app)/profile/edit')}>
          <Text style={styles.optionText}>Nombre de Usuario</Text>
          <Text style={styles.optionValue}>Usuario</Text>
        </TouchableOpacity>
        <View style={styles.option}>
          <Text style={styles.optionText}>Idioma</Text>
          <Text style={styles.optionValue}>Español ✓</Text>
        </View>
        <Text style={styles.subsectionTitle}>Notificaciones</Text>
        <View style={styles.switchRow}>
          <Text style={styles.optionText}>Compromisos</Text>
          <Switch value={notifications.compromisos} onValueChange={(val) => setNotifications({ ...notifications, compromisos: val })} />
        </View>
        <View style={styles.switchRow}>
          <Text style={styles.optionText}>Finanzas</Text>
          <Switch value={notifications.finanzas} onValueChange={(val) => setNotifications({ ...notifications, finanzas: val })} />
        </View>
        <View style={styles.switchRow}>
          <Text style={styles.optionText}>Gastos Fijos</Text>
          <Switch value={notifications.gastosFijos} onValueChange={(val) => setNotifications({ ...notifications, gastosFijos: val })} />
        </View>
        <Text style={styles.subsectionTitle}>Modo</Text>
        <View style={styles.modeRow}>
          <TouchableOpacity style={[styles.modeButton, !darkMode && styles.modeActive]} onPress={() => setDarkMode(false)}><Text>Claro</Text></TouchableOpacity>
          <TouchableOpacity style={[styles.modeButton, darkMode && styles.modeActive]} onPress={() => setDarkMode(true)}><Text>Oscuro</Text></TouchableOpacity>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Seguridad</Text>
        <View style={styles.switchRow}>
          <Text style={styles.optionText}>Verificación en dos pasos</Text>
          <Switch value={twoFactor} onValueChange={setTwoFactor} />
        </View>
        <View style={styles.switchRow}>
          <Text style={styles.optionText}>Respaldo automático</Text>
          <Switch value={autoBackup} onValueChange={setAutoBackup} />
        </View>
        <TouchableOpacity style={styles.option}>
          <Text style={styles.optionText}>Diario</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.option}>
          <Text style={styles.optionText}>Inicio de Sesión</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.dangerButton}>
          <Text style={styles.dangerText}>Activar</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
        <Text style={styles.backText}>Volver</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  section: { backgroundColor: '#fff', borderRadius: 24, padding: 16, marginBottom: 20 },
  sectionTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 12, color: Colors.text },
  subsectionTitle: { fontSize: 16, fontWeight: '600', marginTop: 12, marginBottom: 8, color: Colors.text },
  option: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#eee' },
  optionText: { fontSize: 16, color: Colors.text },
  optionValue: { fontSize: 14, color: Colors.textSecondary },
  switchRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#eee' },
  modeRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 },
  modeButton: { flex: 1, paddingVertical: 10, marginHorizontal: 4, borderRadius: 20, backgroundColor: '#eee', alignItems: 'center' },
  modeActive: { backgroundColor: Colors.primary, borderWidth: 0 },
  dangerButton: { backgroundColor: Colors.error, paddingVertical: 12, borderRadius: 30, alignItems: 'center', marginTop: 16 },
  dangerText: { color: '#fff', fontWeight: 'bold' },
  backButton: { alignItems: 'center', marginVertical: 20 },
  backText: { color: Colors.link, fontSize: 16 },
});
