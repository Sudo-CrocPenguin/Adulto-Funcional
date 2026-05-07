/**
 * @file _layout.tsx
 * @description Layout protegido del segmento principal de la aplicación Adulto Funcional.
 *              Actúa como guardián de rutas (route guard): verifica el estado de
 *              autenticación antes de permitir el acceso a cualquier pantalla del
 *              segmento `(app)`, redirigiendo al login si no hay sesión activa.
 *
 * @module app/(app)/_layout
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Este layout implementa tres estados de renderizado:
 * 1. **Cargando**: mientras `isLoading` es `true`, muestra un indicador de actividad
 *    centrado para evitar un parpadeo o redirección prematura.
 * 2. **No autenticado**: si `isAuthenticated` es `false`, redirige automáticamente
 *    a `/(auth)/login` usando el componente {@link Redirect} de expo-router.
 * 3. **Autenticado**: renderiza el navegador Stack con todas las pantallas protegidas.
 *
 * Pantallas registradas en el Stack:
 * - `index`          → Pantalla principal de bienvenida.
 * - `compromises`    → Gestión de compromisos del usuario.
 * - `finances`       → Gestión de finanzas personales.
 * - `fixed-expenses` → Registro de gastos fijos recurrentes.
 * - `passwords`      → Gestión segura de contraseñas.
 * - `profile`        → Perfil y configuración del usuario.
 * - `categories`     → Administración de categorías.
 *
 * @example
 * // Este layout es montado automáticamente por expo-router en la ruta:
 * // /(app)/_layout
 */

import { Redirect, Stack } from 'expo-router';
import React from 'react';
import { ActivityIndicator, View } from 'react-native';
import { useAuth } from '../../src/contexts/AuthContext';

/**
 * @function AppLayout
 * @description Componente de layout protegido para el segmento `(app)`.
 *              Evalúa el estado de autenticación y decide qué renderizar:
 *              un loader, una redirección al login, o el Stack de pantallas protegidas.
 *
 * @returns {JSX.Element} Loader, redirección o Stack de navegación según el estado de sesión.
 */
export default function AppLayout() {
  const { isAuthenticated, isLoading } = useAuth();

  /* ── Estado 1: verificando sesión ── */
  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  /* ── Estado 2: sin sesión activa → redirigir al login ── */
  if (!isAuthenticated) {
    return <Redirect href="/(auth)/login" />;
  }

  /* ── Estado 3: autenticado → renderizar pantallas protegidas ── */
  return (
    <Stack screenOptions={{ headerShown: false }}>

      {/* ── Pantalla principal de bienvenida ── */}
      <Stack.Screen name="index" />

      {/* ── Gestión de compromisos ── */}
      <Stack.Screen name="compromises" />

      {/* ── Gestión de finanzas personales ── */}
      <Stack.Screen name="finances" />

      {/* ── Registro de gastos fijos recurrentes ── */}
      <Stack.Screen name="fixed-expenses" />

      {/* ── Gestión segura de contraseñas ── */}
      <Stack.Screen name="passwords" />

      {/* ── Perfil y configuración del usuario ── */}
      <Stack.Screen name="profile" />

      {/* ── Administración de categorías ── */}
      <Stack.Screen name="categories" />

    </Stack>
  );
}