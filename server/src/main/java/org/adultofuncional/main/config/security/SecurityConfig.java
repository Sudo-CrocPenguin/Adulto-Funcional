package org.adultofuncional.main.config.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Configuración principal de Spring Security.
 *
 * <p>
 * Define la cadena de filtros de seguridad, la política de sesiones, las reglas
 * de autorización, la configuración CORS y el registro del filtro JWT.
 *
 * <p>
 * <strong>Decisiones de diseño:</strong>
 * <ul>
 * <li><strong>CSRF deshabilitado</strong>: Seguro porque el token se almacena
 * en una
 * cookie con {@code SameSite=Strict}, que el navegador no envía en requests
 * cross-site. Ver {@link CookieUtils}.</li>
 * <li><strong>Sesiones stateless</strong>: No se usa {@code HttpSession}. El
 * estado
 * de autenticación se mantiene exclusivamente en el JWT.</li>
 * <li><strong>CORS con credenciales</strong>: {@code allowCredentials(true)} es
 * necesario para que el navegador envíe y reciba cookies en requests
 * cross-origin.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtAuthenticationFilter
 * @see CookieUtils
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * Filtro JWT que se ejecuta antes del filtro de autenticación estándar de
   * Spring.
   */
  private final JwtAuthenticationFilter jwtAuthFilter;

  /**
   * Lista de orígenes permitidos para CORS.
   * Configurado via {@code CORS_ALLOWED_ORIGINS}
   */
  @Value("${CORS_ALLOWED_ORIGINS}")
  private List<String> allowedOrigins;

  /**
   * Configura la cadena de filtros de seguridad HTTP.
   *
   * <p>
   * Rutas públicas:
   * <ul>
   * <li>{@code /api/auth/**} — login, register y logout</li>
   * <li>{@code /actuator/health} — health check para Docker</li>
   * </ul>
   * Todas las demás rutas requieren autenticación válida via JWT.
   *
   * @param http builder de configuración de Spring Security
   * @return cadena de filtros configurada
   * @throws Exception si ocurre un error durante la configuración
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
            .anyRequest().authenticated())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Expone el {@link AuthenticationManager} como bean de Spring.
   *
   * <p>
   * Necesario para que los casos de uso de autenticación (login) puedan
   * delegar la verificación de credenciales a Spring Security.
   *
   * @param config configuración de autenticación de Spring
   * @return instancia del {@link AuthenticationManager}
   * @throws Exception si ocurre un error al obtener el manager
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configura la política CORS para la API.
   *
   * <p>
   * {@code allowCredentials(true)} es requerido para que el navegador incluya
   * las cookies en requests cross-origin. Por esto, {@code allowedOrigins} debe
   * ser una lista explícita de dominios — no se permite {@code *} con
   * credenciales.
   *
   * @return fuente de configuración CORS registrada para todas las rutas
   *         ({@code /**})
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type", "X-Requested-With"));
    config.setExposedHeaders(List.of());
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
