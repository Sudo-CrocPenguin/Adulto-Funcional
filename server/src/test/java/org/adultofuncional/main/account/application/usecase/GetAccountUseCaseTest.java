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
 * Pruebas unitarias para {@link GetAccountUseCase}.
 *
 * <p><strong>¿Qué se prueba?</strong><br>
 * El comportamiento del caso de uso que consulta una cuenta por ID.
 *
 * <p><strong>Escenarios cubiertos:</strong>
 * <ul>
 *   <li><strong>Caso feliz:</strong> La cuenta existe → se retorna {@link AccountResponse} correctamente.</li>
 *   <li><strong>Caso error:</strong> La cuenta NO existe → se lanza {@link NotFoundException}.</li>
 * </ul>
 *
 * <p><strong>¿Cómo se prueba?</strong><br>
 * Se utiliza Mockito para simular el comportamiento del repositorio y verificar
 * que el caso de uso reacciona correctamente ante diferentes situaciones.
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

        // Construir entidad simulada con datos de ejemplo
        mockEntity = new AccountEntity();
        mockEntity.setAccount_id(existingAccountId);
        mockEntity.setAccount_names("Miguel Angel");
        mockEntity.setAccount_lastnames("Blandon Montes");
        mockEntity.setAccount_email("miguel@example.com");
        mockEntity.setAccount_phone("+573001234567");
        mockEntity.setAccount_created_at(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    /**
     * Prueba: La cuenta existe → se retorna un AccountResponse con los datos correctos.
     *
     * //TODO: Agregar prueba para verificar que el mapeo excluye correctamente campos sensibles
     */
    @Test
    @DisplayName("Debería retornar AccountResponse cuando la cuenta existe")
    void shouldReturnAccountResponseWhenAccountExists() {
        // Arrange: Configurar el mock para que encuentre la cuenta
        when(accountRepository.findById(existingAccountId)).thenReturn(Optional.of(mockEntity));

        // Act: Ejecutar el caso de uso
        AccountResponse response = getAccountUseCase.execute(existingAccountId);

        // Assert: Verificar el resultado
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(existingAccountId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
    }

    /**
     * Prueba: La cuenta NO existe → se lanza NotFoundException.
     *
     * //TODO: Agregar prueba para verificar el mensaje exacto de la excepción
     */
    @Test
    @DisplayName("Debería lanzar NotFoundException cuando la cuenta no existe")
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        // Arrange: Configurar el mock para que NO encuentre la cuenta
        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // Act & Assert: Verificar que se lanza la excepción esperada
        assertThrows(NotFoundException.class, () -> getAccountUseCase.execute(nonExistentAccountId));
    }
}