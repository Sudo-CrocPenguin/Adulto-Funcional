/**
 * @file _layout.tsx
 * @description Layout raíz de la aplicación Adulto Funcional.
 *              Es el punto de entrada principal de expo-router: envuelve toda la
 *              aplicación con el proveedor de autenticación {@link AuthProvider}
 *              y define el navegador en pila (Stack) de nivel superior con los
 *              dos segmentos principales de navegación.
 *
 * @module app/_layout
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Al ser el layout raíz, este archivo es el primero en montarse. Su función es:
 * 1. Proveer el contexto global de autenticación a toda la aplicación a través
 *    de {@link AuthProvider}, haciendo accesible `useAuth` en cualquier pantalla.
 * 2. Registrar los dos segmentos de navegación principales:
 *    - `(auth)` → Segmento de pantallas públicas (login, registro, recuperación).
 *    - `(app)`  → Segmento de pantallas protegidas (funcionalidades de la app).
 *
 * @example
 * // Este layout es montado automáticamente por expo-router en la ruta:
 * // app/_layout
 */

import { Stack } from 'expo-router';
import React from 'react';
import { AuthProvider } from '../src/contexts/AuthContext';

/**
 * @function RootLayout
 * @description Componente raíz de la aplicación. Provee el contexto de autenticación
 *              global y configura el navegador Stack principal sin encabezado visible,
 *              delegando el diseño de cada segmento a sus propios layouts.
 *
 * @returns {JSX.Element} Árbol raíz con {@link AuthProvider} y el Stack de navegación.
 */
export default function RootLayout() {
  return (
    <AuthProvider>
      <Stack screenOptions={{ headerShown: false }}>

        {/* ── Segmento de autenticación (pantallas públicas) ── */}
        <Stack.Screen name="(auth)" />

        {/* ── Segmento principal de la app (pantallas protegidas) ── */}
        <Stack.Screen name="(app)" />

      </Stack>
    </AuthProvider>
  );
}