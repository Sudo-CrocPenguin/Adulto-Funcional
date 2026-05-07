/**
 * @file forgot-password.tsx
 * @description Pantalla de recuperación de contraseña para la aplicación Adulto Funcional.
 *              Actualmente funciona como pantalla de marcador de posición (placeholder)
 *              mientras la funcionalidad completa es implementada.
 *
 * @module app/(auth)/forgot-password
 * @author Miguel Angel Blandón Montes
 *
 * @remarks
 * Esta pantalla es accesible desde el flujo de autenticación cuando el usuario
 * no recuerda su contraseña. En su estado actual muestra un mensaje indicando
 * que la funcionalidad está en construcción.
 *
 * @example
 * // Esta pantalla es montada automáticamente por expo-router en la ruta:
 * // /(auth)/forgot-password
 */

import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Colors } from '../../src/constants/Colors';

/**
 * @function ForgotPasswordScreen
 * @description Componente principal de la pantalla de recuperación de contraseña.
 *              Renderiza un contenedor centrado con un título y un mensaje de estado
 *              indicando que la pantalla está en construcción.
 *
 * @returns {JSX.Element} Vista centrada con título y subtítulo informativos.
 */
export default function ForgotPasswordScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Recuperar contraseña</Text>
      <Text style={styles.subtitle}>Pantalla en construcción</Text>
    </View>
  );
}

/**
 * @constant styles
 * @description Estilos locales del componente {@link ForgotPasswordScreen}.
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