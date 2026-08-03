package org.adultofuncional.main.config.beans;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableScheduling
public class AppConfig {

  /**
   * Reloj UTC compartido por reglas de expiración, auditoría y dominio.
   *
   * <p>Inyectar {@link Clock} evita llamadas dispersas a {@code now()} y
   * permite que las pruebas controlen de forma determinista el tiempo.</p>
   */
  @Bean
  public Clock applicationClock() {
    return Clock.systemUTC();
  }

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
