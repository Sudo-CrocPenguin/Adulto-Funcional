/**
 * Configuración de seguridad de la aplicación.
 *
 * <p>
 * Implementa la autenticación y autorización mediante Spring Security,
 * JWT (JSON Web Tokens) y filtros personalizados para peticiones stateless.
 *
 * <p>
 * Componentes principales:
 * <ul>
 * <li>{@link org.adultofuncional.main.config.security.SecurityConfig} - Configuración de la cadena de filtros</li>
 * <li>{@link org.adultofuncional.main.config.security.JwtService} - Generación y validación de JWT</li>
 * <li>{@link org.adultofuncional.main.config.security.JwtAuthenticationFilter} - Filtro de autenticación JWT</li>
 * <li>{@link org.adultofuncional.main.config.security.DatabaseUserDetailsService} - Carga de usuarios desde la base de datos</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
package org.adultofuncional.main.config.security;
