package org.adultofuncional.main.account.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.mapper.AccountMapper;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.junit.jupiter.api.Test;

class AccountRepositoryImplTest {

  @Test
  void updatesManagedEntityScalarsWithoutReplacingChildCollections() {
    SpringAccountJpaRepository jpaRepository = mock(SpringAccountJpaRepository.class);
    AccountRepositoryImpl repository = new AccountRepositoryImpl(jpaRepository, new AccountMapper());

    UUID accountId = UUID.randomUUID();
    Instant createdAt = Instant.now().minusSeconds(86_400);
    Account account = Account.reconstitute(
        accountId,
        "Nuevo",
        "Usuario",
        "nuevo@example.com",
        "3001234567",
        createdAt,
        "hash-password-nuevo",
        "hash-master-key-nuevo");

    AccountEntity managedEntity = new AccountEntity();
    managedEntity.setAccountId(accountId);
    managedEntity.setAccountNames("Anterior");
    managedEntity.setAccountLastNames("Nombre");
    managedEntity.setAccountEmail("anterior@example.com");
    managedEntity.setAccountPhone("3000000000");
    managedEntity.setAccountCreatedAt(createdAt);
    managedEntity.setAccountPassword("hash-password-anterior");
    managedEntity.setAccountMasterKey("hash-master-key-anterior");

    ArrayList<MovementEntity> movements = new ArrayList<>();
    managedEntity.setMovements(movements);

    when(jpaRepository.findById(accountId)).thenReturn(java.util.Optional.of(managedEntity));
    when(jpaRepository.save(any(AccountEntity.class))).thenReturn(managedEntity);

    Account saved = repository.save(account);

    verify(jpaRepository).save(managedEntity);
    assertThat(managedEntity.getMovements()).isSameAs(movements);
    assertThat(managedEntity.getAccountEmail()).isEqualTo("nuevo@example.com");
    assertThat(managedEntity.getAccountPassword()).isEqualTo("hash-password-nuevo");
    assertThat(saved.getEmail()).isEqualTo("nuevo@example.com");
  }
}
