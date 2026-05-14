/**
 * Mapeadores entre entidades JPA y modelos de dominio del módulo de finanzas.
 *
 * <p>
 * Contiene los componentes responsables de convertir entre las entidades JPA
 * ({@link org.adultofuncional.main.finances.infrastructure.persistence.entity})
 * y los modelos de dominio
 * ({@link org.adultofuncional.main.finances.domain.model}).
 * Los mapeadores utilizan los métodos de fábrica del dominio
 * ({@code reconstitute}) para reconstituir instancias desde persistencia,
 * y construyen las entidades JPA con referencias mínimas (solo IDs) para
 * que Hibernate resuelva las claves foráneas.
 *
 * <h2>Mapeadores incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.mapper.CategoryMapper}
 * —
 * Convierte entre {@code CategoryEntity} y {@code Category}.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.mapper.FixedExpenseMapper}
 * —
 * Convierte entre {@code FixedExpensesEntity} y {@code FixedExpense}.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.mapper.MovementMapper}
 * —
 * Convierte entre {@code MovementEntity} y {@code Movement}. Preserva la
 * fecha de registro ({@code movementRegisterDate}) durante las
 * actualizaciones.</li>
 * </ul>
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.infrastructure.persistence.entity
 * @see org.adultofuncional.main.finances.domain.model
 */
package org.adultofuncional.main.finances.infrastructure.persistence.mapper;
