/**
 * Módulo de autenticación y autorización.
 *
 * <p>Gestiona el acceso al sistema mediante JWT y hash Argon2.
 * Expone endpoints públicos para login y registro de usuarios.</p>
 *
 * <p>Características:
 * <ul>
 *   <li>Autenticación stateless con JSON Web Tokens</li>
 *   <li>Hash de contraseñas con Argon2 (resistente a ataques GPU/ASIC)</li>
 *   <li>Verificación de Master Key para el gestor de contraseñas</li>
 * </ul>
 * </p>
 *
 * <p>Tablas asociadas: {@code accounts} (campos {@code account_password}
 * y {@code account_master_key}).</p>
 *
 * @author Equipo de desarrollo
 * @since 0.0.1
 */
package org.adultofuncional.main.auth;
