import React, { useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { usePasswords } from '../../../src/hooks/usePasswords';
import { Colors } from '../../../src/constants/Colors';
import { BottomNav } from '../../../src/components/common/BottomNav';

export default function PasswordsScreen() {
  const { passwords, loading, error, masterKeyVerified, hasMasterKey, fetchPasswords } = usePasswords();

  useEffect(() => {
    const check = async () => {
      const has = await hasMasterKey();
      if (has && !masterKeyVerified) {
        router.push('/(app)/passwords/master-key/verify');
      } else {
        fetchPasswords();
      }
    };
    check();
  }, []);

  if (loading) return <View style={styles.centered}><ActivityIndicator size="large" color={Colors.primary} /></View>;
  if (error) return <View style={styles.centered}><Text>Error: {error}</Text></View>;

  const renderItem = ({ item }) => (
    <TouchableOpacity style={styles.card} onPress={() => router.push(`/(app)/passwords/${item.id}`)}>
      <Text style={styles.title}>{item.name}</Text>
      <Text style={styles.username}>{item.username || 'Sin usuario'}</Text>
      <Text style={styles.date}>{new Date(item.lastChangeDate).toLocaleDateString()}</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  card: { backgroundColor: '#fff', marginHorizontal: 16, marginBottom: 12, padding: 16, borderRadius: 16 },
  title: { fontSize: 18, fontWeight: 'bold', color: Colors.text },
  username: { fontSize: 14, color: Colors.textSecondary, marginTop: 4 },
  date: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  empty: { textAlign: 'center', marginTop: 50, color: Colors.textSecondary },
  fab: { position: 'absolute', bottom: 80, right: 20, backgroundColor: Colors.primary, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center' },
  fabText: { fontSize: 32, color: '#fff', lineHeight: 36 },
});
