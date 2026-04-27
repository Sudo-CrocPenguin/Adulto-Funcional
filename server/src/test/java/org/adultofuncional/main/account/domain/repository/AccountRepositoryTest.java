package org.adultofuncional.main.account.domain.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

//import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prueba unitaria para la implementacion de {@link AccountRepository}.
 * <p>
 * Utiliza Mockito para simular el repositorio {@link SpringAccountJpaRepository} y asi
 * aislar la logica del adaptador. No requiere base de datos real.
 * <p>
 * Esta prueba permite:
 * <ul>
 *   <li>Simular la respuesta de {@code SpringAccountJpaRepository.findByAccount_email}.</li>
 *   <li>Verificar que el metodo {@code findByEmail} de la implementacion devuelve un modelo
 *       {@code Account} cuando el email existe.</li>
 *   <li>Verificar que retorna {@code Optional.empty()} cuando el email no existe.</li>
 *   <li>Validar el mapeo de {@code AccountEntity} a {@code Account} (modelo de dominio)
 *       sin depender de una base de datos relacional.</li>
 *   <li>Asegurar que el adaptador (implementacion de {@code AccountRepository}) delega
 *       correctamente al repositorio Spring Data.</li>
 *   <li>Detectar errores en la capa de infraestructura de forma rapida sin necesidad de
 *       levantar el contexto completo de Spring.</li>
 * </ul>
 *
 * @author daniel
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountRepository - Tests del adaptador JPA")
class AccountRepositoryTest {

    @Mock
    private SpringAccountJpaRepository jpaRepo;

    // Descomentar cuando exista la implementacion concreta de AccountRepository
    // @InjectMocks
    // private JpaAccountRepository repository;

    @Nested
    @DisplayName("Metodo findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("Debe retornar Account cuando el email existe")
        void shouldReturnAccountWhenEmailExists() {
            String email = "test@example.com";
            AccountEntity entity = new AccountEntity();
            entity.setAccount_email(email);
            entity.setAccount_names("Juan");

            when(jpaRepo.findByAccount_email(email)).thenReturn(Optional.of(entity));

            // Descomentar cuando la implementacion este disponible
            // Optional<Account> result = repository.findByEmail(email);
            // assertTrue(result.isPresent());
            // assertEquals(email, result.get().getEmail());

            verify(jpaRepo).findByAccount_email(email);
        }

        @Test
        @DisplayName("Debe retornar Optional.empty() cuando el email no existe")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(jpaRepo.findByAccount_email("no@existe.com")).thenReturn(Optional.empty());

            // Descomentar cuando la implementacion este disponible
            // Optional<Account> result = repository.findByEmail("no@existe.com");
            // assertFalse(result.isPresent());
        }
    }
}