package org.adultofuncional.main.config.security;

import java.util.Base64;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Impide iniciar producción con secretos de ejemplo o cookies degradadas.
 *
 * <p>Los secretos criptográficos deben contener al menos 32 bytes aleatorios
 * codificados en Base64. Esta forma hace comprobable la cantidad de material
 * secreto y evita aceptar frases conocidas que solo cumplen una longitud
 * mínima. La contraseña de Redis sigue la misma política porque protege las
 * Master Keys efímeras almacenadas por las instancias del servidor.</p>
 */
@Component
@Profile("prod")
public class ProductionSecurityConfigurationValidator {

  private static final int MINIMUM_SECRET_BYTES = 32;
  private static final int MINIMUM_DISTINCT_BYTES = 16;

  public ProductionSecurityConfigurationValidator(
      JwtProperties jwtProperties,
      @Value("${master-key.session.secret}") String masterKeySessionSecret,
      @Value("${spring.data.redis.password}") String redisPassword,
      @Value("${APP_COOKIE_SECURE}") boolean cookieSecure) {
    validateRandomBase64("JWT_SECRET", jwtProperties.getSecret());
    validateRandomBase64("MASTER_KEY_SESSION_SECRET", masterKeySessionSecret);
    validateRandomBase64("REDIS_PASSWORD", redisPassword);
    if (!cookieSecure) {
      throw new IllegalStateException(
          "APP_COOKIE_SECURE debe ser true en el perfil prod");
    }
  }

  private void validateRandomBase64(String name, String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(name + " es obligatorio en producción");
    }
    String normalized = value.strip();
    String lower = normalized.toLowerCase(Locale.ROOT);
    if (lower.contains("change_me")
        || lower.contains("changeme")
        || lower.contains("replace_me")
        || lower.contains("example")
        || lower.contains("tu_clave")) {
      throw new IllegalStateException(name + " no puede usar un valor de ejemplo");
    }

    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(normalized);
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException(
          name + " debe ser Base64 generado a partir de al menos 32 bytes aleatorios",
          exception);
    }
    if (decoded.length < MINIMUM_SECRET_BYTES
        || distinctByteCount(decoded) < MINIMUM_DISTINCT_BYTES) {
      throw new IllegalStateException(
          name + " debe contener al menos 32 bytes aleatorios en Base64");
    }
  }

  private long distinctByteCount(byte[] value) {
    boolean[] seen = new boolean[256];
    int distinct = 0;
    for (byte current : value) {
      int index = current & 0xff;
      if (!seen[index]) {
        seen[index] = true;
        distinct++;
      }
    }
    return distinct;
  }
}
