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
 * Pruebas unitarias para el caso de uso {@link UpdateAccountUseCase}.
 *
 * <p>Este conjunto de pruebas verifica:
 * <ul>
 *   <li>Actualización exitosa de todos los campos permitidos.</li>
 *   <li>Lanzamiento de {@link NotFoundException} si la cuenta no existe.</li>
 *   <li>Lanzamiento de {@link BusinessException} si el nuevo email ya está en uso por otra cuenta.</li>
 *   <li>Que el repositorio guarda la entidad modificada.</li>
 * </ul>
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
     * Prueba la actualización exitosa de una cuenta existente.
     */
    @Test
    @DisplayName("Debería actualizar y retornar AccountResponse cuando la cuenta existe y el email es nuevo")
    void shouldUpdateAndReturnAccountResponseWhenAccountExistsAndEmailIsNew() {
        // Arrange
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        when(accountRepository.existsByEmail(validRequest.getEmail())).thenReturn(false); // email no usado

        // Capturar la entidad que se pasa al método save
        ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        when(accountRepository.save(entityCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountResponse response = updateAccountUseCase.execute(accountId, validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(accountId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");

        // Verificar que la entidad fue modificada antes de guardar
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
     * Prueba que se lanza NotFoundException si la cuenta no existe.
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
     * Prueba que se lanza BusinessException si el nuevo email ya está en uso por otra cuenta.
     */
    @Test
    @DisplayName("Debería lanzar BusinessException cuando el nuevo email ya está registrado por otra cuenta")
    void shouldThrowBusinessExceptionWhenEmailAlreadyExistsForOtherAccount() {
        // Arrange
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        // El nuevo email ya existe en otra cuenta (diferente a la actual)
        when(accountRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> updateAccountUseCase.execute(accountId, validRequest));

        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    /**
     * Prueba que cuando el email no cambia, no se verifica la unicidad y se actualizan los demás campos.
     */
    @Test
    @DisplayName("Debería actualizar sin verificar email si el email no ha cambiado")
    void shouldUpdateWithoutEmailUniquenessCheckWhenEmailIsSame() {
        // Arrange
        // Modificamos la solicitud para que tenga el mismo email que la entidad existente
        UpdateAccountRequest requestWithSameEmail = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("juan@example.com")  // mismo email original
                .phone("+573001234567")
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingEntity));
        // No se debe llamar a existsByEmail, por lo que no lo configuramos (Mockito lanzaría error si se llama)

        ArgumentCaptor<AccountEntity> entityCaptor = ArgumentCaptor.forClass(AccountEntity.class);
        when(accountRepository.save(entityCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountResponse response = updateAccountUseCase.execute(accountId, requestWithSameEmail);

        // Assert
        assertThat(response.getEmail()).isEqualTo("juan@example.com"); // se mantiene
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getPhone()).isEqualTo("+573001234567");

        // Verificar que el repositorio guardó los cambios
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
        // Verificamos que nunca se preguntó por existencia del email
        verify(accountRepository, never()).existsByEmail(anyString());
    }
}