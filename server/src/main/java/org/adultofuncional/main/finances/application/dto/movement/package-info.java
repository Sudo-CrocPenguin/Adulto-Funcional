/**
 * DTOs de entrada y salida para la gestión de movimientos financieros.
 *
 * <p>
 * Contiene los objetos de transferencia de datos para las operaciones
 * de movimientos (creación, consulta y listado) dentro del módulo de
 * finanzas. Estos DTOs definen los contratos de comunicación entre los
 * clientes de la API y la capa de aplicación, aplicando validaciones de
 * formato, reglas de negocio, seguridad y parámetros de paginación/orden.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.dto.movement.MovementResponse}
 * —
 * Proyección de los datos de un movimiento retornados al cliente. Incluye
 * la categoría asociada como un
 * {@link org.adultofuncional.main.finances.application.dto.category.CategoryResponse}
 * anidado. Distingue entre la fecha del movimiento real
 * ({@code movementDate}) y la fecha de registro en el sistema
 * ({@code registerDate}).</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.movement.CreateMovementRequest}
 * —
 * Datos de entrada para la creación de un nuevo movimiento. Valida que el
 * tipo de movimiento sea obligatorio ({@code @NotNull}), el monto sea mayor
 * a 0.01 ({@code @DecimalMin}), la fecha sea obligatoria, la descripción
 * opcional no exceda 65,535 caracteres ({@code @Size}) y esté libre de HTML
 * ({@code @NoHtml}). La categoría es opcional.</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest}
 * —
 * Filtros opcionales para listar movimientos. Permite filtrar por rango de
 * fechas ({@code startDate}, {@code endDate}), tipo de movimiento, categoría,
 * y término de búsqueda libre sobre la descripción (máximo 100 caracteres,
 * sin HTML). Incluye parámetros de ordenamiento ({@code sortBy},
 * {@code sortDirection}) y paginación ({@code page}, {@code size}).</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre
 * ({@code description} en {@code CreateMovementRequest} y {@code searchTerm}
 * en {@code MovementFilterRequest}) están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 */
package org.adultofuncional.main.finances.application.dto.movement;
