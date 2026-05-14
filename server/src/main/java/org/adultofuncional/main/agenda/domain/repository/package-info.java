/**
 * Puertos de repositorio (interfaces) del dominio de agenda.
 *
 * <p>
 * Define el contrato de persistencia que los casos de uso requieren para
 * interactuar con los eventos sin conocer los detalles de almacenamiento.
 * La interfaz expone las operaciones mínimas necesarias y es implementada
 * en la capa de infraestructura mediante adaptadores JPA.
 *
 * <h2>Puerto incluido</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.domain.repository.EventRepository}
 * —
 * Acceso a {@link org.adultofuncional.main.agenda.domain.model.Event}.
 * Incluye búsqueda por ID, búsqueda por ID y cuenta (validación de
 * propiedad), verificación de existencia, listado por cuenta y operaciones
 * CRUD.</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 1.0
 * @see org.adultofuncional.main.agenda.domain.model.Event
 * @see org.adultofuncional.main.agenda.infrastructure.repository.EventRepositoryImpl
 */
package org.adultofuncional.main.agenda.domain.repository;
