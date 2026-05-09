/**
 * Casos de uso del módulo de agenda para la gestión de eventos.
 *
 * <p>
 * Contiene los servicios de aplicación que orquestan las operaciones de
 * creación, consulta, actualización, eliminación y listado de eventos.
 * Cada caso de uso coordina el dominio
 * ({@link org.adultofuncional.main.agenda.domain.model.Event}) y los puertos
 * de repositorio correspondientes, validando reglas de negocio como la
 * existencia de la cuenta, la coherencia de las horas y la pertenencia del
 * evento a la cuenta indicada.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase}
 * —
 * Crea un nuevo evento. Valida que la cuenta y la categoría (si se proporciona)
 * existan, que la hora de inicio sea anterior a la de fin, y asigna valores por
 * defecto para prioridad y estado.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.GetEventUseCase}
 * —
 * Obtiene un evento por su UUID junto con la categoría asociada, verificando la
 * pertenencia a la cuenta.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase}
 * —
 * Actualiza parcialmente un evento. Aplica solo los campos proporcionados y
 * revalida la coherencia de horas y categoría.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.DeleteEventUseCase}
 * —
 * Elimina un evento verificando previamente su existencia y propiedad.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase}
 * —
 * Lista los eventos de una cuenta con filtros opcionales por estado, prioridad
 * y categoría. Carga las categorías en lote para evitar consultas N+1.</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code create}, {@code update},
 * {@code delete}) se ejecutan con {@code @Transactional} para garantizar
 * atomicidad.</li>
 * <li>La validación de propiedad del evento se aplica en cada caso de uso
 * mediante consultas por ID y cuenta.</li>
 * <li>Los DTOs de entrada aplican validaciones de formato y anti‑XSS antes
 * de alcanzar estos casos de uso.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.application.dto
 * @see org.adultofuncional.main.agenda.domain.model.Event
 * @see org.adultofuncional.main.agenda.domain.repository.EventRepository
 */
package org.adultofuncional.main.agenda.application.usecase;
