/**
 * Formato estándar de respuestas para toda la API.
 *
 * <p>
 * Define la clase {@link org.adultofuncional.main.shared.response.ApiResponse}
 * que conserva el formato de éxito y añade códigos estables, errores de campo
 * ordenados y trazabilidad a las respuestas fallidas. La creación de errores
 * se centraliza en
 * {@link org.adultofuncional.main.shared.response.ApiErrorFactory} y su
 * serialización fuera de MVC en
 * {@link org.adultofuncional.main.shared.response.ApiErrorResponseWriter}.
 *
 * @see org.adultofuncional.main.shared.response.ApiResponse
 * @see org.adultofuncional.main.shared.response.ApiErrorCode
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
package org.adultofuncional.main.shared.response;
