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
 * Credencial de un servicio externo (plataforma, usuario, contraseña cifrada
 * con AES‑256, notas) vinculada a una cuenta propietaria.</li>
 * </ul>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li><strong>Métodos de fábrica:</strong> {@code create} para nuevas
 * credenciales (genera UUID v7 y {@code createdAt}) y {@code reconstitute}
 * para reconstruir instancias desde la capa de persistencia.</li>
 * <li><strong>Validación de invariantes:</strong> Valida que la plataforma,
 * el usuario, la contraseña cifrada y la cuenta no sean nulos ni vacíos,
 * y que la fecha de creación no sea futura.</li>
 * <li><strong>Método de actualización:</strong> {@code update} modifica los
 * campos editables y registra automáticamente la fecha de modificación en
 * {@code updatedAt}.</li>
 * <li><strong>Seguridad:</strong> El campo {@code encryptedPassword} se
 * excluye del método {@code toString()} para evitar fugas accidentales en
 * logs.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.domain.model.Password
 * @see org.adultofuncional.main.security.domain.repository.PasswordRepository
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 */
package org.adultofuncional.main.security.domain.model;
