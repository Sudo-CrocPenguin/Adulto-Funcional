/**
 * Componentes transversales compartidos en toda la aplicación.
 *
 * <p>
 * Contiene contratos y adaptadores reutilizables que no pertenecen a un
 * módulo de negocio específico.
 *
 * <h2>Paquetes</h2>
 * <ul>
 * <li>{@code exception}: catálogo, excepciones y manejador HTTP global.</li>
 * <li>{@code normalization}: normalización canónica de texto.</li>
 * <li>{@code observability}: generación y propagación de {@code traceId}.</li>
 * <li>{@code pagination}: consulta, resultado y metadatos paginados.</li>
 * <li>{@code ratelimit}: políticas y puertos de limitación de abuso.</li>
 * <li>{@code response}: sobres JSON de éxito y error.</li>
 * <li>{@code security}: validaciones y reglas transversales de ownership.</li>
 * <li>{@code validation}: validadores Bean Validation personalizados.</li>
 * <li>{@code web}: filtros y utilidades HTTP comunes.</li>
 * </ul>
 *
 * <p>
 * Las excepciones de negocio y de infraestructura HTTP se traducen
 * centralizadamente mediante
 * {@link org.adultofuncional.main.shared.exception.GlobalExceptionHandler}.
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.shared;
