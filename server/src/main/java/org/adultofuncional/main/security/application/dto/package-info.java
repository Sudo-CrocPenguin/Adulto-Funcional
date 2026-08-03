/**
 * DTOs de entrada y salida para la gestión de credenciales almacenadas.
 *
 * <p>
 * Contiene los objetos de transferencia de datos para las operaciones de
 * creación, actualización y consulta de contraseñas en el gestor seguro.
 * Estos DTOs definen los contratos de comunicación entre los clientes de la
 * API y la capa de aplicación, aplicando validaciones de formato y protección
 * anti‑XSS en los campos de texto libre.
 *
 * <p>
 * <strong>Nota de seguridad:</strong> el campo {@code password} en los DTOs
 * de entrada contiene la contraseña en texto plano. La encriptación AES‑256
 * se realiza en la capa de aplicación usando la Master Key del usuario. Este
 * valor nunca se almacena en texto plano. Solo la consulta individual lo
 * devuelve, después de validar la sesión de Master Key; los listados lo omiten.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordRequest}
 * —
 * Datos de entrada para la creación o actualización completa de una
 * credencial. Valida que el nombre de la aplicación sea obligatorio
 * ({@code @NotBlank}), máximo 35 caracteres ({@code @Size}) y esté libre de
 * HTML ({@code @NoHtml}).</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordUpdateRequest}
 * —
 * Datos de entrada para la modificación parcial de una credencial
 * (comportamiento PATCH). Todos los campos son opcionales; los no enviados
 * conservan su valor actual.</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordResponse}
 * —
 * Proyección de una credencial. Nunca expone material criptográfico
 * ({@code salt}, {@code iv}, {@code ciphertext}); el secreto descifrado solo
 * se llena en la consulta individual y nunca en listados.</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.PasswordFilterRequest}
 * — filtros, página y orden del listado de bóveda.</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.ConfigureMasterKeyRequest}
 * y {@link org.adultofuncional.main.security.application.dto.ChangeMasterKeyRequest}
 * — configuración y rotación reautenticadas.</li>
 * <li>{@link org.adultofuncional.main.security.application.dto.VerifyMasterKeyRequest}
 * y {@link org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse}
 * — desbloqueo y estado de la Master Key en la sesión actual.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre
 * ({@code applicationName}) en los DTOs de entrada están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 */
package org.adultofuncional.main.security.application.dto;
