package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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

  @Test
  void doesNotReturnFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    GetFixedExpenseUseCase useCase = new GetFixedExpenseUseCase(
        fixedExpenseRepository,
        categoryRepository);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID ownerAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    FixedExpense foreignExpense = fixedExpense(expenseId, ownerAccountId, categoryId);
    Category category = Category.reconstitute(categoryId, "Servicios", CategoryType.FINANCES);

    when(fixedExpenseRepository.findById(expenseId)).thenReturn(Optional.of(foreignExpense));
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(categoryRepository, never()).findById(categoryId);
  }

  @Test
  void doesNotDeleteFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    DeleteFixedExpenseUseCase useCase = new DeleteFixedExpenseUseCase(fixedExpenseRepository);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID ownerAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();
    FixedExpense foreignExpense = fixedExpense(expenseId, ownerAccountId, UUID.randomUUID());

    when(fixedExpenseRepository.findById(expenseId)).thenReturn(Optional.of(foreignExpense));

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(fixedExpenseRepository, never()).deleteById(expenseId);
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
}
