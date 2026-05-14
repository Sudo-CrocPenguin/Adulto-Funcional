import React from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';
import { router, useSegments } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors } from '../../constants/Colors';

const navItems = [
  { name: 'Inicio', path: '/', icon: 'home' },
  { name: 'Compromisos', path: '/compromises', icon: 'calendar-check' },
  { name: 'Finanzas', path: '/finances', icon: 'currency-usd' },
  { name: 'Gastos Fijos', path: '/fixed-expenses', icon: 'cash' },
  { name: 'Contraseñas', path: '/passwords', icon: 'lock' },
  { name: 'Perfil', path: '/profile', icon: 'account' },
];

export const BottomNav = () => {
  const segments = useSegments();
  // Obtener la ruta actual: puede ser ['(app)', 'compromises'] -> '/compromises'
  const currentPath = segments.length > 1 ? `/${segments[segments.length - 1]}` : '/';

  return (
    <View style={styles.container}>
      {navItems.map((item) => {
        const isActive = currentPath === item.path;
        return (
          <TouchableOpacity
            key={item.path}
            style={styles.navItem}
            onPress={() => router.push(item.path)}
          >
            <MaterialCommunityIcons
              name={item.icon}
              size={28}
              color={isActive ? Colors.primary : Colors.textSecondary}
            />
          </TouchableOpacity>
        );
      })}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    backgroundColor: '#fff',
    paddingVertical: 10,
    paddingBottom: 12,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 5,
  },
  navItem: {
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 30,
  },
});
