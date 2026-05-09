/**
 * Casos de uso del módulo de finanzas para la gestión de categorías.
 *
 * <p>
 * Orquesta las operaciones de creación, consulta, actualización, eliminación
 * y listado de categorías financieras. Cada caso de uso es un servicio de
 * aplicación independiente que coordina el dominio
 * ({@link org.adultofuncional.main.finances.domain.model.Category})
 * y el puerto de repositorio
 * ({@link org.adultofuncional.main.finances.domain.repository.CategoryRepository})
 * para ejecutar una única operación de negocio, manteniendo la lógica de
 * aplicación desacoplada de la infraestructura REST y de persistencia.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.category.CreateCategoryUseCase}
 * —
 * Crea una nueva categoría a partir de un
 * {@link org.adultofuncional.main.finances.application.dto.category.CreateCategoryRequest}
 * validado y retorna un
 * {@link org.adultofuncional.main.finances.application.dto.category.CategoryResponse}.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.category.GetCategoryUseCase}
 * —
 * Obtiene una categoría por su UUID. Lanza
 * {@link org.adultofuncional.main.shared.exception.NotFoundException}
 * si no existe.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.category.UpdateCategoryUseCase}
 * —
 * Actualiza parcialmente una categoría existente. Solo modifica los campos
 * proporcionados en el
 * {@link org.adultofuncional.main.finances.application.dto.category.UpdateCategoryRequest}
 * (nombre y/o tipo).</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.category.DeleteCategoryUseCase}
 * —
 * Elimina una categoría por su identificador. Verifica existencia previa.</li>
 * <li>{@link org.adultofuncional.main.finances.application.usecase.category.ListCategoriesUseCase}
 * —
 * Lista todas las categorías, con filtro opcional por
 * {@link org.adultofuncional.main.finances.domain.enums.CategoryType}.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code create}, {@code update},
 * {@code delete}) se ejecutan con {@code @Transactional} para garantizar
 * atomicidad.</li>
 * <li>Las validaciones de formato y seguridad (XSS) se aplican en los DTOs
 * de entrada correspondientes, por lo que los casos de uso asumen datos
 * ya validados.</li>
 * <li>Se recomienda no añadir lógica de negocio adicional que no esté
 * directamente relacionada con la orquestación de la operación; la lógica
 * de dominio reside en la entidad
 * {@link org.adultofuncional.main.finances.domain.model.Category}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.dto.category
 * @see org.adultofuncional.main.finances.domain.model.Category
 * @see org.adultofuncional.main.finances.domain.repository.CategoryRepository
 */
package org.adultofuncional.main.finances.application.usecase.category;
