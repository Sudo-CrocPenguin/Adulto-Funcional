/**
 * Módulo de agenda y gestión de eventos.
 *
 * <p>
 * Permite a los usuarios gestionar sus eventos personales con
 * soporte para recordatorios, prioridades, estados y recurrencia.
 *
 * 
 * <p>
 * Características de eventos:
 * <ul>
 * <li>Prioridad: Baja, Media, Alta</li>
 * <li>Estado: Pendiente, Completado, Cancelado, Pospuesto</li>
 * <li>Recurrencia configurable en días (0 = único, 1 = diario, etc.)</li>
 * <li>Recordatorios con fecha y hora específica</li>
 * </ul>
 *
 * 
 * <p>
 * Tabla asociada: {@code events} con FK a {@code accounts}
 * y opcionalmente a {@code categories}.
 *
 * 
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.agenda;
