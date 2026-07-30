/**
 * Adaptadores de repositorio del módulo de agenda.
 *
 * <p>
 * Contiene la implementación concreta del puerto
 * {@link org.adultofuncional.main.agenda.domain.repository.EventRepository},
 * que conecta el dominio con Spring Data JPA. Utiliza
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository}
 * y el mapper para traducir entre entidades JPA y el modelo de dominio.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl
 * @see org.adultofuncional.main.agenda.domain.repository.EventRepository
 */
package org.adultofuncional.main.agenda.infrastructure.repository;
