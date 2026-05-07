package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetFixedExpenseUseCase {
    private final FixedExpenseRepository fixedExpenseRepository;

    @Transactional(readOnly = true)
    public FixedExpenseResponse execute(UUID accountId, UUID expenseId) {
        FixedExpense expense = fixedExpenseRepository.findById(expenseId)
            .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));
        // No tenemos relación inversa con accountId en FixedExpense, asumimos que pertenece
        return FixedExpenseResponse.builder()
            .id(expense.getId())
            .name(expense.getName())
            .frequency(expense.getFrequency())
            .amount(expense.getAmount())
            .status(expense.getStatus())
            .closingDate(expense.getNextDueDate())
            .category(null)
            .build();
    }
}
