package org.adultofuncional.main.finances.application.usecase.fixedexpense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.fixedexpense.CreateFixedExpenseRequest;
import org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.finances.domain.model.Category;
import org.adultofuncional.main.finances.domain.model.FixedExpense;
import org.adultofuncional.main.finances.domain.repository.CategoryRepository;
import org.adultofuncional.main.finances.domain.repository.FixedExpenseRepository;
import org.junit.jupiter.api.Test;

class CreateFixedExpenseUseCaseTest {

  @Test
  void appliesRequestedInitialStatusAndReturnsCategoryName() {
    FixedExpenseRepository fixedExpenseRepository = mock(FixedExpenseRepository.class);
    AccountRepository accountRepository = mock(AccountRepository.class);
    CategoryRepository categoryRepository = mock(CategoryRepository.class);
    CreateFixedExpenseUseCase useCase = new CreateFixedExpenseUseCase(
        fixedExpenseRepository,
        accountRepository,
        categoryRepository);

    UUID accountId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();
    Account account = Account.reconstitute(
        accountId,
        "Usuario",
        "Prueba",
        "usuario@example.com",
        "3001234567",
        LocalDateTime.now().minusDays(1),
        "hash-password",
        null);
    Category category = Category.reconstitute(categoryId, "Servicios", CategoryType.FINANCES);

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(categoryRepository.findAccessibleByIdAndType(
        accountId,
        categoryId,
        CategoryType.FINANCES)).thenReturn(Optional.of(category));
    when(fixedExpenseRepository.save(any(FixedExpense.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CreateFixedExpenseRequest request = CreateFixedExpenseRequest.builder()
        .name("Internet")
        .frequency(Frequency.MONTHLY)
        .amount(new BigDecimal("120000"))
        .status(Status.INACTIVE)
        .nextDueDate(LocalDate.now().plusMonths(1))
        .categoryId(categoryId)
        .build();

    FixedExpenseResponse response = useCase.execute(accountId, request);

    assertThat(response.getStatus()).isEqualTo(Status.INACTIVE);
    assertThat(response.getCategory().getName()).isEqualTo("Servicios");
  }
}
