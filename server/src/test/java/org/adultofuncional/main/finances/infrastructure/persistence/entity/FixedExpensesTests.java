package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixedExpensesEntityTest {

  private FixedExpensesEntity fixedExpense;

  @BeforeEach
  void setUp() {
    fixedExpense = new FixedExpensesEntity();
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    BigDecimal amount = new BigDecimal("1500.99");
    LocalDate closingDate = LocalDate.now();

    fixedExpense.setFixed_expense_id(id);
    fixedExpense.setFixed_expense_name("Netflix");
    fixedExpense.setFixed_expense_frequency("Mensual");
    fixedExpense.setFixed_expense_amount(amount);
    fixedExpense.setFixed_expense_status("Activo");
    fixedExpense.setFixed_expense_closing_date(closingDate);

    assertEquals(id, fixedExpense.getFixed_expense_id());
    assertEquals("Netflix", fixedExpense.getFixed_expense_name());
    assertEquals("Mensual", fixedExpense.getFixed_expense_frequency());
    assertEquals(amount, fixedExpense.getFixed_expense_amount());
    assertEquals("Activo", fixedExpense.getFixed_expense_status());
    assertEquals(closingDate, fixedExpense.getFixed_expense_closing_date());
  }

  @Test
  void testAmountPrecision() {
    BigDecimal amount = new BigDecimal("999.99");
    fixedExpense.setFixed_expense_amount(amount);
    assertEquals(2, fixedExpense.getFixed_expense_amount().scale());
  }
}
