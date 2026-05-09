/**
 * Repositorios Spring Data JPA del módulo de agenda.
 *
 * <p>
 * Contiene la interfaz
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository},
 * que extiende {@link org.springframework.data.jpa.repository.JpaRepository} y
 * define consultas específicas para la entidad
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity}.
 * Estas interfaces son utilizadas por los adaptadores de repositorio en la capa
 * de infraestructura.
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity
 * @see org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl
 */
package org.adultofuncional.main.agenda.infrastructure.persistence.repository;
