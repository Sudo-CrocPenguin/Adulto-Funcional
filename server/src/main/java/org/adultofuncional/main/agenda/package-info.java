/**
 * Módulo de agenda personal.
 *
 * <p>
 * Implementa la funcionalidad completa de eventos bajo los principios de
 * Clean Architecture, distribuyendo las responsabilidades en las capas de
 * dominio, aplicación e infraestructura.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Creación de eventos con prioridad, estado y recordatorio.</li>
 * <li>Actualización parcial de eventos (PATCH).</li>
 * <li>Consulta individual y listado de eventos por cuenta con filtros.</li>
 * <li>Eliminación de eventos con validación de propiedad.</li>
 * <li>Soporte para recurrencia mediante frecuencia en días.</li>
 * <li>Asociación obligatoria a una cuenta y una categoría.</li>
 * <li>Protección contra Stored XSS mediante {@code @NoHtml} en los DTOs de
 * entrada.</li>
 * </ul>
 *
 * <h2>Estructura</h2>
 * 
 * <pre>
 * agenda/
 * ├── domain/            → Modelo {@code
 * Event
 * } y puerto {@code
 * EventRepository
 * }
 * ├── application/       → Casos de uso ({@code
 * CreateEventUseCase
 * },
 * │                         {@code
 * GetEventUseCase
 * }, …) y DTOs
 * └── infrastructure/    → Controlador REST, entidad JPA, mapeador y
 *                           adaptador de repositorio
 * </pre>
 *
 * <h2>Seguridad</h2>
 * <ul>
 * <li>Todos los endpoints requieren autenticación JWT.</li>
 * <li>La validación de propiedad del evento se ejecuta antes de cualquier
 * operación (consulta, actualización, eliminación).</li>
 * <li>Los campos de texto libre se validan con {@code @NoHtml} para rechazar
 * HTML/scripts maliciosos.</li>
 * </ul>
 *
 * <h2>Tabla asociada</h2>
 * {@code events} — identificador UUID v7, FK a {@code accounts} y
 * {@code categories}.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain
 * @see org.adultofuncional.main.agenda.application
 * @see org.adultofuncional.main.agenda.infrastructure
 */
package org.adultofuncional.main.agenda;
