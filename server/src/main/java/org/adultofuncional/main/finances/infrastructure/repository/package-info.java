/**
 * Repositorios Spring Data JPA del módulo de finanzas.
 *
 * <p>
 * Contiene las interfaces que extienden
 * {@link org.springframework.data.jpa.repository.JpaRepository}
 * para las entidades JPA del módulo financiero. Estas interfaces son utilizadas
 * por los adaptadores de repositorio en la capa de infraestructura para
 * traducir las operaciones de los puertos de dominio
 * ({@link org.adultofuncional.main.finances.domain.repository}) a consultas JPA
 * contra MariaDB.
 *
 * <h2>Repositorios incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository}
 * —
 * Acceso a la tabla {@code categories}. Incluye consulta por tipo de
 * categoría.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository}
 * —
 * Acceso a la tabla {@code fixed_expenses}. Incluye consulta por cuenta
 * propietaria.</li>
 * <li>{@link org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository}
 * —
 * Acceso a la tabla {@code movements}. Incluye consulta por cuenta
 * propietaria.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.finances.infrastructure.persistence.entity
 * @see org.adultofuncional.main.finances.infrastructure.repository
 */
package org.adultofuncional.main.finances.infrastructure.persistence.repository;
