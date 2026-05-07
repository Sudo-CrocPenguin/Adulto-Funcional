/**
 * @file index.tsx
 * @description Pantalla principal (Home) de la aplicación Adulto Funcional.
 *              Es la primera pantalla que ve el usuario tras autenticarse.
 *              Muestra un saludo personalizado con los datos del usuario activo
 *              y provee la opción de cerrar sesión.
 *
 * @module app/(app)/index
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Esta pantalla consume el contexto de autenticación {@link useAuth} para
 * obtener los datos del usuario actual y ejecutar el cierre de sesión.
 * Al cerrar sesión, redirige automáticamente a la pantalla de login.
 *
 * En modo demo se muestra un recordatorio con las credenciales de prueba
 * en la parte inferior de la pantalla.
 *
 * @example
 * // Esta pantalla es montada automáticamente por expo-router en la ruta:
 * // /(app)/index  →  ruta raíz del segmento (app)
 */

import { router } from 'expo-router';
import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';
import { useAuth } from '../../src/contexts/AuthContext';

/**
 * @function HomeScreen
 * @description Componente principal de la pantalla de inicio.
 *              Muestra el nombre completo y correo del usuario autenticado,
 *              un botón para cerrar sesión y un recordatorio de credenciales demo.
 *
 * @returns {JSX.Element} Vista centrada con información del usuario y opción de logout.
 */
export default function HomeScreen() {
  const { user, logout } = useAuth();

  /**
   * @function handleLogout
   * @description Ejecuta el cierre de sesión llamando a `logout` del contexto
   *              de autenticación y redirige al usuario a la pantalla de login.
   *
   * @returns {Promise<void>}
   */
  const handleLogout = async (): Promise<void> => {
    await logout();
    router.replace('/(auth)/login');
  };

  return (
    <View style={styles.container}>

      {/* ── Saludo principal ── */}
      <Text style={styles.title}>¡Bienvenido!</Text>

      {/* ── Nombre completo del usuario autenticado ── */}
      <Text style={styles.subtitle}>
        {user?.names} {user?.lastnames}
      </Text>

      {/* ── Correo electrónico del usuario autenticado ── */}
      <Text style={styles.email}>{user?.email}</Text>

      {/* ── Botón de cierre de sesión ── */}
      <TouchableOpacity style={styles.button} onPress={handleLogout}>
        <Text style={styles.buttonText}>Cerrar sesión</Text>
      </TouchableOpacity>

      {/* ── Recordatorio de credenciales en modo demo ── */}
      <Text style={styles.demoInfo}>
        Demo: admin@desarrollo.com / Admin123!
      </Text>

    </View>
  );
}

/**
 * @constant styles
 * @description Estilos locales del componente {@link HomeScreen}.
 *
 * @property {object} container  - Contenedor principal: pantalla completa centrada
 *                                 con padding lateral y color de fondo global.
 * @property {object} title      - Título de bienvenida: tamaño 28, negrita y color primario.
 * @property {object} subtitle   - Nombre del usuario: tamaño 20 con margen inferior.
 * @property {object} email      - Correo del usuario: tamaño 16 en color secundario.
 * @property {object} button     - Botón de logout: fondo primario, bordes redondeados y padding.
 * @property {object} buttonText - Texto del botón: blanco y negrita.
 * @property {object} demoInfo   - Texto informativo de demo: pequeño, centrado y en color secundario.
 */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: Colors.primary,
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 20,
    marginBottom: 5,
  },
  email: {
    fontSize: 16,
    color: Colors.textSecondary,
    marginBottom: 30,
  },
  button: {
    backgroundColor: Colors.primary,
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 12,
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  demoInfo: {
    marginTop: 40,
    fontSize: 12,
    color: Colors.textSecondary,
    textAlign: 'center',
  },
});