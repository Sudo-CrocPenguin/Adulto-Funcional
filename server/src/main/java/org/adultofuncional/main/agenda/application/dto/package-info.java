/**
 * DTOs de entrada y salida para la gestión de eventos de la agenda.
 *
 * <p>
 * Contiene los objetos de transferencia de datos para las operaciones de
 * eventos (creación, actualización, consulta y listado) dentro del módulo
 * de agenda. Estos DTOs definen los contratos de comunicación entre los
 * clientes de la API y la capa de aplicación, aplicando validaciones de
 * formato, reglas de negocio y protección anti‑XSS en los campos de texto.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventRequest} —
 * Datos de entrada para la creación de un nuevo evento. Valida que el título
 * sea obligatorio ({@code @NotBlank}), no exceda 35 caracteres ({@code @Size})
 * y esté libre de HTML ({@code @NoHtml}). La fecha debe ser presente o futura
 * ({@code @FutureOrPresent}), y las horas de inicio y fin son
 * obligatorias.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventUpdateRequest}
 * —
 * Datos de entrada para la modificación parcial de un evento. Todos los campos
 * son opcionales para permitir actualizaciones selectivas (comportamiento
 * PATCH).
 * Los campos de texto mantienen las validaciones {@code @Size} y
 * {@code @NoHtml}.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventResponse} —
 * Proyección de los datos de un evento retornados al cliente. Incluye la
 * categoría
 * asociada como un
 * {@link org.adultofuncional.main.finances.application.dto.category.CategoryResponse}
 * anidado cuando el evento tiene categoría vinculada.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre ({@code title},
 * {@code description}) en los DTOs de entrada están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 */
package org.adultofuncional.main.agenda.application.dto;
