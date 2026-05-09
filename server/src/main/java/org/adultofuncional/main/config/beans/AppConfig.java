package org.adultofuncional.main.config.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de beans generales de la aplicación.
 *
 * <p>
 * Centraliza los beans de infraestructura que no pertenecen a un módulo
 * concreto y que son necesarios en varios contextos. Actualmente proporciona
 * el {@link PasswordEncoder} para el hashing de contraseñas con Argon2.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Configuration
public class AppConfig {

  /**
   * Provee un encoder de contraseñas basado en Argon2.
   *
   * <p>
   * Utiliza los parámetros predeterminados de Spring Security 5.8, que
   * equilibran seguridad y rendimiento para la mayoría de los casos de uso.
   * Este bean se inyecta en los casos de uso de autenticación
   * ({@code LoginUseCase}, {@code RegisterUseCase}) y en cualquier otro
   * componente que requiera verificar o generar hashes.
   *
   * @return instancia de {@link Argon2PasswordEncoder}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }
}
