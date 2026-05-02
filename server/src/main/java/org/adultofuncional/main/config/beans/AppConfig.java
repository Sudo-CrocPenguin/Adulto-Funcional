package org.adultofuncional.main.config.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de beans generales de la aplicación.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Configuration
public class AppConfig {

  /**
   * Bean de codificación de contraseñas usando Argon2.
   *
   * <p>
   * Argon2 es el algoritmo recomendado para hash de contraseñas.
   * Los parámetros usados son los valores por defecto de Spring Security,
   * balanceando seguridad y rendimiento.
   *
   * @return instancia de {@link Argon2PasswordEncoder}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }
}
