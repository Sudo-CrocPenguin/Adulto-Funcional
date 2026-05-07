package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Eliminar un gasto fijo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class DeleteFixedExpenseUseCase {

    private final FixedExpenseRepository fixedExpenseRepository;

    @Transactional
    public void execute(UUID accountId, UUID expenseId) {
        if (!fixedExpenseRepository.existsByIdAndAccountId(expenseId, accountId)) {
            throw new NotFoundException("Gasto fijo no encontrado: " + expenseId);
        }
        fixedExpenseRepository.deleteById(expenseId);
    }
}
