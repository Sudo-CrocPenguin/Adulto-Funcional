package org.adultofuncional.main.testsupport;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Infraestructura compartida para las pruebas de integración con MariaDB.
 *
 * <p><strong>Qué es:</strong> una clase base que mantiene un único contenedor
 * MariaDB durante toda la JVM de pruebas y publica su conexión en el contexto
 * de Spring.</p>
 *
 * <p><strong>Para qué sirve:</strong> evita crear y detener un contenedor por
 * clase, permite reutilizar el mismo esquema migrado por Flyway y elimina la
 * carrera de cierre entre Hibernate, Hikari y Testcontainers.</p>
 *
 * <p><strong>Cómo funciona:</strong> el contenedor se inicia una sola vez al
 * cargar esta clase. No se invoca {@code stop()}; Testcontainers y Ryuk se
 * encargan de retirarlo cuando finaliza la JVM. Las pruebas que heredan de
 * esta clase reciben las propiedades del {@code DataSource} mediante
 * {@link DynamicPropertySource}. El aislamiento de cada prueba se conserva
 * con las transacciones y rollback provistos por Spring Test.</p>
 */
@ActiveProfiles("test")
public abstract class MariaDbIntegrationTestSupport {

  private static final MariaDBContainer<?> MARIA_DB =
      new MariaDBContainer<>(
          DockerImageName.parse(
              "mariadb:11.8.8@sha256:efb4959ef2c835cd735dbc388eb9ad6aab0c78dd64febcd51bc17481111890c4")
              .asCompatibleSubstituteFor("mariadb"))
          .withDatabaseName("adulto_funcional_test")
          .withUsername("test")
          .withPassword("test");

  static {
    MARIA_DB.start();
  }

  /**
   * Registra la conexión del contenedor compartido en cada contexto Spring.
   *
   * @param registry registro dinámico de propiedades del contexto de pruebas
   */
  @DynamicPropertySource
  protected static void registerMariaDbProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MARIA_DB::getJdbcUrl);
    registry.add("spring.datasource.username", MARIA_DB::getUsername);
    registry.add("spring.datasource.password", MARIA_DB::getPassword);
  }
}
