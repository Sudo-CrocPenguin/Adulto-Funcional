package org.adultofuncional.main.config.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Configuración principal de Spring Security.
 *
 * <p>
 * Define la cadena de filtros de seguridad, la política de sesiones, las reglas
 * de autorización, los headers de seguridad HTTP, la configuración CORS y el
 * registro del filtro JWT.
 *
 * <p>
 * <strong>Decisiones de diseño:</strong>
 * <ul>
 * <li><strong>CSRF deshabilitado</strong>: Esta es una API REST stateless
 * consumida por clientes web y nativos (móvil/desktop). Los clientes web
 * están protegidos por el atributo {@code SameSite} de la cookie HttpOnly.
 * Los clientes nativos se autentican con Bearer token en el header
 * {@code Authorization}, no con cookies. Si en el futuro se incorpora un
 * cliente web que no pueda garantizar {@code SameSite}, se debe reactivar
 * {@code CookieCsrfTokenRepository}.</li>
 * <li><strong>Sesiones stateless</strong>: No se crea ni consulta
 * {@code HttpSession}. El estado de autenticación vive exclusivamente en
 * el JWT firmado.</li>
 * <li><strong>CORS con credenciales</strong>: {@code allowCredentials(true)}
 * es necesario para que el navegador envíe la cookie HttpOnly en requests
 * cross-origin. Por ello {@code allowedOrigins} debe ser una lista
 * explícita — {@code *} no es compatible con credenciales.</li>
 * <li><strong>Headers de seguridad</strong>: Se configuran CSP,
 * X-Frame-Options,
 * X-XSS-Protection, X-Content-Type-Options y HSTS para reducir la superficie
 * de ataque ante XSS, clickjacking y sniffing de contenido.</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtAuthenticationFilter
 * @see CookieUtils
 * @see ClientTypeResolver
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * Filtro JWT personalizado que valida el token en cada request entrante.
   * Se ejecuta antes del filtro de autenticación estándar de Spring Security.
   */
  private final JwtAuthenticationFilter jwtAuthFilter;

  /**
   * Lista de orígenes permitidos para CORS, inyectada desde la variable de
   * entorno {@code CORS_ALLOWED_ORIGINS}. Debe contener URLs absolutas
   * (ej. {@code https://miaplicacion.com}). No se permite {@code *} porque
   * {@code allowCredentials} está activo.
   */
  @Value("${CORS_ALLOWED_ORIGINS}")
  private List<String> allowedOrigins;

  /**
   * Configura la cadena de filtros de seguridad HTTP.
   *
   * <p>
   * <strong>Rutas públicas:</strong>
   * <ul>
   * <li>{@code /api/auth/**} — login, registro y logout</li>
   * <li>{@code /actuator/health} — health check para Docker</li>
   * </ul>
   * Todas las demás rutas requieren un JWT válido en la cookie o en el
   * header {@code Authorization}.
   *
   * <p>
   * <strong>Headers de seguridad configurados:</strong>
   * <ul>
   * <li><strong>Content-Security-Policy</strong>: Restringe las fuentes de
   * scripts, estilos e imágenes a {@code 'self'}, bloqueando la ejecución
   * de scripts inyectados (XSS). {@code frame-ancestors 'none'} equivale
   * a {@code X-Frame-Options: DENY} en navegadores modernos.</li>
   * <li><strong>X-Frame-Options: DENY</strong>: Impide que la app sea
   * embebida en iframes, previniendo ataques de clickjacking.</li>
   * <li><strong>X-XSS-Protection</strong>: Activa el filtro XSS del navegador
   * en modo bloqueo. Compatibilidad legacy para navegadores sin soporte CSP.</li>
   * <li><strong>X-Content-Type-Options: nosniff</strong>: Evita que el
   * navegador infiera el tipo MIME de una respuesta, previniendo ataques
   * de MIME sniffing.</li>
   * <li><strong>HSTS</strong>: Fuerza HTTPS durante un año en el dominio y
   * subdominios. Solo efectivo si la app se sirve bajo HTTPS.</li>
   * </ul>
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
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self'; " +
                        "img-src 'self' data:; " +
                        "object-src 'none'; " +
                        "frame-ancestors 'none';"))
            .frameOptions(frame -> frame.deny())
            .xssProtection(xss -> xss
                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .contentTypeOptions(Customizer.withDefaults())
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)))
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
   * Requerido por {@code LoginUseCase} para delegar la verificación de
   * credenciales (email + contraseña Argon2) a Spring Security sin acoplar
   * el caso de uso a la infraestructura de seguridad directamente.
   *
   * @param config configuración de autenticación provista por Spring
   * @return instancia del {@link AuthenticationManager} activo
   * @throws Exception si ocurre un error al obtener el manager
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configura la política CORS para todas las rutas de la API ({@code /**}).
   *
   * <p>
   * <strong>Headers permitidos:</strong>
   * <ul>
   * <li>{@code Content-Type} — tipo de cuerpo del request (JSON)</li>
   * <li>{@code X-Requested-With} — identificador de requests AJAX</li>
   * <li>{@code X-Client-Type} — señal declarativa del tipo de cliente;
   * ver {@link ClientTypeResolver}</li>
   * <li>{@code Authorization} — Bearer token para clientes nativos</li>
   * </ul>
   *
   * <p>
   * <strong>Headers expuestos:</strong>
   * <ul>
   * <li>{@code X-Total-Count} — total de registros para respuestas paginadas</li>
   * </ul>
   *
   * @return fuente de configuración CORS registrada globalmente
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of(
        "Content-Type",
        "X-Requested-With",
        "X-Client-Type",
        "Authorization"));
    config.setExposedHeaders(List.of("X-Total-Count"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
