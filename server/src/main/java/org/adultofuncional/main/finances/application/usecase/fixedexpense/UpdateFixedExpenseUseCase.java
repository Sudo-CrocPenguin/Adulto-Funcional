package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.UpdateFixedExpenseRequest;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Caso de uso: Actualizar un gasto fijo existente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class UpdateFixedExpenseUseCase {

    private final FixedExpenseRepository fixedExpenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public FixedExpenseResponse execute(UUID accountId, UUID expenseId, UpdateFixedExpenseRequest request) {
        FixedExpensesEntity entity = fixedExpenseRepository.findByIdAndAccountId(expenseId, accountId)
                .orElseThrow(() -> new NotFoundException("Gasto fijo no encontrado: " + expenseId));

        if (StringUtils.hasText(request.getName())) {
            entity.setFixedExpenseName(request.getName());
        }
        if (request.getFrequency() != null) {
            entity.setFixedExpenseFrequency(request.getFrequency().name());
        }
        if (request.getAmount() != null) {
            entity.setFixedExpenseAmount(request.getAmount());
        }
        if (request.getStatus() != null) {
            entity.setFixedExpenseStatus(request.getStatus().name());
        }
        if (request.getClosingDate() != null) {
            entity.setFixedExpenseClosingDate(request.getClosingDate());
        }
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.getCategoryId()));
            entity.setCategory(category);
        }

        FixedExpensesEntity updated = fixedExpenseRepository.save(entity);
        return mapToResponse(updated);
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
