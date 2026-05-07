package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateFixedExpenseUseCase {
    private final FixedExpenseRepository fixedExpenseRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public FixedExpenseResponse execute(UUID accountId, CreateFixedExpenseRequest request) {
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        if (request.getClosingDate().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de cierre debe ser posterior a la fecha actual");
        }

        UUID categoryId = null;
        if (request.getCategoryId() != null) {
            categoryId = request.getCategoryId();
            categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada con id: " + categoryId));
        }

        // Nota: el modelo FixedExpense requiere startDate, nextDueDate, reminderDays.
        // Como el DTO solo tiene closingDate, usaremos closingDate como nextDueDate
        // y startDate como hoy. reminderDays por defecto 0.
        LocalDate startDate = LocalDate.now();
        LocalDate nextDueDate = request.getClosingDate();
        int reminderDays = 0;

        FixedExpense expense = FixedExpense.create(
            request.getName(),
            request.getAmount(),
            categoryId,
            request.getFrequency(),
            startDate,
            nextDueDate,
            reminderDays
        );

        FixedExpense saved = fixedExpenseRepository.save(expense);
        return FixedExpenseResponse.builder()
            .id(saved.getId())
            .name(saved.getName())
            .frequency(saved.getFrequency())
            .amount(saved.getAmount())
            .status(saved.getStatus())
            .closingDate(saved.getNextDueDate()) // usamos nextDueDate como closingDate
            .category(null)
            .build();
    }
}
