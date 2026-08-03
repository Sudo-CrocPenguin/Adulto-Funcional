package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

class CreateFixedExpenseUseCaseTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 8, 3);
  private static final Clock CLOCK = Clock.fixed(
      Instant.parse("2026-08-03T12:00:00Z"),
      ZoneOffset.UTC);

  @Test
  void appliesRequestedInitialStatusAndReturnsCategoryName() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    CreateFixedExpenseUseCase useCase = new CreateFixedExpenseUseCase(
        fixedExpenseRepository,
        accountRepository,
        categoryRepository,
        CLOCK);

    UUID accountId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    Account account = Account.reconstitute(
        accountId,
        "Usuario",
        "Prueba",
        "usuario@example.com",
        "3001234567",
        Instant.parse("2026-08-02T12:00:00Z"),
        "hash-password",
        null);
    Category category = Category.reconstitute(categoryId, "Servicios", CategoryType.FINANCES);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        categoryId,
        CategoryType.FINANCES)).thenReturn(Optional.of(category));
    when(fixedExpenseRepository.save(any(FixedExpense.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateFixedExpenseRequest request = CreateFixedExpenseRequest.builder()
        .name("Internet")
        .frequency(Frequency.MONTHLY)
        .amount(new BigDecimal("120000"))
        .status(Status.INACTIVE)
        .startDate(TODAY.minusMonths(1))
        .reminderDays(3)
        .nextDueDate(TODAY.plusMonths(1))
        .categoryId(categoryId)
        .build();

    FixedExpenseResponse response = useCase.execute(accountId, request);

    assertThat(response.getStatus()).isEqualTo(Status.INACTIVE);
    assertThat(response.getStartDate()).isEqualTo(TODAY.minusMonths(1));
    assertThat(response.getReminderDays()).isEqualTo(3);
    assertThat(response.getCategory().getName()).isEqualTo("Servicios");
  }

  @Test
  void rejectsDueDateEqualToTheInjectedCurrentDate() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    CreateFixedExpenseUseCase useCase = new CreateFixedExpenseUseCase(
        fixedExpenseRepository,
        accountRepository,
        categoryRepository,
        CLOCK);
    UUID accountId = UUID.randomUUID();
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(Account.reconstitute(
        accountId,
        "Usuario",
        "Prueba",
        "usuario@example.com",
        "+573001234567",
        Instant.parse("2026-08-02T12:00:00Z"),
        "hash-password",
        null)));

    CreateFixedExpenseRequest request = CreateFixedExpenseRequest.builder()
        .name("Internet")
        .frequency(Frequency.MONTHLY)
        .amount(new BigDecimal("120000.00"))
        .status(Status.ACTIVE)
        .nextDueDate(TODAY)
        .categoryId(UUID.randomUUID())
        .build();

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> useCase.execute(accountId, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("La fecha de cierre debe ser posterior a la fecha actual");
  }
}
