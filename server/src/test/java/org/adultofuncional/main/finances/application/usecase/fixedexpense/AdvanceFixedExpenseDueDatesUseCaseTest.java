package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.junit.jupiter.api.Test;

class AdvanceFixedExpenseDueDatesUseCaseTest {

  @Test
  void processesABoundedLockedBatchUsingTheInjectedClock() {
    FixedExpenseRepository repository = mock(FixedExpenseRepository.class);
    Clock clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);
    AdvanceFixedExpenseDueDatesUseCase useCase =
        new AdvanceFixedExpenseDueDatesUseCase(repository, clock, 100, 10);
    FixedExpense expense = FixedExpense.create(
        "Internet",
        new BigDecimal("100.00"),
        UUID.randomUUID(),
        UUID.randomUUID(),
        Frequency.MONTHLY,
        LocalDate.of(2026, 6, 1),
        LocalDate.of(2026, 7, 1),
        2);
    when(repository.findDueForUpdate(LocalDate.of(2026, 8, 3), 100))
        .thenReturn(List.of(expense));

    int updated = useCase.execute();

    assertThat(updated).isOne();
    assertThat(expense.getNextDueDate()).isEqualTo(LocalDate.of(2026, 9, 1));
    verify(repository).save(expense);
  }
}
