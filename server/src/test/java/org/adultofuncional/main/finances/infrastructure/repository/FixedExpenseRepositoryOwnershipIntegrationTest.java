package org.adultofuncional.main.finances.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.mapper.FixedExpenseMapper;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringCategoryJpaRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.repository.SpringFixedExpenseJpaRepository;
import org.adultofuncional.main.testsupport.MariaDbIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FixedExpenseMapper.class)
class FixedExpenseRepositoryOwnershipIntegrationTest extends MariaDbIntegrationTestSupport {

  @Autowired
  SpringFixedExpenseJpaRepository fixedExpenseJpaRepository;

  @Autowired
  SpringAccountJpaRepository accountJpaRepository;

  @Autowired
  SpringCategoryJpaRepository categoryJpaRepository;

  @Autowired
  FixedExpenseMapper fixedExpenseMapper;

  FixedExpenseRepositoryImpl repository;
  AccountEntity accountA;
  AccountEntity accountB;
  CategoryEntity category;

  @BeforeEach
  void setUp() {
    repository = new FixedExpenseRepositoryImpl(
        fixedExpenseJpaRepository,
        fixedExpenseMapper);
    accountA = persistAccount("cuenta-a@example.com");
    accountB = persistAccount("cuenta-b@example.com");
    category = categoryJpaRepository.findById(
        UUID.fromString("01988e6b-0c00-7000-8000-000000000006")).orElseThrow();
  }

  @Test
  void findsExpenseOnlyInsideOwningAccount() {
    FixedExpensesEntity expense = persistExpense(accountA);

    assertThat(repository.findByIdAndAccountId(
        expense.getFixedExpenseId(), accountA.getAccountId())).isPresent();
    assertThat(repository.findByIdAndAccountId(
        expense.getFixedExpenseId(), accountB.getAccountId())).isEmpty();
  }

  @Test
  void foreignAccountCannotDeleteExpense() {
    FixedExpensesEntity expense = persistExpense(accountA);
    UUID expenseId = expense.getFixedExpenseId();

    boolean deletedByForeignAccount = repository.deleteByIdAndAccountId(
        expenseId,
        accountB.getAccountId());

    assertThat(deletedByForeignAccount).isFalse();
    assertThat(fixedExpenseJpaRepository.existsById(expenseId)).isTrue();
  }

  @Test
  void owningAccountCanDeleteExpense() {
    FixedExpensesEntity expense = persistExpense(accountA);
    UUID expenseId = expense.getFixedExpenseId();

    boolean deletedByOwner = repository.deleteByIdAndAccountId(
        expenseId,
        accountA.getAccountId());

    assertThat(deletedByOwner).isTrue();
    assertThat(fixedExpenseJpaRepository.existsById(expenseId)).isFalse();
  }

  private AccountEntity persistAccount(String email) {
    AccountEntity account = new AccountEntity();
    account.setAccountId(UUID.randomUUID());
    account.setAccountNames("Usuario");
    account.setAccountLastNames("Prueba");
    account.setAccountEmail(email);
    account.setAccountPhone("3001234567");
    account.setAccountPassword("hash-password");
    return accountJpaRepository.saveAndFlush(account);
  }

  private FixedExpensesEntity persistExpense(AccountEntity owner) {
    LocalDate startDate = LocalDate.now();
    FixedExpensesEntity entity = new FixedExpensesEntity();
    entity.setFixedExpenseId(UUID.randomUUID());
    entity.setFixedExpenseName("Internet");
    entity.setFixedExpenseFrequency(Frequency.MONTHLY.name());
    entity.setFixedExpenseAmount(new BigDecimal("120000.00"));
    entity.setFixedExpenseStatus(Status.ACTIVE.name());
    entity.setFixedExpenseStartDate(startDate);
    entity.setFixedExpenseNextDueDate(startDate.plusMonths(1));
    entity.setFixedExpenseReminderDays(3);
    entity.setCategory(category);
    entity.setAccount(owner);
    return fixedExpenseJpaRepository.saveAndFlush(entity);
  }
}
