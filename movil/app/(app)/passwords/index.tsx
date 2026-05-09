import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, FlatList, ActivityIndicator, Alert, SafeAreaView } from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function PasswordsScreen() {
  const insets = useSafeAreaInsets();
  const {
    passwords,
    loading,
    error,
    hasMasterKey,
    masterKeyVerified,
    verifyMasterKey,
    fetchPasswords,
    deletePassword,
  } = usePasswords();

  const [verificationKey, setVerificationKey] = useState('');
  const [mode, setMode] = useState<'checking' | 'verify' | 'list'>('checking');

  useEffect(() => {
    if (hasMasterKey === false) {
      Alert.alert(
        'Clave maestra no configurada',
        'Para usar el gestor de contraseñas, primero debes configurar una clave maestra en tu perfil.',
        [{ text: 'Ir a Perfil', onPress: () => router.push('/(app)/profile') }]
      );
      setMode('checking');
    } else if (hasMasterKey === true && !masterKeyVerified) {
      setMode('verify');
    } else if (hasMasterKey === true && masterKeyVerified) {
      setMode('list');
    }
  }, [hasMasterKey, masterKeyVerified]);

  const handleVerifyMasterKey = async () => {
    if (!verificationKey) {
      Alert.alert('Error', 'Ingrese la clave maestra');
      return;
    }
    try {
      await verifyMasterKey(verificationKey);
      setVerificationKey('');
      setMode('list');
      await fetchPasswords();
    } catch (err: any) {
      Alert.alert('Error', err.message);
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      if (mode === 'list') fetchPasswords();
    }, [mode, fetchPasswords])
  );

  const renderItem = ({ item }) => (
    <TouchableOpacity style={styles.card} onPress={() => router.push(`/(app)/passwords/${item.id}`)}>
      <Text style={styles.title}>{item.applicationName}</Text>
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
    </TouchableOpacity>
  );

  if (mode === 'checking' || (mode === 'list' && loading)) {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>
        <BottomNav />
      </SafeAreaView>
    );
  }

  if (mode === 'verify') {
    return (
      <SafeAreaView style={[styles.safeContainer, { paddingTop: insets.top }]}>
        <View style={styles.container}>
          <Text style={styles.title}>Verificar clave maestra</Text>
          <Text style={styles.subtitle}>Ingresa tu clave maestra para acceder a tus contraseñas</Text>
          <TextInput
            style={styles.input}
            secureTextEntry
            value={verificationKey}
            onChangeText={setVerificationKey}
            placeholder="Clave maestra"
          />
          <TouchableOpacity style={styles.button} onPress={handleVerifyMasterKey}>
            <Text style={styles.buttonText}>Verificar</Text>
          </TouchableOpacity>
        </View>
        <BottomNav />
      </SafeAreaView>
    );
  }

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
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  subtitle: { fontSize: 16, color: Colors.textSecondary, textAlign: 'center', marginBottom: 30 },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, width: '100%', marginBottom: 16, backgroundColor: '#fff' },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center', width: '100%' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  card: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 16 },
  title: { fontSize: 18, fontWeight: 'bold', color: Colors.text },
  date: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  row: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 12 },
  edit: { color: Colors.link, marginRight: 16 },
  delete: { color: Colors.error },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 5 },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
