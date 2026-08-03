/**
 * Modelos de dominio del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene la entidad
 * {@link org.adultofuncional.main.security.domain.model.Password},
 * que representa una credencial almacenada de forma segura asociada a una
 * cuenta.
 * Encapsula las invariantes de negocio y es responsable de generar su identidad
 * (UUID v7) a través de métodos de fábrica.
 *
 * <h2>Entidad incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.domain.model.Password} —
 * Credencial de un servicio externo con nombre de aplicación, material
 * cifrado versionado y fecha de último cambio, vinculada a una cuenta.</li>
 * </ul>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li><strong>Métodos de fábrica:</strong> {@code create} para nuevas
 * credenciales (genera UUID v7 y {@code createdAt}) y {@code reconstitute}
 * para reconstruir instancias desde la capa de persistencia.</li>
 * <li><strong>Validación de invariantes:</strong> valida identidad, aplicación,
 * versión criptográfica, salt, IV de 12 bytes, ciphertext y cuenta.</li>
 * <li><strong>Método de actualización:</strong> {@code update} sustituye los
 * campos editables y conserva la versión que describe el cifrado.</li>
 * <li><strong>Seguridad:</strong> salt, IV y ciphertext se excluyen de
 * {@code toString()} para evitar fugas accidentales en logs.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.model.Password
 * @see org.adultofuncional.main.security.domain.repository.PasswordRepository
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 */
package org.adultofuncional.main.security.domain.model;
