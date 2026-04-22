package org.adultofuncional.main;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Tests de integración para el contexto de Spring Boot.
 * 
 * <p>
 * Verifica que el contexto de la aplicación Spring Boot cargue correctamente
 * con todas sus dependencias. Utiliza Testcontainers para levantar una
 * instancia real de MariaDB en un contenedor Docker, asegurando que las
 * configuraciones de base de datos sean compatibles con el entorno de
 * producción.
 * 
 * <p>
 * Características del test:
 * <ul>
 * <li>Levanta un contenedor MariaDB 11.8 para las pruebas</li>
 * <li>Configura dinámicamente las propiedades de DataSource</li>
 * <li>Deshabilita Flyway para pruebas rápidas (opcional)</li>
 * <li>Verifica que el contexto de Spring pueda inicializarse sin errores</li>
 * </ul>
 * 
 * @author adultofuncional
 * @since 0.0.1
 */
@Testcontainers
@SpringBootTest
class AdultoFuncionalServerApplicationTests {

  /**
   * Contenedor MariaDB para pruebas de integración.
   * 
   * <p>
   * Utiliza la imagen oficial de MariaDB 11.8. Se configura con:
   * <ul>
   * <li>Base de datos: testdb</li>
   * <li>Usuario: test</li>
   * <li>Contraseña: test</li>
   * </ul>
   * 
   * <p>
   * El contenedor se inicia automáticamente antes de ejecutar los tests
   * gracias a la anotación {@code @Testcontainers}.
   */
  @Container
  static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.8")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  /**
   * Configura dinámicamente las propiedades de Spring Boot para usar
   * el contenedor MariaDB en lugar de la base de datos de producción.
   * 
   * <p>
   * Las propiedades sobrescritas son:
   * <ul>
   * <li>spring.datasource.url - URL de conexión al contenedor MariaDB</li>
   * <li>spring.datasource.username - Usuario de la base de datos de prueba</li>
   * <li>spring.datasource.password - Contraseña de la base de datos de
   * prueba</li>
   * <li>spring.flyway.enabled - Deshabilitado para pruebas rápidas</li>
   * </ul>
   * 
   * @param registry Registro de propiedades dinámicas de Spring Boot
   */
  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mariadb::getJdbcUrl);
    registry.add("spring.datasource.username", mariadb::getUsername);
    registry.add("spring.datasource.password", mariadb::getPassword);
    registry.add("spring.flyway.enabled", () -> false);
  }

  /**
   * Verifica que el contexto de Spring Boot cargue correctamente.
   * 
   * <p>
   * Este test es fundamental porque valida que:
   * <ul>
   * <li>Todos los beans de Spring se puedan instanciar</li>
   * <li>Las configuraciones de base de datos sean correctas</li>
   * <li>Las dependencias entre componentes estén bien definidas</li>
   * <li>No haya conflictos en el classpath</li>
   * </ul>
   * 
   * <p>
   * Si este test falla, indica un problema estructural en la configuración
   * de Spring Boot que debe corregirse antes de continuar.
   */
  @Test
  void contextLoads() {
    // El test pasa automáticamente si el contexto de Spring se carga sin errores
  }
}
