package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UpdateFixedExpenseUseCase {
    private final FixedExpenseRepository fixedExpenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public FixedExpenseResponse execute(UUID accountId, UUID expenseId, UpdateFixedExpenseRequest request) {
        FixedExpense expense = fixedExpenseRepository.findById(expenseId)
            .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado con id: " + expenseId));

        // Actualizar campos (usando el método update del modelo)
        String name = expense.getName();
        BigDecimal amount = expense.getAmount();
        UUID categoryId = expense.getCategoryId();
        var frequency = expense.getFrequency();
        var status = expense.getStatus();
        LocalDate startDate = expense.getStartDate();
        LocalDate nextDueDate = expense.getNextDueDate();
        int reminderDays = expense.getReminderDays();

        if (StringUtils.hasText(request.getName())) name = request.getName();
        if (request.getAmount() != null) amount = request.getAmount();
        if (request.getFrequency() != null) frequency = request.getFrequency();
        if (request.getStatus() != null) status = request.getStatus();
        if (request.getClosingDate() != null) {
            if (request.getClosingDate().isBefore(LocalDate.now()))
                throw new BusinessException("La fecha de cierre debe ser futura");
            nextDueDate = request.getClosingDate();
        }
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
            categoryId = request.getCategoryId();
        }

        expense.update(name, amount, categoryId, frequency, startDate, nextDueDate, reminderDays);
        if (request.getStatus() != null) {
            if (request.getStatus() == Status.ACTIVE) expense.activate();
            else expense.deactivate();
        }

        FixedExpense saved = fixedExpenseRepository.save(expense);
        return FixedExpenseResponse.builder()
            .id(saved.getId())
            .name(saved.getName())
            .frequency(saved.getFrequency())
            .amount(saved.getAmount())
            .status(saved.getStatus())
            .closingDate(saved.getNextDueDate())
            .category(null)
            .build();
    }
}
