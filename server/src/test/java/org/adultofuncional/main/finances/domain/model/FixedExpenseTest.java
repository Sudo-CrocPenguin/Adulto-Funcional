package org.adultofuncional.main.finances.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.junit.jupiter.api.Test;

class FixedExpenseTest {

  @Test
  void advancesEveryMissedOccurrenceUntilTheDueDateIsFuture() {
    FixedExpense expense = FixedExpense.create(
        "Arriendo",
        new BigDecimal("1000000.00"),
        UUID.randomUUID(),
        UUID.randomUUID(),
        Frequency.MONTHLY,
        LocalDate.of(2026, 4, 30),
        LocalDate.of(2026, 5, 31),
        3);

    int advances = expense.advanceBeyond(LocalDate.of(2026, 8, 3));

    assertThat(advances).isEqualTo(3);
    assertThat(expense.getNextDueDate()).isEqualTo(LocalDate.of(2026, 8, 30));
  }
}
