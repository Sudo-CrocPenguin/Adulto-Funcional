/**
 * Capa de infraestructura del módulo de agenda.
 *
 * <p>
 * Implementa los adaptadores que conectan el sistema con tecnologías externas
 * (Spring MVC, Spring Data JPA, MariaDB). Contiene el controlador REST, las
 * entidades JPA, los repositorios Spring Data, los mapeadores y la
 * implementación concreta de los puertos de repositorio definidos en el
 * dominio.
 *
 * <h2>Subpaquetes</h2>
 * <ul>
 * <li>{@code controller} —
 * {@link org.adultofuncional.main.agenda.infrastructure.controller.EventController}</li>
 * <li>{@code persistence} — Entidades JPA, repositorios Spring Data y
 * mapeadores</li>
 * <li>{@code repository} — Adaptador concreto del puerto
 * {@code EventRepository}</li>
 * </ul>
 *
 * @author Lidys Jaraba, Daniel Salazar, Juan Sebastian Rios, Jeronimo Ospina
 *         Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain
 * @see org.adultofuncional.main.agenda.application
 */
package org.adultofuncional.main.agenda.infrastructure;
