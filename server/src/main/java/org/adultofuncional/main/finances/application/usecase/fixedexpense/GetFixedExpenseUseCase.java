package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Obtener un gasto fijo por ID.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetFixedExpenseUseCase {

    private final FixedExpenseRepository fixedExpenseRepository;

    @Transactional(readOnly = true)
    public FixedExpenseResponse execute(UUID accountId, UUID expenseId) {
        FixedExpensesEntity entity = fixedExpenseRepository.findByIdAndAccountId(expenseId, accountId)
                .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado: " + expenseId));
        return mapToResponse(entity);
    }

    private FixedExpenseResponse mapToResponse(FixedExpensesEntity entity) {
        CategoryResponse categoryResponse = null;
        if (entity.getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .id(entity.getCategory().getCategoryId())
                    .name(entity.getCategory().getCategoryName())
                    .type(org.adultofuncional.main.finances.domain.enums.CategoryType.valueOf(entity.getCategory().getCategoryType()))
                    .createdAt(entity.getCategory().getCategoryCreatedAt())
                    .deleted(entity.getCategory().getCategoryDeletedAt() != null)
                    .build();
        }
        return FixedExpenseResponse.builder()
                .id(entity.getFixedExpenseId())
                .name(entity.getFixedExpenseName())
                .frequency(org.adultofuncional.main.finances.domain.enums.FixedExpenseFrequency.valueOf(entity.getFixedExpenseFrequency()))
                .amount(entity.getFixedExpenseAmount())
                .status(org.adultofuncional.main.finances.domain.enums.FixedExpenseStatus.valueOf(entity.getFixedExpenseStatus()))
                .closingDate(entity.getFixedExpenseClosingDate())
                .category(categoryResponse)
                .build();
    }
}
