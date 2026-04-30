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
 * Pruebas de integración del repositorio {@link SpringAccountJpaRepository}.
 *
 * <p>Usa {@code @DataJpaTest} con una base de datos en memoria (H2) para
 * ejecutar consultas reales y validar:
 * <ul>
 *   <li>Mapeo JPA de {@link AccountEntity} contra el schema de MariaDB</li>
 *   <li>Consulta derivada {@code findByAccount_email()}</li>
 *   <li>Operaciones CRUD heredadas de {@code JpaRepository}</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 */
@DisplayName("SpringAccountJpaRepository")
@DataJpaTest
class SpringAccountJpaRepositoryTest {

    /** Permite persistir entidades directamente sin usar el repositorio bajo prueba. */
    @Autowired
    private TestEntityManager entityManager;

    /** Repositorio que se está probando. */
    @Autowired
    private SpringAccountJpaRepository repository;

    /** Entidad de prueba configurada antes de cada test. */
    private AccountEntity accountEntity;

    /**
     * Prepara una cuenta de ejemplo con datos fijos.
     * No se persiste automáticamente para que cada test controle el estado inicial.
     */
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
    @DisplayName("findByAccount_email")
    class FindByAccountEmail {

        /**
         * Verifica que se encuentra la cuenta cuando el email existe en la BD.
         */
        @Test
        @DisplayName("Debe encontrar una cuenta por email existente")
        void shouldFindByExistingEmail() {
            entityManager.persistAndFlush(accountEntity);
            Optional<AccountEntity> result = repository.findByAccount_email("maria@email.com");

            assertTrue(result.isPresent());
            assertEquals("Maria", result.get().getAccount_names());
            assertEquals("maria@email.com", result.get().getAccount_email());
        }

        /**
         * Verifica que se retorna {@code Optional.empty()} cuando el email no está registrado.
         */
        @Test
        @DisplayName("Debe retornar Optional.empty() para email inexistente")
        void shouldReturnEmptyForNonExistingEmail() {
            Optional<AccountEntity> result = repository.findByAccount_email("no@existe.com");
            assertFalse(result.isPresent());
        }

        /**
         * Verifica que la búsqueda por email distingue mayúsculas y minúsculas.
         */
        @Test
        @DisplayName("Debe ser case-sensitive")
        void shouldBeCaseSensitive() {
            entityManager.persistAndFlush(accountEntity);
            Optional<AccountEntity> result = repository.findByAccount_email("MARIA@email.com");
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("Operaciones heredadas de JpaRepository")
    class JpaRepositoryOperations {

        /**
         * Verifica que se puede guardar una entidad y recuperarla por su UUID.
         */
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
