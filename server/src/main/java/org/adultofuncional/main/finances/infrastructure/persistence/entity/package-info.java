/**
 * Entidades JPA del módulo de finanzas.
 *
 * <p>
 * Contiene las entidades que mapean las tablas {@code categories},
 * {@code movements} y {@code fixed_expenses} de MariaDB. Estas entidades
 * son utilizadas exclusivamente por la capa de infraestructura y los
 * mapeadores; nunca se exponen directamente a las capas de aplicación o
 * dominio.
 *
 * <h2>Entidades incluidas</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity}
 * —
 * Categorías de clasificación para movimientos, gastos fijos y eventos.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity}
 * —
 * Movimientos financieros (ingresos y egresos) asociados a una cuenta y
 * categoría.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity}
 * —
 * Gastos fijos recurrentes con frecuencia, estado y recordatorio.</li>
 * </ul>
 *
 * <h2>Características</h2>
 * <ul>
 * <li><strong>Relaciones:</strong> Todas las entidades referencian a
 * {@link org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity}
 * como propietario y a {@code CategoryEntity} como clasificador.</li>
 * <li><strong>Fechas automáticas:</strong> {@code MovementEntity} establece
 * {@code movement_register_date} mediante {@code @PrePersist} en el momento
 * del primer {@code INSERT}.</li>
 * <li><strong>Conversiones:</strong> Los mapeadores en
 * {@code org.adultofuncional.main.finances.infrastructure.persistence.mapper}
 * convierten estas entidades a los modelos de dominio correspondientes.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.infrastructure.persistence.mapper
 * @see org.adultofuncional.main.finances.domain.model
 */
package org.adultofuncional.main.finances.infrastructure.persistence.entity;
