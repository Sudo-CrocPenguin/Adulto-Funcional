package org.adultofuncional.main.finances.infrastructure.scheduling;

import org.adultofuncional.main.finances.application.usecase.fixedexpense.AdvanceFixedExpenseDueDatesUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Adaptador que activa periódicamente el avance de gastos fijos vencidos. */
@Component
@ConditionalOnProperty(name = "app.jobs.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class FixedExpenseSchedule {

  private final AdvanceFixedExpenseDueDatesUseCase advanceDueDates;

  @Scheduled(
      cron = "${app.jobs.fixed-expenses.cron:0 5 * * * *}",
      zone = "${app.jobs.fixed-expenses.zone:UTC}")
  public void advanceDueDates() {
    advanceDueDates.execute();
  }
}
