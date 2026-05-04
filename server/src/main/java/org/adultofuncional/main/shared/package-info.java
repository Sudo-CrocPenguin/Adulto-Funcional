/**
 * Componentes transversales compartidos en toda la aplicación.
 *
 * <p>
 * Contiene elementos de infraestructura común que no pertenecen
 * a un módulo de negocio específico.
 *
 * 
 * <p>
 * Paquetes:
 * <ul>
 * <li>{@code constants} — Constantes globales del sistema</li>
 * <li>{@code exception} — Jerarquía de excepciones de negocio
 * y {@code GlobalExceptionHandler}</li>
 * <li>{@code response} — Estructura estándar {@code ApiResponse}
 * para todas las respuestas REST</li>
 * <li>{@code util} — Clases de utilidad general (pendiente)</li>
 * </ul>
 *
 * 
 * <p>
 * Todas las excepciones extienden
 * {@link org.adultofuncional.main.shared.exception.BusinessException}
 * y se manejan centralizadamente por
 * {@link org.adultofuncional.main.shared.exception.GlobalExceptionHandler}.
 *
 * 
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.shared;
