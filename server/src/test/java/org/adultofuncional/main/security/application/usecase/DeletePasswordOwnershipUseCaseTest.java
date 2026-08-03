package org.adultofuncional.main.security.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ForbiddenException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class DeletePasswordOwnershipUseCaseTest {

  @Test
  void doesNotDeleteCredentialOwnedByAnotherAccount() {
    PasswordRepository passwordRepository = mock(PasswordRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    MasterKeySessionService masterKeyService = mock(MasterKeySessionService.class);
    DeletePasswordUseCase useCase = new DeletePasswordUseCase(
        passwordRepository,
        accountRepository,
        masterKeyService);
    UUID accountId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID passwordId = UUID.randomUUID();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account(accountId)));
    when(masterKeyService.find(accountId, sessionId)).thenReturn(unlocked());
    when(passwordRepository.deleteByIdAndAccountId(passwordId, accountId)).thenReturn(false);

    assertThatThrownBy(() -> useCase.execute(accountId, sessionId, passwordId))
        .isInstanceOf(NotFoundException.class);

    verify(passwordRepository).deleteByIdAndAccountId(passwordId, accountId);
  }

  @Test
  void deletesCredentialForOwningAccount() {
    PasswordRepository passwordRepository = mock(PasswordRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    MasterKeySessionService masterKeyService = mock(MasterKeySessionService.class);
    DeletePasswordUseCase useCase = new DeletePasswordUseCase(
        passwordRepository,
        accountRepository,
        masterKeyService);
    UUID accountId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID passwordId = UUID.randomUUID();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account(accountId)));
    when(masterKeyService.find(accountId, sessionId)).thenReturn(unlocked());
    when(passwordRepository.deleteByIdAndAccountId(passwordId, accountId)).thenReturn(true);

    assertThatCode(() -> useCase.execute(accountId, sessionId, passwordId)).doesNotThrowAnyException();

    verify(passwordRepository).deleteByIdAndAccountId(passwordId, accountId);
  }

  @Test
  void doesNotAttemptDeletionWithoutVerifiedMasterKey() {
    PasswordRepository passwordRepository = mock(PasswordRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    MasterKeySessionService masterKeyService = mock(MasterKeySessionService.class);
    DeletePasswordUseCase useCase = new DeletePasswordUseCase(
        passwordRepository,
        accountRepository,
        masterKeyService);
    UUID accountId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UUID passwordId = UUID.randomUUID();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account(accountId)));
    when(masterKeyService.find(accountId, sessionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(accountId, sessionId, passwordId))
        .isInstanceOf(ForbiddenException.class);

    verify(passwordRepository, never()).deleteByIdAndAccountId(passwordId, accountId);
  }

  private Account account(UUID accountId) {
    return Account.reconstitute(
        accountId,
        "Usuario",
        "Prueba",
        "usuario@example.com",
        "3001234567",
        LocalDateTime.now().minusDays(1),
        "hash-password",
        "hash-master-key");
  }

  private Optional<MasterKeySessionService.UnlockedMasterKey> unlocked() {
    return Optional.of(new MasterKeySessionService.UnlockedMasterKey(
        "master-key",
        Instant.now().plusSeconds(3_600)));
  }
}
