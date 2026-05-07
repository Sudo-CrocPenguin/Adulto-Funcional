// src/constants/Styles.ts

/**
 * Estilos globales reutilizables en toda la aplicación.
 * Sigue el sistema de diseño basado en tarjetas y header azul.
 *
 * @author Miguel Angel Blandon Montes
 */

import { StyleSheet } from 'react-native';
import { Colors } from './Colors';

export const GlobalStyles = StyleSheet.create({
  // Contenedor principal de todas las pantallas
  mainContainer: {
    flex: 1,
    backgroundColor: Colors.background,
  },

  // Header azul superior (contiene logo y eslogan)
  header: {
    backgroundColor: Colors.primary,
    paddingTop: 60,
    paddingBottom: 40,
    alignItems: 'center',
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  headerTitle: {
    color: Colors.textLight,
    fontSize: 26,
    fontWeight: 'bold',
    marginTop: 10,
  },
  headerSubtitle: {
    color: Colors.textLight,
    fontSize: 14,
    opacity: 0.85,
    textAlign: 'center',
    paddingHorizontal: 20,
  },

  // Tarjeta blanca que flota sobre el header
  card: {
    backgroundColor: Colors.cardBackground,
    borderRadius: 30,
    padding: 25,
    marginTop: -30, // Superposición sobre el header azul
    marginHorizontal: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.1,
    shadowRadius: 15,
    elevation: 8,
  },

  // Títulos dentro de la tarjeta
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 20,
    color: Colors.text,
  },

  // Campos de entrada
  inputContainer: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: Colors.text,
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 12,
    fontSize: 16,
    backgroundColor: Colors.cardBackground,
    color: Colors.text,
  },
  inputError: {
    borderColor: Colors.error,
  },
  errorText: {
    color: Colors.error,
    fontSize: 12,
    marginTop: 4,
  },

  // Botones
  button: {
    backgroundColor: Colors.primary,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    backgroundColor: Colors.textSecondary,
    opacity: 0.6,
  },
  buttonText: {
    color: Colors.textLight,
    fontSize: 16,
    fontWeight: '600',
  },

  // Enlaces
  link: {
    color: Colors.link,
    fontWeight: '500',
  },

  // Checkbox (para "Recuérdame")
  checkboxContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 12,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderWidth: 1,
    borderColor: Colors.border,
    borderRadius: 4,
    marginRight: 8,
    backgroundColor: Colors.cardBackground,
  },
  checkboxChecked: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  checkboxText: {
    fontSize: 14,
    color: Colors.text,
  },
});