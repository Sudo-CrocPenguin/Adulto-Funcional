package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class FixedExpenseOwnershipUseCaseTest {

  private static final Clock CLOCK = Clock.fixed(
      Instant.parse("2026-08-03T12:00:00Z"),
      ZoneOffset.UTC);

  @Test
  void doesNotReturnFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    GetFixedExpenseUseCase useCase = new GetFixedExpenseUseCase(
        fixedExpenseRepository,
        categoryRepository);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, authenticatedAccountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(categoryRepository, never()).findAccessibleByIdAndType(any(), any(), any());
  }

  @Test
  void doesNotDeleteFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    DeleteFixedExpenseUseCase useCase = new DeleteFixedExpenseUseCase(fixedExpenseRepository);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();

    when(fixedExpenseRepository.deleteByIdAndAccountId(expenseId, authenticatedAccountId))
        .thenReturn(false);

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(fixedExpenseRepository).deleteByIdAndAccountId(expenseId, authenticatedAccountId);
  }

  @Test
  void doesNotUpdateFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    UpdateFixedExpenseUseCase useCase = new UpdateFixedExpenseUseCase(
        fixedExpenseRepository,
        categoryRepository,
        CLOCK);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    UpdateFixedExpenseRequest request = UpdateFixedExpenseRequest.builder()
        .name("Nombre ajeno")
        .build();

    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, authenticatedAccountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId, request))
        .isInstanceOf(NotFoundException.class);

    verify(fixedExpenseRepository, never()).save(any(FixedExpense.class));
  }

  @Test
  void returnsFixedExpenseToOwningAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    GetFixedExpenseUseCase useCase = new GetFixedExpenseUseCase(
        fixedExpenseRepository,
        categoryRepository);

    UUID accountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    FixedExpense expense = fixedExpense(expenseId, accountId, categoryId);
    Category category = category(categoryId);

    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, accountId))
        .thenReturn(Optional.of(expense));
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        categoryId,
        CategoryType.FINANCES)).thenReturn(Optional.of(category));

    FixedExpenseResponse response = useCase.execute(accountId, expenseId);

    assertThat(response.getId()).isEqualTo(expenseId);
    assertThat(response.getCategory().getId()).isEqualTo(categoryId);
  }

  @Test
  void deletesFixedExpenseForOwningAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    DeleteFixedExpenseUseCase useCase = new DeleteFixedExpenseUseCase(fixedExpenseRepository);

    UUID accountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    when(fixedExpenseRepository.deleteByIdAndAccountId(expenseId, accountId))
        .thenReturn(true);

    assertThatCode(() -> useCase.execute(accountId, expenseId)).doesNotThrowAnyException();

    verify(fixedExpenseRepository).deleteByIdAndAccountId(expenseId, accountId);
  }

  @Test
  void updatesFixedExpenseForOwningAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    UpdateFixedExpenseUseCase useCase = new UpdateFixedExpenseUseCase(
        fixedExpenseRepository,
        categoryRepository,
        CLOCK);

    UUID accountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    FixedExpense expense = fixedExpense(expenseId, accountId, categoryId);
    Category category = category(categoryId);
    UpdateFixedExpenseRequest request = UpdateFixedExpenseRequest.builder()
        .name("Internet hogar")
        .build();

    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, accountId))
        .thenReturn(Optional.of(expense));
    when(fixedExpenseRepository.save(expense)).thenReturn(expense);
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        categoryId,
        CategoryType.FINANCES)).thenReturn(Optional.of(category));

    FixedExpenseResponse response = useCase.execute(accountId, expenseId, request);

    assertThat(response.getName()).isEqualTo("Internet hogar");
    verify(fixedExpenseRepository).save(expense);
  }

  private FixedExpense fixedExpense(UUID expenseId, UUID accountId, UUID categoryId) {
    LocalDate startDate = LocalDate.now();
    return FixedExpense.reconstitute(
        expenseId,
        "Internet",
        new BigDecimal("120000.00"),
        categoryId,
        accountId,
        Frequency.MONTHLY,
        Status.ACTIVE,
        startDate,
        startDate.plusMonths(1),
        3);
  }

  private Category category(UUID categoryId) {
    return Category.reconstitute(categoryId, "Servicios", CategoryType.FINANCES);
  }
}
