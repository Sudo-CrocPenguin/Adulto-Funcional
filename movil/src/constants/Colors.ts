// src/constants/Colors.ts

/**
 * Paleta de colores oficial de la aplicación.
 * Centraliza todos los colores para facilitar cambios de tema en el futuro.
 *
 * @author Miguel Angel Blandon Montes
 */

export const Colors = {
  // Colores primarios (basados en el diseño)
  primary: '#2D4F83',      // Azul corporativo (Headers y botones principales)
  primaryDark: '#1E3A5F',   // Azul más oscuro para hover/active
  primaryLight: '#E8F0FE',  // Fondo azul muy claro para tarjetas seleccionadas

  // Colores de fondo
  background: '#F4F7FA',    // Fondo general (gris azulado muy suave)
  cardBackground: '#FFFFFF', // Fondo de tarjetas y formularios

  // Colores de texto
  text: '#1A1A1A',          // Texto principal
  textSecondary: '#666666', // Subtítulos y placeholders
  textLight: '#FFFFFF',     // Texto sobre fondos oscuros

  // Bordes y líneas
  border: '#E1E8EF',        // Color de bordes de inputs y tarjetas

  // Estados y acciones
  link: '#3498db',          // Azul para enlaces
  error: '#e74c3c',         // Rojo para mensajes de error
  success: '#27ae60',       // Verde para éxito
  warning: '#f39c12',       // Amarillo para advertencias

  // Prioridades (para eventos)
  priorityHigh: '#e74c3c',
  priorityMedium: '#f39c12',
  priorityLow: '#27ae60',

  // Frecuencias (gastos fijos)
  frequencyMonthly: '#3498db',
  frequencyWeekly: '#9b59b6',
  frequencyDaily: '#1abc9c',

  // Estado de gastos fijos
  statusActive: '#27ae60',
  statusInactive: '#95a5a6',
};