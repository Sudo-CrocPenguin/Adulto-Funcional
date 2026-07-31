/**
 * Repositorios Spring Data JPA del módulo de finanzas.
 *
 * <p><strong>Qué contiene:</strong> interfaces JPA para acceder a las tablas
 * {@code categories}, {@code movements} y {@code fixed_expenses}.</p>
 *
 * <p><strong>Para qué sirve:</strong> concentra en infraestructura los métodos
 * derivados y las consultas nativas, incluyendo los filtros de ownership por
 * cuenta.</p>
 *
 * <p><strong>Cómo funciona:</strong> Spring Data implementa estas interfaces en
 * runtime. Los adaptadores del paquete
 * {@link org.adultofuncional.main.finances.infrastructure.repository} las
 * consumen sin exponer JPA a dominio o aplicación.</p>
 *
 * @see org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository
 * @see org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository
 * @see org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository
 */
package org.adultofuncional.main.finances.infrastructure.persistence.repository;
