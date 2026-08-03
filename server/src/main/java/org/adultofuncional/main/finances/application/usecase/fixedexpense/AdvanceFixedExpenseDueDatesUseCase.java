package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Avanza de manera acotada los vencimientos recurrentes ya alcanzados.
 *
 * <p>Procesa lotes bloqueados en base de datos, usa el reloj compartido y
 * conserva la aritmética de recurrencia dentro del agregado de dominio.</p>
 */
@Service
public class AdvanceFixedExpenseDueDatesUseCase {

  private final FixedExpenseRepository repository;
  private final Clock clock;
  private final int batchSize;
  private final int maxBatches;

  public AdvanceFixedExpenseDueDatesUseCase(
      FixedExpenseRepository repository,
      Clock clock,
      @Value("${app.jobs.fixed-expenses.batch-size:100}") int batchSize,
      @Value("${app.jobs.fixed-expenses.max-batches:10}") int maxBatches) {
    if (batchSize < 1 || batchSize > 1_000 || maxBatches < 1 || maxBatches > 100) {
      throw new IllegalArgumentException("Configuración de lotes de gastos fijos inválida");
    }
    this.repository = repository;
    this.clock = clock;
    this.batchSize = batchSize;
    this.maxBatches = maxBatches;
  }

  /** @return número de gastos actualizados durante esta ejecución. */
  @Transactional
  public int execute() {
    LocalDate today = LocalDate.now(clock);
    int updated = 0;
    for (int batchNumber = 0; batchNumber < maxBatches; batchNumber++) {
      List<FixedExpense> dueExpenses = repository.findDueForUpdate(today, batchSize);
      if (dueExpenses.isEmpty()) {
        break;
      }
      dueExpenses.forEach(expense -> {
        expense.advanceBeyond(today);
        repository.save(expense);
      });
      updated += dueExpenses.size();
      if (dueExpenses.size() < batchSize) {
        break;
      }
    }
    return updated;
  }
}
