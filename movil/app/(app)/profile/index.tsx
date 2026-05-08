import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useProfile } from '../../../src/hooks/useProfile';
import { useAuth } from '../../../src/contexts/AuthContext';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function ProfileScreen() {
  const { profile, loading } = useProfile();
  const { user, logout } = useAuth();

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (!profile) return <View style={styles.centered}><Text>Error cargando perfil</Text></View>;

  return (
    <View style={styles.container}>
      <ScrollView>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Perfil</Text>
        </View>

        <View style={styles.statsCard}>
          <Text style={styles.sectionTitle}>MI ACTIVIDAD</Text>
          <View style={styles.statsRow}>
            <View style={styles.statItem}><Text style={styles.statNumber}>47</Text><Text style={styles.statLabel}>Compromisos completados</Text></View>
            <View style={styles.statItem}><Text style={styles.statNumber}>14</Text><Text style={styles.statLabel}>Racha máxima (días)</Text></View>
          </View>
          <View style={styles.statsRow}>
            <View style={styles.statItem}><Text style={styles.statNumber}>2</Text><Text style={styles.statLabel}>Contraseñas guardadas</Text></View>
            <View style={styles.statItem}><Text style={styles.statNumber}>8</Text><Text style={styles.statLabel}>Gastos fijos registrados</Text></View>
          </View>
        </View>

        <View style={styles.infoCard}>
          <Text style={styles.sectionTitle}>INFORMACIÓN PERSONAL</Text>
          <View style={styles.infoRow}><Text style={styles.infoLabel}>Nombre completo</Text><Text style={styles.infoValue}>{profile.names} {profile.lastnames}</Text></View>
          <View style={styles.infoRow}><Text style={styles.infoLabel}>Correo electrónico</Text><Text style={styles.infoValue}>{profile.email}</Text></View>
          <View style={styles.infoRow}><Text style={styles.infoLabel}>Teléfono</Text><Text style={styles.infoValue}>{profile.phone}</Text></View>
          <TouchableOpacity style={styles.editButton} onPress={() => router.push('/(app)/profile/edit')}><Text style={styles.editButtonText}>Editar perfil</Text></TouchableOpacity>
        </View>

        <View style={styles.accountCard}>
          <TouchableOpacity onPress={() => router.push('/(app)/profile/change-password')}><Text style={styles.accountOption}>Cambiar contraseña</Text></TouchableOpacity>
          <TouchableOpacity onPress={() => router.push('/(app)/profile/settings')}><Text style={styles.accountOption}>Configuración</Text></TouchableOpacity>
          <TouchableOpacity onPress={logout}><Text style={[styles.accountOption, styles.logout]}>Cerrar Sesión</Text></TouchableOpacity>
          <TouchableOpacity onPress={() => alert('Función no implementada')}><Text style={[styles.accountOption, styles.delete]}>Eliminar Cuenta</Text></TouchableOpacity>
        </View>
      </ScrollView>
      <BottomNav />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { backgroundColor: Colors.primary, paddingTop: 60, paddingBottom: 20, alignItems: 'center' },
  headerTitle: { color: '#fff', fontSize: 28, fontWeight: 'bold' },
  statsCard: { backgroundColor: '#fff', margin: 16, padding: 16, borderRadius: 24 },
  sectionTitle: { fontSize: 16, fontWeight: 'bold', marginBottom: 12 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 16 },
  statItem: { alignItems: 'center' },
  statNumber: { fontSize: 24, fontWeight: 'bold', color: Colors.primary },
  statLabel: { fontSize: 12, color: Colors.textSecondary },
  infoCard: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 16, padding: 16, borderRadius: 24 },
  infoRow: { marginBottom: 12 },
  infoLabel: { fontSize: 14, color: Colors.textSecondary },
  infoValue: { fontSize: 16, fontWeight: '500', color: Colors.text },
  editButton: { backgroundColor: Colors.primary, paddingVertical: 10, borderRadius: 20, alignItems: 'center', marginTop: 8 },
  editButtonText: { color: '#fff', fontWeight: 'bold' },
  accountCard: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 30, borderRadius: 24, overflow: 'hidden' },
  accountOption: { padding: 16, fontSize: 16, borderBottomWidth: 1, borderBottomColor: '#eee', color: Colors.text },
  logout: { color: Colors.error },
  delete: { color: Colors.error, borderBottomWidth: 0 },
});
