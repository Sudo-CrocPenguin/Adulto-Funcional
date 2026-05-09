/**
 * Puertos de servicio (interfaces) del dominio de seguridad.
 *
 * <p>
 * Define contratos que el dominio necesita pero cuya implementación
 * concreta pertenece a la capa de infraestructura. Estos servicios
 * son inyectados en los casos de uso para mantener el dominio
 * desacoplado de frameworks y bibliotecas externas.
 *
 * <h2>Servicios definidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.domain.service.EncryptionService}
 * —
 * Cifrado y descifrado de contraseñas (AES‑256‑GCM).</li>
 * <li>{@link org.adultofuncional.main.security.domain.service.MasterKeySessionService}
 * —
 * Gestión de la Master Key en sesión.</li>
 * </ul>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.security.domain.service;
