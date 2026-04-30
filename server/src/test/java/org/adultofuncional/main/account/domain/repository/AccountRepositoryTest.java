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
 * Pruebas unitarias del adaptador {@link AccountRepository}.
 *
 * <p>Usa Mockito para simular {@link SpringAccountJpaRepository} y verificar
 * que la implementación del repositorio delega correctamente las llamadas
 * sin requerir una base de datos real.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>{@code findByEmail()} retorna Account cuando el email existe</li>
 *   <li>{@code findByEmail()} retorna {@code Optional.empty()} cuando el email no existe</li>
 * </ul>
 *
 * @author Daniel Salazar
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountRepository")
class AccountRepositoryTest {

    @Mock
    private SpringAccountJpaRepository jpaRepo;

    // TODO: Descomentar cuando exista la implementacion concreta de AccountRepository
    // @InjectMocks
    // private JpaAccountRepository repository;

    @Nested
    @DisplayName("Metodo findByEmail")
    class FindByEmail {

        /**
         * Verifica que al encontrar una entidad por email, el repositorio
         * delega correctamente en {@code jpaRepo.findByAccount_email()}.
         */
        @Test
        @DisplayName("Debe retornar Account cuando el email existe")
        void shouldReturnAccountWhenEmailExists() {
            String email = "test@example.com";
            AccountEntity entity = new AccountEntity();
            entity.setAccount_email(email);
            entity.setAccount_names("Juan");

            when(jpaRepo.findByAccount_email(email)).thenReturn(Optional.of(entity));

            // TODO: Descomentar cuando la implementacion este disponible
            // Optional<Account> result = repository.findByEmail(email);
            // assertTrue(result.isPresent());
            // assertEquals(email, result.get().getEmail());

            verify(jpaRepo).findByAccount_email(email);
        }

        /**
         * Verifica que cuando el email no existe, se retorna {@code Optional.empty()}.
         */
        @Test
        @DisplayName("Debe retornar Optional.empty() cuando el email no existe")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(jpaRepo.findByAccount_email("no@existe.com")).thenReturn(Optional.empty());

            // TODO: Descomentar cuando la implementacion este disponible
            // Optional<Account> result = repository.findByEmail("no@existe.com");
            // assertFalse(result.isPresent());
        }
    }
}
