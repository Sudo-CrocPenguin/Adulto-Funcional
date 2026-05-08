/**
 * DTOs de entrada y salida para la gestión de gastos fijos recurrentes.
 *
 * <p>
 * Contiene los objetos de transferencia de datos específicos para las
 * operaciones de gastos fijos (creación, actualización, consulta y listado)
 * dentro del módulo de finanzas. Estos DTOs definen los contratos de
 * comunicación entre los clientes de la API y la capa de aplicación,
 * aplicando validaciones de formato, reglas de negocio y seguridad.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse}
 * —
 * Proyección de los datos de un gasto fijo retornados al cliente. Incluye
 * la categoría asociada como un
 * {@link org.adultofuncional.main.finances.application.dto.category.CategoryResponse}
 * anidado para evitar consultas adicionales.</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest}
 * —
 * Datos de entrada para la creación de un nuevo gasto fijo. Valida que el
 * nombre sea obligatorio y no exceda 20 caracteres ({@code @Size}), esté
 * libre de HTML ({@code @NoHtml}), que el monto sea mayor a 0.01
 * ({@code @DecimalMin}), que la frecuencia ({@code @NotNull}) y el estado
 * ({@code @NotNull}) sean valores válidos de los enumerados correspondientes,
 * y que la fecha de cierre sea futura ({@code @Future}).</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest}
 * —
 * Datos de entrada para la modificación parcial de un gasto fijo. Todos
 * los campos son opcionales para permitir actualizaciones selectivas
 * (comportamiento PATCH). Los campos proporcionados aplican las mismas
 * validaciones que en la creación.</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest}
 * —
 * Filtros opcionales para listar gastos fijos. Permite filtrar por
 * {@link org.adultofuncional.main.finances.domain.enums.Status}, por
 * categoría y por un término de búsqueda libre sobre el nombre
 * (máximo 50 caracteres, sin HTML).</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre ({@code name}
 * en los DTOs de entrada y {@code searchTerm} en el filtro) están anotados
 * con {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 */
package org.adultofuncional.main.finances.application.dto.fixedexpense;
