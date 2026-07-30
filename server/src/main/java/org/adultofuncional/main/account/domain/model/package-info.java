/**
 * Modelos de dominio del módulo de cuentas de usuario.
 *
 * <p>
 * Contiene la entidad
 * {@link org.adultofuncional.main.account.domain.model.Account},
 * que representa la cuenta de un usuario en el sistema. Encapsula las
 * invariantes
 * de negocio y es responsable de generar su identidad (UUID v7) y la fecha de
 * creación a través de métodos de fábrica.
 *
 * <h2>Entidad incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.domain.model.Account} —
 * Cuenta de usuario con datos personales, email único, hash de contraseña y
 * master key opcional.</li>
 * </ul>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li><strong>Métodos de fábrica:</strong> {@code create} para nuevas cuentas
 * (genera UUID v7 y {@code createdAt}) y {@code reconstitute} para reconstruir
 * instancias desde la capa de persistencia.</li>
 * <li><strong>Validación de invariantes:</strong> Valida que el ID y la fecha
 * de creación no sean nulos ni futuros. Las validaciones de formato se delegan
 * a los DTOs de la capa de aplicación.</li>
 * <li><strong>Métodos de actualización:</strong> {@code updateDetails} para
 * nombres, apellidos y teléfono; {@code updateEmail} para el correo.</li>
 * <li><strong>Datos sensibles:</strong> Los campos {@code passwordHash} y
 * {@code masterKeyHash} se almacenan en el dominio pero se excluyen de las
 * proyecciones de respuesta ({@code AccountResponse}) mediante el mapper de
 * infraestructura.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 */
package org.adultofuncional.main.account.domain.model;
