package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

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
    UUID expenseId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, authenticatedAccountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(categoryRepository, never()).findById(categoryId);
  }

  @Test
  void doesNotDeleteFixedExpenseOwnedByAnotherAccount() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    DeleteFixedExpenseUseCase useCase = new DeleteFixedExpenseUseCase(fixedExpenseRepository);

    UUID authenticatedAccountId = UUID.randomUUID();
    UUID expenseId = UUID.randomUUID();

    when(fixedExpenseRepository.findByIdAndAccountId(expenseId, authenticatedAccountId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(authenticatedAccountId, expenseId))
        .isInstanceOf(NotFoundException.class);

    verify(fixedExpenseRepository, never()).deleteById(expenseId);
  }
}
