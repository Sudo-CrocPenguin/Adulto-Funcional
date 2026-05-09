import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { router, usePathname } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
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
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingBottom: Math.max(insets.bottom, 10) }]}>
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
    justifyContent: 'space-evenly',
    backgroundColor: '#fff',
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#eee',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 5,
  },
  navItem: {
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 6,
    borderRadius: 20,
  },
  navText: {
    fontSize: 14,
    fontWeight: '500',
    color: Colors.textSecondary,
  },
  activeText: {
    color: Colors.primary,
    fontWeight: 'bold',
    fontSize: 15,
    borderBottomWidth: 2,
    borderBottomColor: Colors.primary,
    paddingBottom: 4,
  },
});
