package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.category.CategoryResponse;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso: Listar gastos fijos con filtros.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ListFixedExpensesUseCase {

    private final FixedExpenseRepository fixedExpenseRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> execute(UUID accountId, FixedExpenseFilterRequest filter) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Cuenta no encontrada: " + accountId));

        List<FixedExpensesEntity> expenses = fixedExpenseRepository.findAllByAccountId(accountId);

        if (filter != null) {
            if (filter.getStatus() != null) {
                expenses = expenses.stream()
                        .filter(e -> e.getFixedExpenseStatus().equals(filter.getStatus().name()))
                        .collect(Collectors.toList());
            }
            if (filter.getCategoryId() != null) {
                expenses = expenses.stream()
                        .filter(e -> e.getCategory() != null && e.getCategory().getCategoryId().equals(filter.getCategoryId()))
                        .collect(Collectors.toList());
            }
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
                String term = filter.getSearchTerm().toLowerCase();
                expenses = expenses.stream()
                        .filter(e -> e.getFixedExpenseName().toLowerCase().contains(term))
                        .collect(Collectors.toList());
            }
        }

        return expenses.stream().map(this::mapToResponse).collect(Collectors.toList());
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
