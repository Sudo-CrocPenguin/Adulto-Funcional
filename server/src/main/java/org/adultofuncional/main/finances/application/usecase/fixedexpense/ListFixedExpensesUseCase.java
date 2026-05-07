package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseFilterRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ListFixedExpensesUseCase {
    private final FixedExpenseRepository fixedExpenseRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<FixedExpenseResponse> execute(UUID accountId, FixedExpenseFilterRequest filter) {
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        List<FixedExpense> expenses = fixedExpenseRepository.findAllByAccountId(accountId);
        if (filter != null) {
            if (filter.getStatus() != null) {
                expenses = expenses.stream()
                    .filter(e -> e.getStatus() == filter.getStatus())
                    .collect(Collectors.toList());
            }
            if (filter.getCategoryId() != null) {
                expenses = expenses.stream()
                    .filter(e -> e.getCategoryId() != null && e.getCategoryId().equals(filter.getCategoryId()))
                    .collect(Collectors.toList());
            }
            if (StringUtils.hasText(filter.getSearchTerm())) {
                String term = filter.getSearchTerm().toLowerCase();
                expenses = expenses.stream()
                    .filter(e -> e.getName().toLowerCase().contains(term))
                    .collect(Collectors.toList());
            }
        }
        return expenses.stream()
            .map(e -> FixedExpenseResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .frequency(e.getFrequency())
                .amount(e.getAmount())
                .status(e.getStatus())
                .closingDate(e.getNextDueDate())
                .category(null)
                .build())
            .collect(Collectors.toList());
    }
}
