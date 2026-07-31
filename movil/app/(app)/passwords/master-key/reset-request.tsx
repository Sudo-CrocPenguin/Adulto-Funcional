import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { Colors } from '../../../../src/constants/Colors';

export default function ResetMasterKeyRequestScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Cambiar Clave Maestra</Text>
      <Text style={styles.text}>Para proteger tus contraseñas, el cambio requiere tu clave maestra actual.</Text>
      <Text style={styles.note}>Si la olvidaste, no es posible recuperar las contraseñas cifradas.</Text>
      <TouchableOpacity style={styles.button} onPress={() => router.push('/(app)/passwords/master-key/reset-new')}>
        <Text style={styles.buttonText}>Continuar</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: Colors.background, justifyContent: 'center' },
  title: { fontSize: 28, fontWeight: 'bold', marginBottom: 20, textAlign: 'center' },
  text: { fontSize: 16, textAlign: 'center', marginBottom: 20 },
  note: { fontSize: 14, textAlign: 'center', marginBottom: 30, color: Colors.textSecondary },
  button: { backgroundColor: Colors.primary, paddingVertical: 14, borderRadius: 30, alignItems: 'center' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
});
