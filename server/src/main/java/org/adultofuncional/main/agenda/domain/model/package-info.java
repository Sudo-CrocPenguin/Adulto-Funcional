/**
 * Modelos de dominio del módulo de agenda.
 *
 * <p>
 * Contiene la entidad
 * {@link org.adultofuncional.main.agenda.domain.model.Event}
 * que representa un evento de la agenda personal. Encapsula las invariantes de
 * negocio y es responsable de generar su identidad (UUID v7) a través de
 * métodos
 * de fábrica.
 *
 * <h2>Entidad incluida</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.agenda.domain.model.Event} —
 * Evento con título, descripción, prioridad, fecha, frecuencia, recordatorio,
 * horas de inicio y fin, estado, categoría y cuenta propietaria.</li>
 * </ul>
 *
 * <h2>Responsabilidades comunes</h2>
 * <ul>
 * <li><strong>Métodos de fábrica:</strong> {@code create} para nuevas entidades
 * (genera UUID v7) y {@code reconstitute} para reconstruir instancias desde la
 * capa de persistencia.</li>
 * <li><strong>Validación de invariantes:</strong> Valida que los campos
 * obligatorios no sean nulos, que las fechas sean coherentes y que la
 * frecuencia
 * no sea negativa.</li>
 * <li><strong>Método de actualización:</strong> Expone un único método
 * {@code update} que modifica todos los campos editables de una vez,
 * reaplicando las invariantes.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.domain.model.Event
 */
package org.adultofuncional.main.agenda.domain.model;
