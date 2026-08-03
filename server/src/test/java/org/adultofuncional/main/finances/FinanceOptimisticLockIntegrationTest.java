package org.adultofuncional.main.finances;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringMovementJpaRepository;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Comprueba el bloqueo optimista contra MariaDB con dos copias obsoletas. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FinanceOptimisticLockIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  SpringAccountJpaRepository accountRepository;

  @Autowired
  SpringCategoryJpaRepository categoryRepository;

  @Autowired
  SpringMovementJpaRepository movementRepository;

  @Autowired
  PlatformTransactionManager transactionManager;

  @Test
  void rejectsTheSecondUpdateFromAStaleMovementVersion() {
    UUID movementId = inTransaction(() -> {
      AccountEntity account = new AccountEntity();
      account.setAccountId(UUID.randomUUID());
      account.setAccountNames("Usuario");
      account.setAccountLastNames("Concurrente");
      account.setAccountEmail("concurrency-" + UUID.randomUUID() + "@example.com");
      account.setAccountPhone("+573001234567");
      account.setAccountPassword("hash-password");
      account = accountRepository.saveAndFlush(account);

      CategoryEntity category = categoryRepository.findById(
          UUID.fromString("01988e6b-0c00-7000-8000-000000000006")).orElseThrow();
      MovementEntity movement = new MovementEntity();
      movement.setMovementId(UUID.randomUUID());
      movement.setMovementType(MovementType.EXPENSE.name());
      movement.setMovementAmount(new BigDecimal("10.00"));
      movement.setMovementDate(LocalDate.of(2026, 8, 3));
      movement.setMovementDescription("Original");
      movement.setAccount(account);
      movement.setCategory(category);
      return movementRepository.saveAndFlush(movement).getMovementId();
    });

    MovementEntity firstCopy = inTransaction(
        () -> movementRepository.findById(movementId).orElseThrow());
    MovementEntity staleCopy = inTransaction(
        () -> movementRepository.findById(movementId).orElseThrow());

    firstCopy.setMovementDescription("Primera escritura");
    inTransaction(() -> movementRepository.saveAndFlush(firstCopy));

    staleCopy.setMovementDescription("Escritura obsoleta");
    assertThatThrownBy(() -> inTransaction(() -> movementRepository.saveAndFlush(staleCopy)))
        .isInstanceOf(ObjectOptimisticLockingFailureException.class);

    MovementEntity persisted = inTransaction(
        () -> movementRepository.findById(movementId).orElseThrow());
    assertThat(persisted.getMovementDescription()).isEqualTo("Primera escritura");
    assertThat(persisted.getVersion()).isEqualTo(1L);
  }

  private <T> T inTransaction(java.util.function.Supplier<T> action) {
    TransactionTemplate transaction = new TransactionTemplate(transactionManager);
    return transaction.execute(status -> action.get());
  }
}
