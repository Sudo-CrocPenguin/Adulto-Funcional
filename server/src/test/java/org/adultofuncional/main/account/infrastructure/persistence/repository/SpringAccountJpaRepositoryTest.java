package org.adultofuncional.main.account.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Prueba de integracion para {@link SpringAccountJpaRepository}.
 * <p>
 * Usa {@code @DataJpaTest} para ejecutar consultas reales contra una base de datos
 * en memoria (H2) y verificar:
 * <ul>
 *   <li>El metodo personalizado {@code findByAccount_email}.</li>
 *   <li>Las operaciones heredadas de {@code JpaRepository} ({@code save} y {@code findById}).</li>
 * </ul>
 * <p>
 * Esta prueba permite:
 * <ul>
 *   <li>Validar el mapeo JPA de {@link AccountEntity}.</li>
 *   <li>Confirmar que la consulta {@code findByAccount_email} no contiene errores de sintaxis SQL.</li>
 *   <li>Detectar fallos tempranos en la capa de persistencia.</li>
 *   <li>Aislar la interaccion con la base de datos sin cargar servicios ni controladores.</li>
 * </ul>
 *
 * @author daniel
 * @since 0.0.1
 */
@DisplayName("SpringAccountJpaRepository - Pruebas de integracion del repositorio JPA")
@DataJpaTest
class SpringAccountJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringAccountJpaRepository repository;

    private AccountEntity accountEntity;

    @BeforeEach
    void setUp() {
        accountEntity = new AccountEntity();
        accountEntity.setAccount_id(UUID.randomUUID());
        accountEntity.setAccount_names("Maria");
        accountEntity.setAccount_lastnames("Gonzalez");
        accountEntity.setAccount_email("maria@email.com");
        accountEntity.setAccount_phone("555123456");
        accountEntity.setAccount_password("hashedPass");
        accountEntity.setAccount_master_key("master");
        accountEntity.setAccount_created_at(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Metodo findByAccount_email")
    class FindByAccountEmail {

        @Test
        @DisplayName("Debe encontrar una cuenta por email existente")
        void shouldFindByExistingEmail() {
            entityManager.persistAndFlush(accountEntity);

            Optional<AccountEntity> result = repository.findByAccount_email("maria@email.com");

            assertTrue(result.isPresent(), "Deberia existir una cuenta con ese email");
            assertEquals("Maria", result.get().getAccount_names());
            assertEquals("maria@email.com", result.get().getAccount_email());
        }

        @Test
        @DisplayName("Debe retornar Optional.empty() para email inexistente")
        void shouldReturnEmptyForNonExistingEmail() {
            Optional<AccountEntity> result = repository.findByAccount_email("no@existe.com");

            assertFalse(result.isPresent(), "No deberia encontrar ninguna cuenta");
        }

        @Test
        @DisplayName("Debe ser case-sensitive segun la configuracion de la columna")
        void shouldBeCaseSensitive() {
            entityManager.persistAndFlush(accountEntity);

            Optional<AccountEntity> result = repository.findByAccount_email("MARIA@email.com");

            assertFalse(result.isPresent(),
                    "No deberia encontrar porque el email difiere en mayusculas");
        }
    }

    @Nested
    @DisplayName("Operaciones heredadas de JpaRepository")
    class JpaRepositoryOperations {

        @Test
        @DisplayName("Debe guardar y recuperar una entidad por ID")
        void shouldSaveAndFindById() {
            AccountEntity saved = repository.save(accountEntity);

            Optional<AccountEntity> found = repository.findById(saved.getAccount_id());

            assertTrue(found.isPresent());
            assertEquals(saved.getAccount_email(), found.get().getAccount_email());
        }
    }
}