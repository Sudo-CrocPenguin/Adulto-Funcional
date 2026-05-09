/**
 * DTOs de entrada y salida para la gestión de cuentas de usuario.
 *
 * <p>
 * Contiene los objetos de transferencia de datos específicos para las
 * operaciones de consulta y actualización de cuentas dentro del módulo
 * {@code account}. Estos DTOs definen los contratos de comunicación entre
 * los clientes de la API y la capa de aplicación, aplicando validaciones de
 * formato, reglas de negocio y protección anti‑XSS en los campos de texto.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.application.dto.AccountResponse}
 * —
 * Proyección de los datos de cuenta retornados al cliente. Implementa
 * {@link org.adultofuncional.main.shared.security.OwnedResource} para
 * integrarse con el validador de propiedad. Nunca expone el hash de
 * contraseña ni la master key.</li>
 * <li>{@link org.adultofuncional.main.account.application.dto.UpdateAccountRequest}
 * —
 * Datos de entrada validados para la modificación de una cuenta. Todos los
 * campos de texto libre están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}
 * para prevenir inyección de HTML malicioso (Stored XSS).</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre
 * ({@code names}, {@code lastnames}, {@code phone}, {@code email}) en
 * {@code UpdateAccountRequest} están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * <li><strong>Ownership:</strong> {@code AccountResponse} implementa
 * {@code OwnedResource} para que el
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator}
 * pueda validar el acceso sin acoplarse al módulo de cuentas.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.account.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.shared.security.OwnedResource
 */
package org.adultofuncional.main.account.application.dto;
