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
 * valor nunca se almacena ni se expone en las respuestas.
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
 * Proyección de los datos no sensibles de una credencial retornados al
 * cliente. Nunca expone material criptográfico ({@code salt}, {@code iv},
 * {@code ciphertext}) ni la contraseña en texto plano.</li>
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
