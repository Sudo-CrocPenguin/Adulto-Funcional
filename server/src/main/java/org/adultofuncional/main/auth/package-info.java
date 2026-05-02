/**
 * Módulo de autenticación y autorización.
 *
 * <p>
 * Implementa la capa de aplicación e infraestructura para el acceso
 * al sistema bajo Clean Architecture.
 *
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>Autenticación stateless con JSON Web Tokens (JWT)</li>
 * <li>Registro de nuevos usuarios con validación de unicidad</li>
 * <li>Hash de contraseñas con Argon2 (resistente a ataques GPU/ASIC)</li>
 * </ul>
 *
 * <p>
 * Tabla asociada: {@code accounts} (campos {@code account_password}
 * y {@code account_master_key}).
 *
 * @author Jeronimo Ospina Zapata, Lydis Ester Jaraba, Juan Sebastian Rios,
 *         Miguel Angel Blandon Montes, Daniel Salazar
 * @since 0.0.1
 */
package org.adultofuncional.main.auth;
