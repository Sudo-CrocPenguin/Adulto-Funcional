/**
 * Módulo de seguridad y gestor de contraseñas.
 *
 * <p>Proporciona almacenamiento seguro de credenciales de servicios
 * externos mediante encriptación AES-256 protegida con una Master Key.</p>
 *
 * <p>Características de seguridad:
 * <ul>
 *   <li>Las contraseñas se encriptan con AES-256 antes de persistirse</li>
 *   <li>La Master Key (hash Argon2 en {@code accounts}) debe verificarse
 *       antes de acceder al gestor</li>
 *   <li>El acceso se protege con {@link ForbiddenException} (HTTP 403)
 *       si no se verifica la Master Key</li>
 * </ul>
 * </p>
 *
 * <p>Tabla asociada: {@code passwords} con FK a {@code accounts}.</p>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.security;
