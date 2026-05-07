package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFixedExpenseUseCase {
    private final FixedExpenseRepository fixedExpenseRepository;

    @Transactional
    public void execute(UUID accountId, UUID expenseId) {
        if (!fixedExpenseRepository.findById(expenseId).isPresent()) {
            throw new NotFoundException("Gasto fijo no encontrado con id: " + expenseId);
        }
        fixedExpenseRepository.deleteById(expenseId);
    }
}
