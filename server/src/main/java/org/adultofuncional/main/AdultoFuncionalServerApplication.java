package org.adultofuncional.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación adulto-funcional-server.
 *
 * <p>
 * Backend Spring Boot 3 construido con Clean Architecture para:
 * <ul>
 * <li>Gestión financiera personal (movimientos, categorías, gastos fijos)</li>
 * <li>Agenda de eventos con recordatorios y prioridades</li>
 * <li>Almacenamiento seguro de contraseñas (AES-256 + Master Key)</li>
 * </ul>
 *
 * <p>
 * Stack tecnológico:
 * <ul>
 * <li>Spring Boot 3.5.13 / Java 21</li>
 * <li>Spring Data JPA + Spring Security (JWT + Argon2)</li>
 * <li>MariaDB + Flyway para migraciones</li>
 * <li>UUID v7 como identificadores primarios</li>
 * <li>Lombok para reducción de boilerplate</li>
 * <li>Testcontainers para pruebas de integración</li>
 * <li>Spring Boot Actuator para health checks en Docker</li>
 * </ul>
 *
 * @author Jeronimo Ospina
 * @since 0.0.1
 */
@SpringBootApplication
public class AdultoFuncionalServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(AdultoFuncionalServerApplication.class, args);
  }

}
