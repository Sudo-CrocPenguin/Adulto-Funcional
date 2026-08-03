package org.adultofuncional.main.config.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class ProductionSecurityConfigurationValidatorTest {

  private static final String JWT_SECRET = randomSecret(0);
  private static final String MASTER_KEY_SECRET = randomSecret(32);
  private static final String REDIS_SECRET = randomSecret(64);

  @Test
  void acceptsIndependentRandomBase64SecretsAndSecureCookies() {
    JwtProperties jwt = jwtWithSecret(JWT_SECRET);

    assertThatCode(() -> new ProductionSecurityConfigurationValidator(
        jwt,
        MASTER_KEY_SECRET,
        REDIS_SECRET,
        true)).doesNotThrowAnyException();
  }

  @Test
  void rejectsRepositoryPlaceholderEvenWhenItIsLong() {
    JwtProperties jwt = jwtWithSecret("change_me_jwt_secret_with_32_chars_minimum");

    assertThatThrownBy(() -> new ProductionSecurityConfigurationValidator(
        jwt,
        MASTER_KEY_SECRET,
        REDIS_SECRET,
        true))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("JWT_SECRET")
        .hasMessageContaining("valor de ejemplo");
  }

  @Test
  void rejectsPredictableBase64Material() {
    JwtProperties jwt = jwtWithSecret(Base64.getEncoder().encodeToString(new byte[32]));

    assertThatThrownBy(() -> new ProductionSecurityConfigurationValidator(
        jwt,
        MASTER_KEY_SECRET,
        REDIS_SECRET,
        true))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("32 bytes aleatorios");
  }

  @Test
  void rejectsInsecureCookiesInProduction() {
    JwtProperties jwt = jwtWithSecret(JWT_SECRET);

    assertThatThrownBy(() -> new ProductionSecurityConfigurationValidator(
        jwt,
        MASTER_KEY_SECRET,
        REDIS_SECRET,
        false))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("APP_COOKIE_SECURE");
  }

  private static JwtProperties jwtWithSecret(String secret) {
    JwtProperties properties = new JwtProperties();
    properties.setSecret(secret);
    return properties;
  }

  private static String randomSecret(int offset) {
    byte[] bytes = new byte[32];
    for (int index = 0; index < bytes.length; index++) {
      bytes[index] = (byte) (index + offset);
    }
    return Base64.getEncoder().encodeToString(bytes);
  }
}
