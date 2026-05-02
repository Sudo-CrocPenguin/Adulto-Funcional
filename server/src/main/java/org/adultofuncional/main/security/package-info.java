/**
 * Módulo de gestor de contraseñas con Master Key.
 *
 * <p>
 * Implementa la capa de infraestructura para almacenar credenciales
 * de servicios externos de forma segura usando encriptación AES-256.
 *
 * <p>
 * Responsabilidades:
 * <ul>
 * <li>La Master Key (hash Argon2 en {@code accounts}) debe verificarse
 * antes de acceder al gestor</li>
 * <li>El acceso se protege con
 * {@link org.adultofuncional.main.shared.exception.ForbiddenException} (HTTP
 * 403)
 * si no se verifica la Master Key</li>
 * <li>Almacenamiento seguro de contraseñas con encriptación AES-256</li>
 * <li>Derivación de claves usando la Master Key del usuario</li>
 * <li>Gestión de salts e IV únicos por credencial</li>
 * </ul>
 *
 * <p>
 * Tabla asociada: {@code passwords}.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.security;
