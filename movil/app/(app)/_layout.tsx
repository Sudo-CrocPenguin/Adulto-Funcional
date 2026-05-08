import React from 'react';
import { Tabs } from 'expo-router';
import { useAuth } from '../../src/contexts/AuthContext';
import { Redirect } from 'expo-router';
import { ActivityIndicator, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';

export default function AppLayout() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  if (!isAuthenticated) {
    return <Redirect href="/(auth)/login" />;
  }

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: Colors.primary,
        tabBarInactiveTintColor: Colors.textSecondary,
        tabBarStyle: {
          backgroundColor: Colors.cardBackground,
          borderTopWidth: 1,
          borderTopColor: Colors.border,
          height: 60,
          paddingBottom: 8,
        },
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '500',
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Inicio',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>🏠</Text>,
        }}
      />
      <Tabs.Screen
        name="compromises"
        options={{
          title: 'Compromisos',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>📅</Text>,
        }}
      />
      <Tabs.Screen
        name="finances"
        options={{
          title: 'Finanzas',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>💰</Text>,
        }}
      />
      <Tabs.Screen
        name="fixed-expenses"
        options={{
          title: 'Gastos Fijos',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>📊</Text>,
        }}
      />
      <Tabs.Screen
        name="passwords"
        options={{
          title: 'Contraseñas',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>🔐</Text>,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Perfil',
          tabBarIcon: ({ color, size }) => <Text style={{ fontSize: size, color }}>👤</Text>,
        }}
      />
      {/* Ocultamos las pantallas que no son tabs, pero que existen en la carpeta (como categories) */}
      <Tabs.Screen
        name="categories"
        options={{
          href: null, // Oculta esta pestaña
        }}
      />
    </Tabs>
  );
}

// Necesitamos importar Text desde react-native para los iconos
import { Text } from 'react-native';
