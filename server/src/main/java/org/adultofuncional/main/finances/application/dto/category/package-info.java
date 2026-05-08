/**
 * DTOs de entrada y salida para la gestión de categorías financieras.
 *
 * <p>
 * Contiene los objetos de transferencia de datos específicos para las
 * operaciones de categorías (creación, actualización, consulta y listado)
 * dentro del módulo de finanzas. Estos DTOs mantienen la separación entre
 * la capa de aplicación y los clientes externos, definiendo contratos claros
 * de entrada/salida y aplicando las validaciones de negocio y seguridad
 * necesarias antes de que los datos alcancen el dominio.
 *
 * <h2>DTOs incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.dto.category.CategoryResponse}
 * —
 * Proyección de los datos de categoría retornados al cliente. Implementa
 * {@link org.adultofuncional.main.shared.security.OwnedResource} para
 * integrarse con el validador de propiedad, retornando {@code null} en
 * {@code getEmail()} dado que las categorías no tienen dueño individual.</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest}
 * —
 * Datos de entrada para la creación de una nueva categoría. Valida que
 * el nombre sea obligatorio ({@code @NotBlank}), no exceda 50 caracteres
 * ({@code @Size}) y esté libre de HTML ({@code @NoHtml}). El tipo es
 * obligatorio ({@code @NotNull}).</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest}
 * —
 * Datos de entrada para la modificación parcial de una categoría. Todos
 * los campos son opcionales para permitir actualizaciones selectivas
 * (comportamiento PATCH). El nombre conserva las validaciones {@code @Size}
 * y {@code @NoHtml}.</li>
 * <li>{@link org.adultofuncional.main.finances.application.dto.category.CategoryFilterRequest}
 * —
 * Filtros opcionales para listar categorías. Actualmente permite filtrar
 * por {@link org.adultofuncional.main.finances.domain.enums.CategoryType}.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li><strong>Stored XSS:</strong> Los campos de texto libre ({@code name})
 * en los DTOs de entrada están anotados con
 * {@link org.adultofuncional.main.shared.security.NoHtml}, que utiliza
 * Jsoup con {@code Safelist.none()} para rechazar cualquier contenido HTML
 * y prevenir la persistencia de scripts maliciosos.</li>
 * <li><strong>Ownership:</strong> {@code CategoryResponse} implementa
 * {@code OwnedResource} para que el
 * {@link org.adultofuncional.main.shared.security.OwnershipValidator}
 * pueda procesar solicitudes sobre categorías sin acoplarse al dominio
 * de finanzas.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.shared.security.OwnedResource
 */
package org.adultofuncional.main.finances.application.dto.category;
