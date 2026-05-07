// app/(auth)/_layout.tsx

/**
 * Layout para el grupo de autenticación (sin autenticación requerida).
 * Oculta el header predeterminado para usar el diseño personalizado.
 *
 * @author Miguel Angel Blandon Montes
 */

import { Stack } from 'expo-router';

export default function AuthLayout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="login" />
      <Stack.Screen name="register" />
      <Stack.Screen name="forgot-password" />
      <Stack.Screen name="reset-password" />
    </Stack>
  );
}