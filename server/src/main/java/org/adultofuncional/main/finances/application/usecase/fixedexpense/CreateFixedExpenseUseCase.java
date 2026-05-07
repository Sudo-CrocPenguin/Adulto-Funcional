package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: Crear un nuevo gasto fijo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreateFixedExpenseUseCase {

    private final FixedExpenseRepository fixedExpenseRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public FixedExpenseResponse execute(UUID accountId, CreateFixedExpenseRequest request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountId));

        CategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + request.getCategoryId()));
        }

        FixedExpensesEntity entity = new FixedExpensesEntity();
        entity.setAccount(account);
        entity.setFixedExpenseName(request.getName());
        entity.setFixedExpenseFrequency(request.getFrequency().name());
        entity.setFixedExpenseAmount(request.getAmount());
        entity.setFixedExpenseStatus(request.getStatus().name());
        entity.setFixedExpenseClosingDate(request.getClosingDate());
        entity.setCategory(category);

        FixedExpensesEntity saved = fixedExpenseRepository.save(entity);
        return mapToResponse(saved);
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
