/**
 * Componentes transversales compartidos en toda la aplicación.
 *
 * <p>Contiene elementos de infraestructura común que no pertenecen
 * a un módulo de negocio específico.</p>
 *
 * <p>Paquetes:
 * <ul>
 *   <li>{@code constants} — Constantes globales del sistema (pendiente)</li>
 *   <li>{@code exception} — Jerarquía de excepciones de negocio
 *       y {@code GlobalExceptionHandler}</li>
 *   <li>{@code response} — Estructura estándar {@code ApiResponse}
 *       para todas las respuestas REST</li>
 *   <li>{@code util} — Clases de utilidad general (pendiente)</li>
 * </ul>
 * </p>
 *
 * <p>Todas las excepciones extienden {@link BusinessException}
 * y se manejan centralizadamente por {@link GlobalExceptionHandler}.</p>
 *
 * @author Equipo de desarrollo
 * @since 0.0.1
 */
package org.adultofuncional.main.shared;
