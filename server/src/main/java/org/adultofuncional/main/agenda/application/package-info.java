/**
 * Capa de aplicación del módulo de agenda.
 *
 * <p>
 * Orquesta los casos de uso y define los DTOs de entrada/salida para la
 * gestión de eventos de la agenda. Los casos de uso coordinan el dominio y
 * los puertos de repositorio sin depender de la infraestructura externa
 * (JPA, REST), manteniendo la lógica de negocio desacoplada de los
 * controladores y de la capa de persistencia.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code dto} — Objetos de transferencia de datos:
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventRequest} —
 * Datos de entrada para creación.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventUpdateRequest}
 * — Datos de entrada para actualización parcial.</li>
 * <li>{@link org.adultofuncional.main.agenda.application.dto.EventResponse} —
 * Proyección de respuesta con categoría anidada.</li>
 * </ul>
 * </li>
 * <li>{@code usecase} — Casos de uso:
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.CreateEventUseCase}</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.GetEventUseCase}</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.UpdateEventUseCase}</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.DeleteEventUseCase}</li>
 * <li>{@link org.adultofuncional.main.agenda.application.usecase.ListEventsUseCase}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <p>
 * La validación anti‑XSS se aplica mediante la anotación personalizada
 * {@link org.adultofuncional.main.shared.security.NoHtml} en todos los
 * campos de texto libre de los DTOs de entrada (título y descripción).
 * Esto rechaza cualquier string que contenga HTML (basado en Jsoup con
 * {@code Safelist.none()}), previniendo el almacenamiento de scripts
 * maliciosos en la base de datos (Stored XSS).
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.shared.security.NoHtml
 * @see org.adultofuncional.main.agenda.application.dto
 * @see org.adultofuncional.main.agenda.application.usecase
 */
package org.adultofuncional.main.agenda.application;
