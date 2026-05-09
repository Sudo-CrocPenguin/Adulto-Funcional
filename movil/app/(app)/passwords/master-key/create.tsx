import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { router } from 'expo-router';
import { Colors } from '../../../../src/constants/Colors';

export default function CreateMasterKeyScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Clave Maestra</Text>
      <Text style={styles.message}>
        La clave maestra se configura en el registro de usuario o en la sección de perfil.
        Por favor, ve a tu perfil para establecer una clave maestra.
      </Text>
      <TouchableOpacity style={styles.button} onPress={() => router.push('/(app)/profile')}>
        <Text style={styles.buttonText}>Ir a Perfil</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20, backgroundColor: Colors.background },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 20 },
  message: { textAlign: 'center', marginBottom: 30, color: Colors.textSecondary },
  button: { backgroundColor: Colors.primary, paddingHorizontal: 20, paddingVertical: 12, borderRadius: 30 },
  buttonText: { color: '#fff', fontWeight: 'bold' },
});
