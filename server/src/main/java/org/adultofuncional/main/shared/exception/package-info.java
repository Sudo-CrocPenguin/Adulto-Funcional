/**
 * Jerarquía de excepciones personalizadas de la aplicación.
 *
 * <p>Todas las excepciones de negocio extienden
 * {@link BusinessException}, lo que permite un manejo centralizado
 * en {@link GlobalExceptionHandler}.</p>
 *
 * <p>Excepciones disponibles:
 * <ul>
 *   <li>{@link BusinessException} — Base para errores de negocio (HTTP 400)</li>
 *   <li>{@link NotFoundException} — Recurso no encontrado (HTTP 404)</li>
 *   <li>{@link UnauthorizedException} — Acceso no autorizado (HTTP 401)</li>
 *   <li>{@link ConflictException} — Conflicto de datos (HTTP 409)</li>
 *   <li>{@link ForbiddenException} — Acceso denegado (HTTP 403)</li>
 * </ul>

 *
 * @since 0.0.1
 */
package org.adultofuncional.main.shared.exception;
