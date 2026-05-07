/**
 * Layout del grupo de autenticación.
 * Define un stack simple sin tabs para login, registro, etc.
 *
 * @author Miguel Angel Blandon Montes
 */

import { Stack } from 'expo-router';

export default function AuthLayout() {
  return (
    <Stack>
      <Stack.Screen name="login" options={{ title: 'Iniciar Sesión' }} />
      <Stack.Screen name="register" options={{ title: 'Registrarse' }} />
      <Stack.Screen name="forgot-password" options={{ title: 'Recuperar Contraseña' }} />
      <Stack.Screen name="reset-password" options={{ title: 'Restablecer Contraseña' }} />
    </Stack>
  );
}