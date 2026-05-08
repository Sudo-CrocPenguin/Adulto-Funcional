import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { router, usePathname } from 'expo-router';
import { Colors } from '../../constants/Colors';

const navItems = [
  { name: 'Inicio', path: '/(app)' },
  { name: 'Compromisos', path: '/(app)/compromises' },
  { name: 'Finanzas', path: '/(app)/finances' },
  { name: 'Gastos Fijos', path: '/(app)/fixed-expenses' },
  { name: 'Contraseñas', path: '/(app)/passwords' },
  { name: 'Perfil', path: '/(app)/profile' },
];

export const BottomNav = () => {
  const pathname = usePathname();

  return (
    <View style={styles.container}>
      {navItems.map((item) => (
        <TouchableOpacity
          key={item.path}
          style={styles.navItem}
          onPress={() => router.push(item.path)}
        >
          <Text style={[styles.navText, pathname === item.path && styles.activeText]}>
            {item.name}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#fff',
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: '#eee',
  },
  navItem: { alignItems: 'center' },
  navText: { fontSize: 12, color: Colors.textSecondary },
  activeText: { color: Colors.primary, fontWeight: 'bold' },
});
