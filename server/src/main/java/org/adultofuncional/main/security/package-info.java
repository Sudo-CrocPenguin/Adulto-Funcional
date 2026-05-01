/**
 * Módulo de seguridad y gestor de contraseñas.
 *
 * <p>
 * Proporciona almacenamiento seguro de credenciales de servicios
 * externos mediante encriptación AES-256 protegida con una Master Key.
 *
 * 
 * <p>
 * Características de seguridad:
 * <ul>
 * <li>Las contraseñas se encriptan con AES-256 antes de persistirse</li>
 * <li>La Master Key (hash Argon2 en {@code accounts}) debe verificarse
 * antes de acceder al gestor</li>
 * <li>El acceso se protege con
 * {@link org.adultofuncional.main.shared.exception.ForbiddenException} (HTTP
 * 403)
 * si no se verifica la Master Key</li>
 * </ul>
 *
 * 
 * <p>
 * Tabla asociada: {@code passwords} con FK a {@code accounts}.
 *
 * 
 * @author Jeronimo Ospina Zapata, Lydis Ester Jaraba, Juan Sebastian Rios,
 *         Miguel Angel Blandon Montes, Daniel Salazar
 * @since 0.0.1
 */
package org.adultofuncional.main.security;
