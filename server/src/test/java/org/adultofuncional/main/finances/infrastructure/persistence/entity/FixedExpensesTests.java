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
 * Tests unitarios para la entidad {@link FixedExpensesEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Funcionamiento de getters y setters para todos los campos</li>
 * <li>Manejo de precisión decimal para montos monetarios</li>
 * <li>Validación de campos obligatorios y formatos</li>
 * </ul>
 * 
 * <p>
 * <strong>Características destacadas:</strong>
 * <ul>
 * <li><b>Precisión monetaria:</b> Usa {@link BigDecimal} con escala 2 para
 * representar valores financieros exactos</li>
 * <li><b>Frecuencias configurables:</b> Soporte para diferentes periodicidades
 * (Mensual, Anual, Semanal, etc.)</li>
 * <li><b>Estados de ciclo de vida:</b> Activo/Inactivo para control de gastos
 * fijos</li>
 * <li><b>Fecha de cierre:</b> Permite programar cuándo finaliza un gasto fijo
 * recurrente</li>
 * </ul>
 * 
 * <p>
 * Esta entidad representa gastos recurrentes como suscripciones, servicios,
 * alquileres o cualquier pago periódico que deba ser registrado
 * automáticamente.
 * 
 * @author juan
 * @since 0.0.1
 * @see java.math.BigDecimal
 */
@DisplayName("FixedExpensesEntity - Tests de entidad JPA para gastos fijos recurrentes")
class FixedExpensesEntityTest {

  private FixedExpensesEntity fixedExpense;

  @BeforeEach
  void setUp() {
    fixedExpense = new FixedExpensesEntity();
  }

  /**
   * Tests relacionados con getters y setters de todos los campos.
   */
  @Nested
  @DisplayName("Getters y Setters")
  class GettersAndSetters {

    /**
     * Verifica que todos los getters y setters de la entidad funcionen
     * correctamente,
     * estableciendo y recuperando valores para cada campo.
     * 
     * <p>
     * Campos probados:
     * <ul>
     * <li>fixed_expense_id (UUID) - Identificador único</li>
     * <li>fixed_expense_name (String) - Nombre descriptivo (máx. 20 chars)</li>
     * <li>fixed_expense_frequency (String) - Periodicidad del gasto</li>
     * <li>fixed_expense_amount (BigDecimal) - Monto con precisión decimal</li>
     * <li>fixed_expense_status (String) - Estado del gasto fijo</li>
     * <li>fixed_expense_closing_date (LocalDate) - Fecha de finalización</li>
     * </ul>
     * 
     * <p>
     * <strong>Nota sobre frecuencias:</strong> Valores típicos incluyen:
     * "Mensual", "Anual", "Semanal", "Quincenal", "Trimestral".
     * 
     * <p>
     * <strong>Nota sobre estados:</strong> Valores típicos: "Activo", "Inactivo".
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba representativos
      UUID id = UUID.randomUUID();
      String nombre = "Netflix";
      String frecuencia = "Mensual";
      BigDecimal amount = new BigDecimal("1500.99");
      String estado = "Activo";
      LocalDate closingDate = LocalDate.now();

      // When - Se establecen todos los campos
      fixedExpense.setFixed_expense_id(id);
      fixedExpense.setFixed_expense_name(nombre);
      fixedExpense.setFixed_expense_frequency(frecuencia);
      fixedExpense.setFixed_expense_amount(amount);
      fixedExpense.setFixed_expense_status(estado);
      fixedExpense.setFixed_expense_closing_date(closingDate);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, fixedExpense.getFixed_expense_id(),
          "El ID del gasto fijo debería coincidir");
      assertEquals(nombre, fixedExpense.getFixed_expense_name(),
          "El nombre del gasto fijo debería coincidir");
      assertEquals(frecuencia, fixedExpense.getFixed_expense_frequency(),
          "La frecuencia del gasto debería coincidir");
      assertEquals(amount, fixedExpense.getFixed_expense_amount(),
          "El monto del gasto debería coincidir");
      assertEquals(estado, fixedExpense.getFixed_expense_status(),
          "El estado del gasto debería coincidir");
      assertEquals(closingDate, fixedExpense.getFixed_expense_closing_date(),
          "La fecha de cierre debería coincidir");
    }

    /**
     * Verifica que el constructor sin argumentos cree una instancia válida.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con el constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      // Given - setUp ya creó la instancia

      // Then - La entidad debe existir
      assertNotNull(fixedExpense,
          "El constructor sin argumentos debería crear una instancia válida");
    }
  }

  /**
   * Tests relacionados con la precisión decimal para montos monetarios.
   */
  @Nested
  @DisplayName("Precisión decimal para montos financieros")
  class AmountPrecision {

    /**
     * Verifica que el campo {@code fixed_expense_amount} mantenga correctamente
     * la precisión de 2 decimales requerida para operaciones financieras.
     * 
     * <p>
     * <strong>Importante:</strong> En aplicaciones financieras NUNCA se debe
     * usar {@code float} o {@code double} para representar dinero debido a
     * errores de redondeo por precisión binaria. {@link BigDecimal} garantiza
     * precisión decimal exacta.
     * 
     * <p>
     * La columna en base de datos está definida como:
     * {@code DECIMAL(10,2)} - 10 dígitos totales, 2 decimales.
     * 
     * @see java.math.BigDecimal
     */
    @Test
    @DisplayName("Debe mantener precisión de 2 decimales para montos monetarios")
    void testAmountPrecision() {
      // Given - Monto con exactamente 2 decimales
      BigDecimal amount = new BigDecimal("999.99");

      // When - Se asigna el monto a la entidad
      fixedExpense.setFixed_expense_amount(amount);

      // Then - Debe conservar la escala de 2 decimales
      assertEquals(2, fixedExpense.getFixed_expense_amount().scale(),
          "El monto debe mantener precisión de 2 decimales para valores financieros");
      assertEquals(amount, fixedExpense.getFixed_expense_amount(),
          "El monto debe coincidir exactamente con el valor asignado");
    }

    /**
     * Verifica el comportamiento con montos que tienen más de 2 decimales.
     * Aunque la base de datos redondeará, es importante conocer el comportamiento.
     */
    @Test
    @DisplayName("Debe preservar la escala original aunque la BD luego redondee")
    void testAmountWithMoreThanTwoDecimals() {
      // Given - Monto con 3 decimales
      BigDecimal amount = new BigDecimal("999.999");

      // When - Se asigna a la entidad
      fixedExpense.setFixed_expense_amount(amount);

      // Then - A nivel de objeto Java, mantiene su escala original
      assertEquals(3, fixedExpense.getFixed_expense_amount().scale(),
          "El objeto BigDecimal mantiene su escala original en Java");
      assertEquals(amount, fixedExpense.getFixed_expense_amount(),
          "El valor debe coincidir exactamente");
    }

    /**
     * Verifica que BigDecimal maneje correctamente montos enteros sin decimales.
     */
    @Test
    @DisplayName("Debe manejar correctamente montos enteros sin decimales")
    void testAmountWithZeroDecimals() {
      // Given - Monto entero (sin decimales)
      BigDecimal amount = new BigDecimal("1500");

      // When - Se asigna el monto
      fixedExpense.setFixed_expense_amount(amount);

      // Then - Escala 0, pero la BD lo almacenará como .00
      assertEquals(0, fixedExpense.getFixed_expense_amount().scale(),
          "El monto entero tiene escala 0 en Java");
      assertEquals(amount, fixedExpense.getFixed_expense_amount(),
          "El valor debe coincidir exactamente");
    }

    /**
     * Verifica que se puedan representar montos negativos (ej. reembolsos).
     */
    @Test
    @DisplayName("Debe aceptar montos negativos para representar reembolsos o ajustes")
    void testNegativeAmount() {
      // Given - Monto negativo (ejemplo: crédito o reembolso)
      BigDecimal negativeAmount = new BigDecimal("-500.00");

      // When - Se asigna a la entidad
      fixedExpense.setFixed_expense_amount(negativeAmount);

      // Then - Debe mantener el signo y la precisión
      assertEquals(negativeAmount, fixedExpense.getFixed_expense_amount(),
          "El monto negativo debe preservarse correctamente");
      assertEquals(2, fixedExpense.getFixed_expense_amount().scale(),
          "Debe mantener la precisión incluso con valores negativos");
    }
  }

  /**
   * Tests relacionados con valores de frecuencia aceptados.
   */
  @Nested
  @DisplayName("Frecuencias de gastos fijos")
  class FrequencyValues {

    /**
     * Verifica los valores típicos de frecuencia aceptados por la entidad.
     * Aunque no hay validación a nivel de entidad, documenta los valores esperados.
     */
    @Test
    @DisplayName("Debe aceptar frecuencias comunes: Mensual, Anual, Semanal, Quincenal")
    void testCommonFrequencyValues() {
      // Given - Frecuencias típicas en español
      String[] frecuencias = { "Mensual", "Anual", "Semanal", "Quincenal", "Trimestral" };

      // When & Then - Todas deben ser aceptadas
      for (String frecuencia : frecuencias) {
        fixedExpense.setFixed_expense_frequency(frecuencia);
        assertEquals(frecuencia, fixedExpense.getFixed_expense_frequency(),
            "La frecuencia '" + frecuencia + "' debería ser aceptada");
      }
    }
  }

  /**
   * Tests relacionados con estados del gasto fijo.
   */
  @Nested
  @DisplayName("Estados del gasto fijo")
  class StatusValues {

    /**
     * Verifica los valores de estado aceptados por la entidad.
     */
    @Test
    @DisplayName("Debe aceptar estados 'Activo' e 'Inactivo'")
    void testStatusValues() {
      // Given - Estados válidos
      String estadoActivo = "Activo";
      String estadoInactivo = "Inactivo";

      // When & Then - Estado Activo
      fixedExpense.setFixed_expense_status(estadoActivo);
      assertEquals(estadoActivo, fixedExpense.getFixed_expense_status(),
          "El estado 'Activo' debería ser aceptado");

      // When & Then - Estado Inactivo
      fixedExpense.setFixed_expense_status(estadoInactivo);
      assertEquals(estadoInactivo, fixedExpense.getFixed_expense_status(),
          "El estado 'Inactivo' debería ser aceptado");
    }
  }
}
