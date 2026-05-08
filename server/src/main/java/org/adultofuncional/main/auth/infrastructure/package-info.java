/**
 * Capa de infraestructura del módulo de autenticación.
 *
 * <p>
 * Expone los endpoints REST de acceso público para login, registro y logout,
 * delegando la lógica de negocio en los casos de uso correspondientes.
 * Actúa como adaptador entre el protocolo HTTP y la capa de aplicación,
 * sin contener reglas de negocio ni acceso directo a datos.
 *
 * <h2>Componentes</h2>
 * <ul>
 * <li>{@code controller} — {@code AuthController}
 * <ul>
 * <li>Procesa {@code POST /api/auth/login}, {@code POST /api/auth/register}
 * y {@code POST /api/auth/logout}.</li>
 * <li>Coordina la entrega del JWT:
 * <ul>
 * <li>Siempre lo establece en una cookie {@code HttpOnly} mediante
 * {@code CookieUtils}.</li>
 * <li>Para clientes nativos (móvil/escritorio), lo incluye también en el body
 * usando la detección de {@code ClientTypeResolver}.</li>
 * </ul>
 * </li>
 * <li>Todas las respuestas siguen el formato estándar {@code ApiResponse<T>},
 * incluyendo el endpoint de logout (204 No Content).</li>
 * <li>La validación de entrada se apoya en Jakarta Bean Validation; los DTOs
 * incorporan {@code @NoHtml} para prevenir la inyección de scripts (Stored
 * XSS).</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>
 * Esta capa no accede directamente a repositorios ni a entidades JPA; se
 * mantiene
 * completamente desacoplada de la persistencia.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.auth.infrastructure;
