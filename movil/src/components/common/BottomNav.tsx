import React, { type ComponentProps } from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';
import { router, useSegments, type Href } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors } from '../../constants/Colors';

type IconName = ComponentProps<typeof MaterialCommunityIcons>['name'];

const navItems: Array<{ name: string; segment: string; path: Href; icon: IconName }> = [
  { name: 'Inicio', segment: '', path: '/(app)', icon: 'home' },
  { name: 'Compromisos', segment: 'compromises', path: '/(app)/compromises', icon: 'calendar-check' },
  { name: 'Finanzas', segment: 'finances', path: '/(app)/finances', icon: 'currency-usd' },
  { name: 'Gastos Fijos', segment: 'fixed-expenses', path: '/(app)/fixed-expenses', icon: 'cash' },
  { name: 'Contraseñas', segment: 'passwords', path: '/(app)/passwords', icon: 'lock' },
  { name: 'Perfil', segment: 'profile', path: '/(app)/profile', icon: 'account' },
];

export const BottomNav = () => {
  const segments = useSegments();
  const currentSegment = segments.length > 1 ? String(segments[1]) : '';

  return (
    <View style={styles.container}>
      {navItems.map((item) => {
        const isActive = currentSegment === item.segment;
        return (
          <TouchableOpacity
            key={item.name}
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
