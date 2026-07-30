/**
 * Casos de uso del módulo de seguridad (gestor de contraseñas).
 *
 * <p>
 * Contiene los servicios de aplicación que orquestan las operaciones de
 * creación, consulta, actualización, eliminación y listado de credenciales
 * almacenadas. Cada caso de uso coordina el dominio
 * ({@link org.adultofuncional.main.security.domain.model.Password}) y los
 * puertos de repositorio y servicios, validando reglas de negocio como la
 * verificación de la Master Key, la unicidad del nombre de aplicación y el
 * cifrado/descifrado AES‑256.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase}
 * —
 * Guarda una nueva credencial.</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase}
 * —
 * Lista las credenciales de una cuenta.</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.GetPasswordUseCase}
 * —
 * Obtiene una credencial con la contraseña descifrada.</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase}
 * —
 * Actualiza parcialmente una credencial.</li>
 * <li>{@link org.adultofuncional.main.security.application.usecase.DeletePasswordUseCase}
 * —
 * Elimina una credencial.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code create}, {@code update},
 * {@code delete}) se ejecutan con {@code @Transactional} para garantizar
 * atomicidad.</li>
 * <li>La Master Key debe estar verificada antes de cualquier operación.</li>
 * <li>El cifrado/descifrado se delega a
 * {@link org.adultofuncional.main.security.domain.service.EncryptionService}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.dto
 * @see org.adultofuncional.main.security.domain.model.Password
 * @see org.adultofuncional.main.security.domain.service
 */
package org.adultofuncional.main.security.application.usecase;
