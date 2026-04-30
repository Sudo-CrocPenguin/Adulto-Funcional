package org.adultofuncional.main.account.application.usecase;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para el caso de uso {@link GetAccountUseCase}.
 *
 * <p>Este conjunto de pruebas verifica que:
 * <ul>
 *   <li>Cuando la cuenta existe, se retorna un {@link AccountResponse} con los datos correctos.</li>
 *   <li>Cuando la cuenta no existe, se lanza {@link NotFoundException}.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para GetAccountUseCase")
class GetAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAccountUseCase getAccountUseCase;

    private UUID existingAccountId;
    private UUID nonExistentAccountId;
    private AccountEntity mockEntity;

    @BeforeEach
    void setUp() {
        existingAccountId = UUID.randomUUID();
        nonExistentAccountId = UUID.randomUUID();

        // Construir una entidad simulada con datos de ejemplo
        mockEntity = new AccountEntity();
        mockEntity.setAccount_id(existingAccountId);
        mockEntity.setAccount_names("Miguel Angel");
        mockEntity.setAccount_lastnames("Blandon Montes");
        mockEntity.setAccount_email("miguel@example.com");
        mockEntity.setAccount_phone("+573001234567");
        mockEntity.setAccount_created_at(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    /**
     * Prueba que cuando la cuenta existe, se retorna un AccountResponse con los datos correctos.
     */
    @Test
    @DisplayName("Debería retornar AccountResponse cuando la cuenta existe")
    void shouldReturnAccountResponseWhenAccountExists() {
        // Arrange
        when(accountRepository.findById(existingAccountId)).thenReturn(Optional.of(mockEntity));

        // Act
        AccountResponse response = getAccountUseCase.execute(existingAccountId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(existingAccountId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    /**
     * Prueba que cuando la cuenta no existe, se lanza NotFoundException.
     */
    @Test
    @DisplayName("Debería lanzar NotFoundException cuando la cuenta no existe")
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        // Arrange
        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> getAccountUseCase.execute(nonExistentAccountId));
    }
}