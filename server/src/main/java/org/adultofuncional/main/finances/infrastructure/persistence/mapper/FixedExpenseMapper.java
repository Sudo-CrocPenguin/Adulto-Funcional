package org.adultofuncional.main.finances.infrastructure.persistence.mapper;

import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.springframework.stereotype.Component;


@Component
public class FixedExpenseMapper {
    
    public FixedExpense toDomain(FixedExpensesEntity entity) {
        if (entity == null) return null;

        UUID categoryId = entity.getCategory() != null ? entity.getCategory().getCategoryId() : null;

        return FixedExpense.reconstitute(
            entity.getFixedExpenseId(),
            entity.getFixedExpenseName(),
            entity.getFixedExpenseAmount(),
            categoryId,
            Frequency.valueOf(entity.getFixedExpenseFrequency()),
            Status.valueOf(entity.getFixedExpenseStatus()),
            entity.getFixedExpenseStartDate(),
            entity.getFixedExpenseNextDueDate(),
            entity.getFixedExpenseReminderDays()
        );
    }

    public FixedExpensesEntity toEntity(FixedExpense fixedExpense, UUID accountId) {
        if (fixedExpense == null) return null;

        FixedExpensesEntity entity = new FixedExpensesEntity();
        entity.setFixedExpenseId(fixedExpense.getId());
        entity.setFixedExpenseName(fixedExpense.getName());
        entity.setFixedExpenseAmount(fixedExpense.getAmount());
        entity.setFixedExpenseFrequency(fixedExpense.getFrequency().name());
        entity.setFixedExpenseStatus(fixedExpense.getStatus().name());
        entity.setFixedExpenseStartDate(fixedExpense.getStartDate());
        entity.setFixedExpenseNextDueDate(fixedExpense.getNextDueDate());
        entity.setFixedExpenseReminderDays(fixedExpense.getReminderDays());

        if (fixedExpense.getCategoryId() != null) {
            CategoryEntity categoryRef = new CategoryEntity();
            categoryRef.setCategoryId(fixedExpense.getCategoryId());
            entity.setCategory(categoryRef);
        }

        AccountEntity accountRef = new AccountEntity();
        accountRef.setAccountId(accountId);
        entity.setAccount(accountRef);

        return entity;
    }
}
