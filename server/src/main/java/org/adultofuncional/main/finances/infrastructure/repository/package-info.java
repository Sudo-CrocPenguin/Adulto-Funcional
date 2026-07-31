/**
 * Adaptadores de persistencia del módulo de finanzas.
 *
 * <p><strong>Qué contiene:</strong> implementaciones de los puertos de
 * repositorio definidos por el dominio para categorías, movimientos y gastos
 * fijos.</p>
 *
 * <p><strong>Para qué sirve:</strong> mantiene al dominio y a los casos de uso
 * independientes de Spring Data JPA. Cada adaptador traduce modelos de dominio
 * a entidades de persistencia mediante su mapper correspondiente.</p>
 *
 * <p><strong>Cómo funciona:</strong> los adaptadores delegan las consultas SQL
 * en las interfaces del paquete
 * {@link org.adultofuncional.main.finances.infrastructure.persistence.repository}
 * y convierten el resultado al tipo expresado por el puerto de dominio.</p>
 *
 * @see org.adultofuncional.main.finances.domain.repository
 * @see org.adultofuncional.main.finances.infrastructure.repository.CategoryRepositoryImpl
 * @see org.adultofuncional.main.finances.infrastructure.repository.MovementRepositoryImpl
 * @see org.adultofuncional.main.finances.infrastructure.repository.FixedExpenseRepositoryImpl
 */
package org.adultofuncional.main.finances.infrastructure.repository;
