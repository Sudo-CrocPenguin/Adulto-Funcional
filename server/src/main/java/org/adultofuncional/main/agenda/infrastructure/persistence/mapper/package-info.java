/**
 * Mapeadores entre entidades JPA y modelos de dominio del módulo de agenda.
 *
 * <p>
 * Contiene el componente
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.mapper.EventMapper},
 * responsable de convertir entre
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity}
 * (JPA) y {@link org.adultofuncional.main.agenda.domain.model.Event} (dominio).
 * Los mapeadores utilizan los métodos de fábrica del dominio
 * ({@code reconstitute}) y construyen las entidades JPA necesarias para la
 * persistencia.
 *
 * @author Lidys Jaraba
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity
 * @see org.adultofuncional.main.agenda.domain.model.Event
 */
package org.adultofuncional.main.agenda.infrastructure.persistence.mapper;
