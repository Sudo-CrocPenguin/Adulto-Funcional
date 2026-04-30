package org.adultofuncional.main.account.application.usecase;

import org.adultofuncional.main.account.application.dto.AccountResponse;
import org.adultofuncional.main.account.application.dto.UpdateAccountRequest;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link UpdateAccountUseCase}.
 *
 * <p><strong>¿Qué se prueba?</strong><br>
 * El comportamiento del caso de uso que actualiza los datos de una cuenta.
 *
 * <p><strong>Escenarios cubiertos:</strong>
 * <ul>
 *   <li><strong>Caso feliz 1:</strong> Actualización con email nuevo y disponible → éxito.</li>
 *   <li><strong>Caso feliz 2:</strong> Actualización sin cambiar email → éxito sin verificar unicidad.</li>
 *   <li><strong>Caso error 1:</strong> La cuenta NO existe → {@link NotFoundException}.</li>
 *   <li><strong>Caso error 2:</strong> Email nuevo ya existe en otra cuenta → {@link BusinessException}.</li>
 * </ul>
 *
 * <p><strong>¿Cómo se prueba?</strong><br>
 * Se utiliza Mockito para simular el repositorio y ArgumentCaptor para verificar
 * que los datos modificados son correctos antes de guardar.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para UpdateAccountUseCase")
class UpdateAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UpdateAccountUseCase updateAccountUseCase;

    private UUID accountId;
    private AccountEntity existingEntity;
    private UpdateAccountRequest validRequest;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();

        // Entidad existente (antes de actualizar)
        existingEntity = new AccountEntity();
        existingEntity.setAccount_id(accountId);
        existingEntity.setAccount_names("Juan");
        existingEntity.setAccount_lastnames("Perez");
        existingEntity.setAccount_email("juan@example.com");
        existingEntity.setAccount_phone("123456789");
        existingEntity.setAccount_created_at(LocalDateTime.now().minusDays(10));

        // Solicitud de actualización con datos nuevos
        validRequest = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .build();
    }

    /**
     * Prueba: Actualización exitosa con email nuevo y disponible.
     *
     * //TODO: Agregar prueba para verificar que se mantiene la fecha de creación original
     */
    @Test
    @DisplayName("Debería actualizar y retornar AccountResponse cuando la cuenta existe y el email es nuevo")
    void shouldUpdateAndReturnAccountResponseWhenAccountExistsAndEmailIsNew() {
        // Arrange
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        when(accountRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);

        // Capturar la entidad que se pasa al método save
        ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        when(accountRepository.save(entityCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountResponse response = updateAccountUseCase.execute(accountId, validRequest);

        // Assert: Verificar DTO de respuesta
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");

        // Assert: Verificar que la entidad fue modificada antes de guardar
        AccountEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getAccount_names()).isEqualTo("Miguel Angel");
        assertThat(savedEntity.getAccount_lastnames()).isEqualTo("Blandon Montes");
        assertThat(savedEntity.getAccount_email()).isEqualTo("miguel@example.com");
        assertThat(savedEntity.getAccount_phone()).isEqualTo("+573001234567");
        // La fecha de creación debe permanecer igual
        assertThat(savedEntity.getAccount_created_at()).isEqualTo(existingEntity.getAccount_created_at());

        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    /**
     * Prueba: La cuenta NO existe → lanza NotFoundException.
     */
    @Test
    @DisplayName("Debería lanzar NotFoundException cuando la cuenta no existe")
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        // Arrange
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> updateAccountUseCase.execute(accountId, validRequest));
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    /**
     * Prueba: Email nuevo ya existe en otra cuenta → lanza BusinessException.
     */
    @Test
    @DisplayName("Debería lanzar BusinessException cuando el nuevo email ya está registrado por otra cuenta")
    void shouldThrowBusinessExceptionWhenEmailAlreadyExistsForOtherAccount() {
        // Arrange
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        when(accountRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> updateAccountUseCase.execute(accountId, validRequest));
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    /**
     * Prueba: El email no cambia → no se verifica unicidad y se actualizan demás campos.
     */
    @Test
    @DisplayName("Debería actualizar sin verificar email si el email no ha cambiado")
    void shouldUpdateWithoutEmailUniquenessCheckWhenEmailIsSame() {
        // Arrange: Solicitud con el mismo email que la entidad
        UpdateAccountRequest requestWithSameEmail = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("juan@example.com")  // mismo que el existente
                .phone("+573001234567")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        // No se debe llamar a existsByEmail, por lo que NO lo configuramos

        ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        when(accountRepository.save(entityCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountResponse response = updateAccountUseCase.execute(accountId, requestWithSameEmail);

        // Assert
        assertThat(response.getEmail()).isEqualTo("juan@example.com"); // se mantiene
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getPhone()).isEqualTo("+573001234567");

        verify(accountRepository, times(1)).save(any(AccountEntity.class));
        verify(accountRepository, never()).existsByEmail(anyString());
    }
}