/**
 * Autenticación, roles y familias de sesión.
 *
 * <p>Login y registro verifican credenciales Argon2 y crean una sesión durable.
 * Cada sesión emite un access JWT y un refresh token opaco de un solo uso. La
 * rotación se protege con bloqueo de fila, una ventana corta de concurrencia y
 * detección de replay.</p>
 *
 * <p>Los navegadores reciben cookies HttpOnly y usan CSRF; los clientes nativos
 * reciben tokens en el cuerpo y presentan el access como Bearer. Logout y los
 * endpoints de sesiones revocan access tokens y bloquean la Master Key
 * asociada.</p>
 *
 * <p>El módulo persiste {@code auth_sessions} y {@code account_roles}. Nunca
 * guarda refresh tokens completos ni expone hashes.</p>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.auth;
