/**
 * @file _layout.tsx
 * @description Layout raíz del módulo de autenticación para la aplicación Adulto Funcional.
 *              Define la estructura de navegación en pila (Stack) que agrupa todas las
 *              pantallas del flujo de autenticación, ocultando la barra de encabezado
 *              nativa en todas ellas.
 *
 * @module app/(auth)/_layout
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Este archivo es reconocido automáticamente por expo-router como el layout del
 * segmento `(auth)`. Todas las pantallas declaradas aquí comparten la configuración
 * de navegación definida en `screenOptions`.
 *
 * Pantallas registradas en el Stack:
 * - `login`           → Pantalla de inicio de sesión.
 * - `register`        → Pantalla de registro de nuevos usuarios.
 * - `forgot-password` → Pantalla de solicitud de recuperación de contraseña.
 * - `reset-password`  → Pantalla de restablecimiento de contraseña.
 *
 * @example
 * // Este layout es montado automáticamente por expo-router en la ruta:
 * // /(auth)/_layout
 */

import { Stack } from 'expo-router';

/**
 * @function AuthLayout
 * @description Componente de layout para el módulo de autenticación.
 *              Configura un navegador en pila (Stack) sin encabezado visible,
 *              permitiendo que cada pantalla maneje su propio diseño visual.
 *
 * @returns {JSX.Element} Navegador Stack con las pantallas del flujo de autenticación.
 */
export default function AuthLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>

      {/* ── Pantalla de inicio de sesión ── */}
      <Stack.Screen name="login" />

      {/* ── Pantalla de registro de nueva cuenta ── */}
      <Stack.Screen name="register" />

      {/* ── Pantalla de solicitud de recuperación de contraseña ── */}
      <Stack.Screen name="forgot-password" />

      {/* ── Pantalla de restablecimiento de contraseña ── */}
      <Stack.Screen name="reset-password" />

    </Stack>
  );
}