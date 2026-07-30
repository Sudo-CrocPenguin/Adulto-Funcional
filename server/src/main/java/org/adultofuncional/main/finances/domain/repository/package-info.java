/**
 * Puertos de repositorio (interfaces) del dominio de finanzas.
 *
 * <p>
 * Define los contratos de persistencia que los casos de uso requieren para
 * interactuar con las entidades del módulo financiero sin conocer los detalles
 * de almacenamiento. Cada interfaz expone las operaciones mínimas necesarias
 * (búsqueda, listado, guardado y eliminación) y es implementada en la capa de
 * infraestructura mediante adaptadores JPA.
 *
 * <h2>Puertos incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.CategoryRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.finances.domain.model.Category}.
 * Incluye búsqueda por lote de IDs ({@code findAllById}) para optimizar la
 * carga de categorías en listados.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.finances.domain.model.FixedExpense}.
 * Expone {@code findAllByAccountId} para obtener todos los gastos fijos de una
 * cuenta.</li>
 * <li>{@link org.adultofuncional.main.finances.domain.repository.MovementRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.finances.domain.model.Movement}.
 * Expone {@code findAllByAccountId} para obtener el historial completo de
 * movimientos de una cuenta.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Los métodos de eliminación ({@code deleteById}) tienen comportamiento
 * silencioso si el identificador no existe; la validación de existencia previa
 * es responsabilidad de la capa de aplicación.</li>
 * <li>Los métodos de listado retornan colecciones completas; el filtrado
 * adicional se aplica en memoria en los casos de uso correspondientes.</li>
 * <li>Las implementaciones concretas se encuentran en el paquete
 * {@code org.adultofuncional.main.finances.infrastructure.repository}.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.finances.domain.model
 * @see org.adultofuncional.main.finances.infrastructure.repository
 */
package org.adultofuncional.main.finances.domain.repository;
