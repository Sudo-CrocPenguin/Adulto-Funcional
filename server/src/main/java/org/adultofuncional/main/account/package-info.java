/**
 * Módulo de gestión de cuentas de usuario.
 *
 * <p>
 * Implementa la capa de dominio, aplicación, infraestructura y
 * presentación para el manejo de cuentas bajo Clean Architecture.
 *
 * 
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>Registro y actualización de datos de cuenta</li>
 * <li>Consulta de información no sensible (sin password ni master key)</li>
 * <li>Validación de unicidad de correo electrónico</li>
 * </ul>
 *
 * 
 * <p>
 * Tabla asociada: {@code accounts} con identificador UUID v7.
 *
 * 
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.account;
