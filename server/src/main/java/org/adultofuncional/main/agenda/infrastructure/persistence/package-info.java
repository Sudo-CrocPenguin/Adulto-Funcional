/**
 * Capa de persistencia del módulo de agenda.
 *
 * <p>
 * Agrupa los componentes JPA que traducen el modelo de dominio a la base de
 * datos relacional. Incluye la entidad JPA, los repositorios Spring Data y los
 * mapeadores que convierten entre las representaciones de persistencia y
 * dominio.
 *
 * <h2>Subpaquetes</h2>
 * <ul>
 * <li>{@code entity} —
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity}</li>
 * <li>{@code repository} —
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.repository.SpringEventJpaRepository}</li>
 * <li>{@code mapper} —
 * {@link org.adultofuncional.main.agenda.infrastructure.persistence.mapper.EventMapper}</li>
 * </ul>
 *
 * @author Daniel Salazar, Lidys Jaraba, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.entity
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.repository
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.mapper
 */
package org.adultofuncional.main.agenda.infrastructure.persistence;
