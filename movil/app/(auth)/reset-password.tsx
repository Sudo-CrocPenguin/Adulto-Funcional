/**
 * @file reset-password.tsx
 * @description Pantalla de restablecimiento de contraseña para la aplicación Adulto Funcional.
 *              Actualmente funciona como pantalla de marcador de posición (placeholder)
 *              mientras la funcionalidad completa es implementada.
 *
 * @module app/(auth)/reset-password
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Esta pantalla forma parte del flujo de recuperación de acceso. Es accedida
 * típicamente tras completar el paso previo en {@link ForgotPasswordScreen},
 * donde el usuario solicita el enlace o código de restablecimiento.
 * En su estado actual muestra un mensaje indicando que está en construcción.
 *
 * @example
 * // Esta pantalla es montada automáticamente por expo-router en la ruta:
 * // /(auth)/reset-password
 */

import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';

/**
 * @function ResetPasswordScreen
 * @description Componente principal de la pantalla de restablecimiento de contraseña.
 *              Renderiza un contenedor centrado con un título y un mensaje de estado
 *              indicando que la pantalla está en construcción.
 *
 * @returns {JSX.Element} Vista centrada con título y subtítulo informativos.
 */
export default function ResetPasswordScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Restablecer contraseña</Text>
      <Text style={styles.subtitle}>Pantalla en construcción</Text>
    </View>
  );
}

/**
 * @constant styles
 * @description Estilos locales del componente {@link ResetPasswordScreen}.
 *
 * @property {object} container - Contenedor principal: ocupa toda la pantalla,
 *                                centra el contenido vertical y horizontalmente,
 *                                y aplica el color de fondo global de la app.
 * @property {object} title     - Estilo del título principal: tamaño 24 y negrita.
 * @property {object} subtitle  - Estilo del subtítulo: tamaño 16 con margen superior.
 */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  subtitle: {
    fontSize: 16,
    marginTop: 10,
  },
});