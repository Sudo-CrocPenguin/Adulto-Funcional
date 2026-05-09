/**
 * Capa de dominio del módulo de agenda.
 *
 * <p>
 * Contiene el modelo de dominio que representa un evento y el puerto de
 * repositorio que desacopla el acceso a datos. Esta capa es completamente
 * independiente de la infraestructura externa (JPA, REST, controladores) y
 * encapsula las reglas de negocio fundamentales de los eventos.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code model} — Entidad del dominio:
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.domain.model.Event} —
 * Evento de la agenda personal con recurrencia, prioridad y estado.</li>
 * </ul>
 * </li>
 * <li>{@code repository} — Puerto de persistencia:
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.domain.repository.EventRepository}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li><strong>Identidad:</strong> El modelo genera su propio UUID v7 a través
 * del método de fábrica {@code create}, garantizando que el dominio sea dueño
 * de su identidad antes de la persistencia.</li>
 * <li><strong>Invariantes de negocio:</strong> Valida estrictamente sus campos
 * obligatorios, la coherencia de horas y la no negatividad de la frecuencia.
 * Las validaciones de formato y seguridad (XSS) corresponden a los DTOs de la
 * capa de aplicación.</li>
 * <li><strong>Separación de operaciones:</strong> El método
 * {@code reconstitute} permite reconstruir instancias desde la capa de
 * persistencia sin revalidar invariantes.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata, Daniel Salazar
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain.model
 * @see org.adultofuncional.main.agenda.domain.repository
 */
package org.adultofuncional.main.agenda.domain;
