/**
 * Configuración de seguridad de la aplicación.
 *
 * <p>
 * Contiene toda la infraestructura de autenticación y autorización basada en
 * Spring Security, JWT y cookies HttpOnly. Configura la cadena de filtros
 * stateless, los headers de protección OWASP, la política CORS y los servicios
 * de generación/validación de tokens.
 *
 * <h2>Componentes principales</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.config.security.SecurityConfig} —
 * Cadena de filtros, reglas de autorización, headers de seguridad
 * (CSP, HSTS, X-Frame-Options, etc.) y configuración CORS.</li>
 * <li>{@link org.adultofuncional.main.config.security.JwtProperties} —
 * Propiedades de configuración para JWT (secreto y expiración), vinculadas
 * automáticamente desde {@code application.yml} o variables de entorno
 * mediante {@code @ConfigurationProperties(prefix = "jwt")}.</li>
 * <li>{@link org.adultofuncional.main.config.security.JwtService} —
 * Generación, firma (HMAC-SHA256) y validación de JWT. Utiliza
 * {@code JwtProperties} para obtener la clave y el tiempo de expiración.</li>
 * <li>{@link org.adultofuncional.main.config.security.JwtAuthenticationFilter}
 * —
 * Filtro que extrae el token del header {@code Authorization} o de la
 * cookie {@code token}, lo valida y establece el contexto de seguridad.</li>
 * <li>{@link org.adultofuncional.main.config.security.CookieUtils} —
 * Gestión de la cookie {@code token} con atributos {@code HttpOnly},
 * {@code Secure} y {@code SameSite}.</li>
 * <li>{@link org.adultofuncional.main.config.security.ClientTypeResolver} —
 * Detecta si el cliente es nativo (móvil/desktop) o navegador web
 * para decidir si el token JWT se incluye en el body de la respuesta.</li>
 * <li>{@link org.adultofuncional.main.config.security.DatabaseUserDetailsService}
 * —
 * Carga credenciales del usuario desde la base de datos para Spring
 * Security.</li>
 * </ul>
 *
 * <h2>Flujo de autenticación</h2>
 * <ol>
 * <li>El usuario hace login → {@code AuthController} →
 * {@code LoginUseCase}.</li>
 * <li>Se genera un JWT con {@code JwtService}.</li>
 * <li>El token se guarda en cookie HttpOnly con {@code CookieUtils}.</li>
 * <li>Si el cliente es nativo, también se envía en el body.</li>
 * <li>En cada request, {@code JwtAuthenticationFilter} extrae y valida el
 * token.</li>
 * </ol>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.config.security;
