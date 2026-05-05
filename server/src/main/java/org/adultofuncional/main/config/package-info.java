/**
 * Configuraciones de Spring Boot para la aplicación.
 *
 * <p>
 * Centraliza las clases de configuración que definen beans de infraestructura,
 * seguridad, serialización JSON y otros aspectos transversales. Los beans
 * declarados aquí son consumidos por los módulos de negocio sin que estos
 * tengan que preocuparse por su instanciación.
 *
 * <h3>Subpaquetes</h3>
 * <ul>
 * <li>{@code beans} — Configuración de beans generales.
 * {@link org.adultofuncional.main.config.beans.AppConfig} expone el
 * {@code PasswordEncoder} (Argon2).</li>
 * <li>{@code jackson} — Configuración de serialización/deserialización
 * JSON con Jackson (módulo en preparación).</li>
 * <li>{@code security} — Configuración completa de Spring Security:
 * cadena de filtros, JWT, cookies HttpOnly, CORS y headers OWASP.
 * Ver {@link org.adultofuncional.main.config.security.SecurityConfig}
 * y las clases auxiliares que la acompañan.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.config;
