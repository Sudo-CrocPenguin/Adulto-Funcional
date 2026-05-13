package org.adultofuncional.main.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Propiedades de configuración para la generación y validación de JWT.
 *
 * <p>
 * Vincula automáticamente las propiedades con prefijo {@code jwt} desde
 * cualquier fuente de configuración de Spring Boot (archivos YAML, variables
 * de entorno, propiedades del sistema, etc.). La vinculación relajada
 * (<i>relaxed binding</i>) permite que tanto {@code jwt.secret} /
 * {@code jwt.expiration} (formato canónico en YAML) como
 * {@code JWT_SECRET} / {@code JWT_EXPIRATION} (formato de variable de entorno)
 * se mapeen correctamente a los campos de esta clase sin necesidad de
 * anotaciones {@code @Value} adicionales.
 *
 * <p>
 * <strong>Uso en perfiles de entorno:</strong>
 * <ul>
 * <li><b>Desarrollo ({@code dev})</b> — el archivo
 * {@code application-dev.yml} define {@code jwt.secret} y
 * {@code jwt.expiration} con valores fijos para el entorno local.</li>
 * <li><b>Producción ({@code prod})</b> — el archivo
 * {@code application-prod.yml} utiliza placeholders
 * {@code ${JWT_SECRET}} y {@code ${JWT_EXPIRATION}} que se resuelven
 * mediante variables de entorno inyectadas en el contenedor Docker.</li>
 * </ul>
 *
 * <p>
 * <strong>Validación de seguridad:</strong> la longitud mínima del secreto
 * (32 caracteres para HS256) se verifica en {@link JwtService} al construir
 * la clave de firma. Esta clase solo transporta los valores sin aplicar
 * reglas de negocio.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see JwtService
 * @see org.adultofuncional.main.config.security.SecurityConfig
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
  /**
   * Clave secreta para firmar los tokens JWT con HMAC-SHA256 (HS256).
   * Debe tener al menos 32 caracteres para garantizar una clave de 256 bits.
   * <p>
   * En desarrollo se toma de {@code application-dev.yml}; en producción
   * se inyecta mediante la variable de entorno {@code JWT_SECRET}.
   */
  private String secret;

  /**
   * Tiempo de vida del token JWT en milisegundos.
   * <p>
   * Valor típico: {@code 86400000} (24 horas). Configurable por entorno
   * mediante {@code jwt.expiration} (YAML) o {@code JWT_EXPIRATION} (env).
   */
  private long expiration;
}
