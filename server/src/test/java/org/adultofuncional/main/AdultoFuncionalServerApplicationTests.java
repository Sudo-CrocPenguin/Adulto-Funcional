package org.adultofuncional.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test de integración que verifica el arranque del contexto de Spring Boot.
 *
 * <p>
 * Levanta un contenedor Docker con MariaDB 11.8 mediante Testcontainers
 * y configura dinámicamente las propiedades del DataSource. Confirma que
 * todos los beans se instancian correctamente y que no hay conflictos de
 * configuración.
 * </p>
 *
 * <p>
 * Características:
 * <ul>
 * <li>MariaDB 11.8 en contenedor Docker</li>
 * <li>Propiedades inyectadas vía {@link DynamicPropertySource}</li>
 * <li>Flyway deshabilitado para arranque rápido</li>
 * </ul>
 * </p>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
class AdultoFuncionalServerApplicationTests {

  /**
   * Contenedor MariaDB 11.8 para pruebas de integración.
   *
   * <p>
   * Configuración: base de datos {@code testdb}, usuario {@code test},
   * contraseña {@code test}.
   * </p>
   *
   * @author Equipo de desarrollo Adulto Funcional
   */

  @Container
  static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.8")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  /**
   * Inyecta las propiedades de conexión del contenedor en el contexto de Spring.
   *
   * @param registry registro de propiedades dinámicas
   *
   * @author Equipo de desarrollo Adulto Funcional
   */

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mariadb::getJdbcUrl);
    registry.add("spring.datasource.username", mariadb::getUsername);
    registry.add("spring.datasource.password", mariadb::getPassword);
  }

  /**
   * Verifica que el contexto de Spring Boot se carga sin errores.
   *
   * @author Equipo de desarrollo Adulto Funcional
   */

  @Test
  void contextLoads() {
  }
}
