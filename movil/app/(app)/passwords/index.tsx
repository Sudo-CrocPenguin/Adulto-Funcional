import React, { useState, useCallback } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, FlatList, ActivityIndicator, Alert, SafeAreaView } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { securityApi } from '../../../src/api/securityApi';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function PasswordsScreen() {
  const insets = useSafeAreaInsets();
  const {
    passwords,
    loading,
    error,
    verifying,
    hasMasterKey,
    masterKeyVerified,
    verifyMasterKey,
    fetchPasswords,
    deletePassword,
  } = usePasswords();

  const [verificationKey, setVerificationKey] = useState('');
  const [visiblePasswords, setVisiblePasswords] = useState<Record<string, string>>({});
  const [loadingPasswords, setLoadingPasswords] = useState<Record<string, boolean>>({});

  // Si no hay clave maestra (caso extremo, pero todos deberían tenerla)
  if (!hasMasterKey) {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.verifyContainer}>
          <Text style={styles.verifyTitle}>Clave maestra no configurada</Text>
          <Text style={styles.verifySubtitle}>No se puede acceder al gestor de contraseñas.</Text>
        </View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  // Si tiene clave pero no está verificada, mostrar formulario de verificación
  if (!masterKeyVerified) {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.verifyContainer}>
          <Text style={styles.verifyTitle}>Verificar clave maestra</Text>
          <Text style={styles.verifySubtitle}>Ingresa tu clave maestra para acceder a tus contraseñas</Text>
          {error && <Text style={styles.errorText}>{error}</Text>}
          <TextInput
            style={styles.verifyInput}
            secureTextEntry
            value={verificationKey}
            onChangeText={setVerificationKey}
            placeholder="Clave maestra"
            editable={!verifying}
          />
          <TouchableOpacity
            style={[styles.verifyButton, verifying && styles.buttonDisabled]}
            onPress={async () => {
              if (!verificationKey.trim()) {
                Alert.alert('Error', 'Ingrese la clave maestra');
                return;
              }
              const success = await verifyMasterKey(verificationKey);
              if (success) {
                setVerificationKey('');
                await fetchPasswords();
              }
            }}
            disabled={verifying}
          >
            {verifying ? <ActivityIndicator color="#fff" /> : <Text style={styles.verifyButtonText}>Verificar</Text>}
          </TouchableOpacity>
        </View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  // Si está verificado y cargando
  if (loading) {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  // Si hay error en la carga (después de verificado)
  if (error) {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.centered}><Text style={styles.errorText}>Error: {error}</Text></View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  const handleTogglePassword = async (id: string) => {
    // Si ya tenemos la contraseña visible, la ocultamos
    if (visiblePasswords[id]) {
      setVisiblePasswords(prev => {
        const newState = { ...prev };
        delete newState[id];
        return newState;
      });
      return;
    }

    // Si no la tenemos, la solicitamos al backend
    setLoadingPasswords(prev => ({ ...prev, [id]: true }));
    try {
      const response = await securityApi.getPassword(id);
      const decryptedPassword = response.data.data.password || '';
      setVisiblePasswords(prev => ({ ...prev, [id]: decryptedPassword }));
    } catch (err: any) {
      Alert.alert('Error', 'No se pudo obtener la contraseña');
    } finally {
      setLoadingPasswords(prev => ({ ...prev, [id]: false }));
    }
  };

  const renderItem = ({ item }) => {
    const isVisible = !!visiblePasswords[item.id];
    const isLoadingPassword = loadingPasswords[item.id];
    return (
      <View style={styles.card}>
        <Text style={styles.title}>{item.applicationName}</Text>
        <View style={styles.passwordRow}>
          <Text style={styles.passwordLabel}>Contraseña:</Text>
          <Text style={styles.passwordValue}>
            {isVisible ? visiblePasswords[item.id] : '********'}
          </Text>
          <TouchableOpacity onPress={() => handleTogglePassword(item.id)} disabled={isLoadingPassword}>
            {isLoadingPassword ? (
              <ActivityIndicator size="small" color={Colors.primary} />
            ) : (
              <Text style={styles.eyeIcon}>{isVisible ? '👁️' : '👁️‍🗨️'}</Text>
            )}
          </TouchableOpacity>
        </View>
        <Text style={styles.date}>Último cambio: {new Date(item.lastChangeDate).toLocaleDateString()}</Text>
        <View style={styles.row}>
          <TouchableOpacity onPress={() => router.push(`/(app)/passwords/${item.id}`)}>
            <Text style={styles.edit}>✏️ Editar</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={() => Alert.alert('Eliminar', '¿Eliminar esta contraseña?', [
            { text: 'Cancelar', style: 'cancel' },
            { text: 'Eliminar', style: 'destructive', onPress: () => deletePassword(item.id) }
          ])}>
            <Text style={styles.delete}>🗑️ Eliminar</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
      <FlatList
        data={passwords}
        keyExtractor={item => item.id}
        renderItem={renderItem}
        contentContainerStyle={{ paddingBottom: 80 }}
        ListEmptyComponent={<Text style={styles.empty}>No hay contraseñas guardadas</Text>}
      />
      <TouchableOpacity style={styles.fab} onPress={() => router.push('/(app)/passwords/new')}>
        <Text style={styles.fabText}>+</Text>
      </TouchableOpacity>
      <BottomNav />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeContainer: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  verifyContainer: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  verifyTitle: { fontSize: 24, fontWeight: 'bold', marginBottom: 12, textAlign: 'center', color: Colors.text },
  verifySubtitle: { fontSize: 16, color: Colors.textSecondary, textAlign: 'center', marginBottom: 20 },
  verifyInput: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, width: '100%', marginBottom: 16, backgroundColor: '#fff' },
  verifyButton: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', width: '100%' },
  buttonDisabled: { backgroundColor: Colors.textSecondary, opacity: 0.6 },
  verifyButtonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  errorText: { color: Colors.error, marginBottom: 12, textAlign: 'center' },
  card: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 16 },
  title: { fontSize: 18, fontWeight: 'bold', color: Colors.text, marginBottom: 8 },
  passwordRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
  passwordLabel: { fontSize: 14, color: Colors.textSecondary, marginRight: 8 },
  passwordValue: { flex: 1, fontSize: 14, color: Colors.text },
  eyeIcon: { fontSize: 18, marginLeft: 8 },
  date: { fontSize: 12, color: Colors.textSecondary, marginBottom: 8 },
  row: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 4 },
  edit: { color: Colors.link, marginRight: 16 },
  delete: { color: Colors.error },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 5 },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
