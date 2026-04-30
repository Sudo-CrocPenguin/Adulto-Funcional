package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la entidad JPA {@link FixedExpensesEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Getters y setters de todos los campos</li>
 *   <li>Precisión decimal de {@code fixed_expense_amount} (DECIMAL(10,2))</li>
 *   <li>Frecuencias: Mensual, Anual, Semanal, Quincenal, Trimestral</li>
 *   <li>Estados: Activo e Inactivo</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("FixedExpensesEntity")
class FixedExpensesEntityTest {

  private FixedExpensesEntity fixedExpense;

  @BeforeEach
  void setUp() {
    fixedExpense = new FixedExpensesEntity();
  }

  @Nested
  @DisplayName("Getters y Setters")
  class GettersAndSetters {

    /**
     * Establece y recupera todos los campos de la entidad.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      UUID id = UUID.randomUUID();
      String nombre = "Netflix";
      String frecuencia = "Mensual";
      BigDecimal amount = new BigDecimal("1500.99");
      String estado = "Activo";
      LocalDate closingDate = LocalDate.now();

      fixedExpense.setFixed_expense_id(id);
      fixedExpense.setFixed_expense_name(nombre);
      fixedExpense.setFixed_expense_frequency(frecuencia);
      fixedExpense.setFixed_expense_amount(amount);
      fixedExpense.setFixed_expense_status(estado);
      fixedExpense.setFixed_expense_closing_date(closingDate);

      assertEquals(id, fixedExpense.getFixed_expense_id());
      assertEquals(nombre, fixedExpense.getFixed_expense_name());
      assertEquals(frecuencia, fixedExpense.getFixed_expense_frequency());
      assertEquals(amount, fixedExpense.getFixed_expense_amount());
      assertEquals(estado, fixedExpense.getFixed_expense_status());
      assertEquals(closingDate, fixedExpense.getFixed_expense_closing_date());
    }

    /**
     * Verifica que el constructor sin argumentos crea una instancia no null.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      assertNotNull(fixedExpense);
    }
  }

  @Nested
  @DisplayName("Precisión decimal")
  class AmountPrecision {

    /**
     * Verifica que {@code fixed_expense_amount} mantiene 2 decimales.
     */
    @Test
    @DisplayName("Debe mantener precisión de 2 decimales")
    void testAmountPrecision() {
      fixedExpense.setFixed_expense_amount(new BigDecimal("999.99"));
      assertEquals(2, fixedExpense.getFixed_expense_amount().scale());
      assertEquals(new BigDecimal("999.99"), fixedExpense.getFixed_expense_amount());
    }

    /**
     * Verifica el comportamiento con más de 2 decimales a nivel Java.
     */
    @Test
    @DisplayName("Debe preservar la escala original aunque la BD redondee")
    void testAmountWithMoreThanTwoDecimals() {
      fixedExpense.setFixed_expense_amount(new BigDecimal("999.999"));
      assertEquals(3, fixedExpense.getFixed_expense_amount().scale());
    }

    /**
     * Verifica que se manejan montos enteros sin decimales.
     */
    @Test
    @DisplayName("Debe manejar montos enteros")
    void testAmountWithZeroDecimals() {
      fixedExpense.setFixed_expense_amount(new BigDecimal("1500"));
      assertEquals(0, fixedExpense.getFixed_expense_amount().scale());
    }

    /**
     * Verifica que se aceptan montos negativos (reembolsos).
     */
    @Test
    @DisplayName("Debe aceptar montos negativos")
    void testNegativeAmount() {
      BigDecimal negativeAmount = new BigDecimal("-500.00");
      fixedExpense.setFixed_expense_amount(negativeAmount);
      assertEquals(negativeAmount, fixedExpense.getFixed_expense_amount());
    }
  }

  @Nested
  @DisplayName("Frecuencias")
  class FrequencyValues {

    /**
     * Verifica que se aceptan las frecuencias comunes.
     */
    @Test
    @DisplayName("Debe aceptar frecuencias comunes")
    void testCommonFrequencyValues() {
      String[] frecuencias = { "Mensual", "Anual", "Semanal", "Quincenal", "Trimestral" };

      for (String frecuencia : frecuencias) {
        fixedExpense.setFixed_expense_frequency(frecuencia);
        assertEquals(frecuencia, fixedExpense.getFixed_expense_frequency());
      }
    }
  }

  @Nested
  @DisplayName("Estados")
  class StatusValues {

    /**
     * Verifica que se aceptan los estados {@code "Activo"} e {@code "Inactivo"}.
     */
    @Test
    @DisplayName("Debe aceptar estados 'Activo' e 'Inactivo'")
    void testStatusValues() {
      fixedExpense.setFixed_expense_status("Activo");
      assertEquals("Activo", fixedExpense.getFixed_expense_status());

      fixedExpense.setFixed_expense_status("Inactivo");
      assertEquals("Inactivo", fixedExpense.getFixed_expense_status());
    }
  }
}
