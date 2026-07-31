package org.adultofuncional.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;

/**
 * Test de integración que verifica el arranque del contexto de Spring Boot.
 *
 * <p>
 * Reutiliza el contenedor MariaDB compartido de la suite y confirma que todos
 * los beans se instancian correctamente sobre el esquema creado por Flyway.
 * </p>
 *
 * <p>
 * Características:
 * <ul>
 * <li>MariaDB 11.8 en contenedor Docker</li>
 * <li>Un único contenedor por JVM de pruebas</li>
 * <li>Migraciones Flyway V1 hasta la última versión</li>
 * </ul>
 * </p>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
@SpringBootTest
class AdultoFuncionalServerApplicationTests extends MariaDbIntegrationTestSupport {

  /**
   * Verifica que el contexto de Spring Boot se carga sin errores.
   *
   * @author Equipo de desarrollo Adulto Funcional
   */

  @Test
  void contextLoads() {
  }
}
