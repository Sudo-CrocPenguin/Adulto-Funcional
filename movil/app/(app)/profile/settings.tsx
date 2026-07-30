import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TouchableOpacity, Switch, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';
import { useAuth } from '../../../src/contexts/AuthContext';

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const { logout, user } = useAuth();
  const [darkMode, setDarkMode] = useState(false);
  const [notifications, setNotifications] = useState({
    compromisos: true,
    finanzas: true,
    gastosFijos: true,
  });
  const [twoFactor, setTwoFactor] = useState(false);
  const [autoBackup, setAutoBackup] = useState(false);

  // Cargar preferencias guardadas
  useEffect(() => {
    const loadPreferences = async () => {
      const savedTheme = await AsyncStorage.getItem('theme');
      if (savedTheme === 'dark') setDarkMode(true);
      const savedNotif = await AsyncStorage.getItem('notifications');
      if (savedNotif) setNotifications(JSON.parse(savedNotif));
    };
    loadPreferences();
  }, []);

  // Guardar modo oscuro
  const toggleDarkMode = async (value: boolean) => {
    setDarkMode(value);
    await AsyncStorage.setItem('theme', value ? 'dark' : 'light');
    // Aquí podrías emitir un evento global para cambiar el tema de toda la app
  };

  // Guardar estado de notificaciones
  const toggleNotification = async (key: keyof typeof notifications) => {
    const updated = { ...notifications, [key]: !notifications[key] };
    setNotifications(updated);
    await AsyncStorage.setItem('notifications', JSON.stringify(updated));
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top }]}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <Text style={styles.title}>Configuración</Text>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>General</Text>
          <TouchableOpacity style={styles.option} onPress={() => router.push('/(app)/profile/edit')}>
            <Text style={styles.optionText}>Editar perfil</Text>
            <Text style={styles.optionValue}>›</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.option} onPress={() => router.push('/(app)/profile/change-password')}>
            <Text style={styles.optionText}>Cambiar contraseña</Text>
            <Text style={styles.optionValue}>›</Text>
          </TouchableOpacity>
          <View style={styles.option}>
            <Text style={styles.optionText}>Idioma</Text>
            <Text style={styles.optionValue}>Español ✓</Text>
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Modo oscuro</Text>
            <Switch value={darkMode} onValueChange={toggleDarkMode} />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Notificaciones</Text>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Compromisos</Text>
            <Switch value={notifications.compromisos} onValueChange={() => toggleNotification('compromisos')} />
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Finanzas</Text>
            <Switch value={notifications.finanzas} onValueChange={() => toggleNotification('finanzas')} />
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Gastos Fijos</Text>
            <Switch value={notifications.gastosFijos} onValueChange={() => toggleNotification('gastosFijos')} />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Seguridad</Text>
          <TouchableOpacity
            style={styles.option}
            onPress={() => router.push(user?.hasMasterKey ? '/(app)/passwords/master-key/reset-request' : '/(app)/passwords/master-key/create')}
          >
            <Text style={styles.optionText}>{user?.hasMasterKey ? 'Cambiar clave maestra' : 'Crear clave maestra'}</Text>
            <Text style={styles.optionValue}>›</Text>
          </TouchableOpacity>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Verificación en dos pasos</Text>
            <Switch value={twoFactor} onValueChange={setTwoFactor} />
          </View>
          <View style={styles.switchRow}>
            <Text style={styles.optionText}>Respaldo automático</Text>
            <Switch value={autoBackup} onValueChange={setAutoBackup} />
          </View>
        </View>

        <TouchableOpacity style={styles.dangerButton} onPress={logout}>
          <Text style={styles.dangerText}>Cerrar sesión</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.dangerButton} onPress={() => router.push('/(app)/profile/delete-account')}>
          <Text style={styles.dangerText}>Eliminar cuenta</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <Text style={styles.backText}>Volver</Text>
        </TouchableOpacity>
      </ScrollView>
      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, paddingHorizontal: 20 },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 24, textAlign: 'center', marginTop: 20 },
  section: { backgroundColor: Colors.cardBackground, borderRadius: 24, padding: 16, marginBottom: 20 },
  sectionTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 12, color: Colors.text },
  option: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: Colors.border },
  optionText: { fontSize: 16, color: Colors.text },
  optionValue: { fontSize: 14, color: Colors.textSecondary },
  switchRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: Colors.border },
  dangerButton: { backgroundColor: Colors.error, paddingVertical: 14, borderRadius: 30, alignItems: 'center', marginTop: 16 },
  dangerText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  backButton: { alignItems: 'center', marginVertical: 20 },
  backText: { color: Colors.link, fontSize: 16 },
});
