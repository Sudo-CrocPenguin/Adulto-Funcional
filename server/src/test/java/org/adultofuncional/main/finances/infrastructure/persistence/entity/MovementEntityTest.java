package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para la entidad {@link MovementEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Callback de ciclo de vida JPA (@PrePersist) para fecha de registro
 * automática</li>
 * <li>Funcionamiento de getters y setters para todos los campos</li>
 * <li>Validación de tipos de movimiento (Ingreso/Egreso)</li>
 * <li>Manejo de precisión decimal para montos monetarios</li>
 * </ul>
 * 
 * <p>
 * <strong>Características destacadas:</strong>
 * <ul>
 * <li><b>Fecha de registro automática:</b> Se establece en {@code @PrePersist}
 * al momento de guardar</li>
 * <li><b>Fecha de movimiento independiente:</b> Permite registrar transacciones
 * en fechas pasadas o futuras</li>
 * <li><b>Tipos de movimiento:</b> "Ingreso" (entrada de dinero) o "Egreso"
 * (salida de dinero)</li>
 * <li><b>Precisión monetaria:</b> {@link BigDecimal} con escala 2 para valores
 * financieros exactos</li>
 * <li><b>Descripción opcional:</b> Campo TEXT para notas o detalles
 * adicionales</li>
 * </ul>
 * 
 * <p>
 * Esta entidad representa cada transacción financiera individual que afecta
 * el balance de una cuenta. Es la unidad fundamental del módulo de finanzas.
 * 
 * @author juan
 * @since 0.0.1
 * @see java.math.BigDecimal
 */
@DisplayName("MovementEntity - Tests de entidad JPA para movimientos financieros")
class MovementEntityTest {

  private MovementEntity movement;

  @BeforeEach
  void setUp() {
    movement = new MovementEntity();
  }

  /**
   * Tests relacionados con los callbacks del ciclo de vida JPA.
   */
  @Nested
  @DisplayName("Ciclo de vida JPA")
  class LifecycleCallbacks {

    /**
     * Verifica que el método anotado con {@code @PrePersist} establezca
     * automáticamente la fecha de registro antes de persistir la entidad.
     * 
     * <p>
     * Este timestamp permite:
     * <ul>
     * <li>Auditoría de cuándo se registró cada movimiento</li>
     * <li>Ordenamiento cronológico por fecha de registro</li>
     * <li>Diferenciar fecha de registro vs fecha del movimiento</li>
     * </ul>
     * 
     * <p>
     * <strong>Importante:</strong> El campo está marcado como
     * {@code updatable = false}, por lo que nunca se modificará
     * después de la inserción inicial.
     */
    @Test
    @DisplayName("Debe establecer movement_register_date automáticamente en @PrePersist")
    void testPrePersistSetsRegisterDate() {
      // Given - Entidad recién creada sin fecha de registro

      // When - Se ejecuta el callback de pre-persistencia
      movement.onCreate();

      // Then - La fecha de registro debe estar presente
      assertNotNull(movement.getMovement_register_date(),
          "La fecha de registro no debería ser null después de @PrePersist");
      assertTrue(movement.getMovement_register_date() instanceof LocalDateTime,
          "La fecha de registro debe ser de tipo LocalDateTime");
    }
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
     * <li>movement_id (UUID) - Identificador único</li>
     * <li>movement_type (String) - "Ingreso" o "Egreso" (máx. 7 chars)</li>
     * <li>movement_amount (BigDecimal) - Monto con precisión decimal</li>
     * <li>movement_register_date (LocalDateTime) - Fecha/hora de registro</li>
     * <li>movement_description (String) - Descripción opcional (TEXT)</li>
     * <li>movement_date (LocalDate) - Fecha efectiva del movimiento</li>
     * </ul>
     * 
     * <p>
     * <strong>Nota sobre relaciones:</strong> Los campos {@code account} y
     * {@code category} ({@code @ManyToOne}) se prueban en tests de integración
     * con base de datos.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba representativos
      UUID id = UUID.randomUUID();
      String tipo = "Ingreso";
      BigDecimal amount = new BigDecimal("250.50");
      LocalDateTime registerDate = LocalDateTime.now();
      String descripcion = "Pago de salario";
      LocalDate date = LocalDate.now();

      // When - Se establecen todos los campos
      movement.setMovement_id(id);
      movement.setMovement_type(tipo);
      movement.setMovement_amount(amount);
      movement.setMovement_register_date(registerDate);
      movement.setMovement_description(descripcion);
      movement.setMovement_date(date);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, movement.getMovement_id(),
          "El ID del movimiento debería coincidir");
      assertEquals(tipo, movement.getMovement_type(),
          "El tipo de movimiento debería coincidir");
      assertEquals(amount, movement.getMovement_amount(),
          "El monto del movimiento debería coincidir");
      assertEquals(registerDate, movement.getMovement_register_date(),
          "La fecha de registro debería coincidir");
      assertEquals(descripcion, movement.getMovement_description(),
          "La descripción del movimiento debería coincidir");
      assertEquals(date, movement.getMovement_date(),
          "La fecha del movimiento debería coincidir");
    }

    /**
     * Verifica que el constructor sin argumentos cree una instancia válida.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con el constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      // Given - setUp ya creó la instancia

      // Then - La entidad debe existir
      assertNotNull(movement,
          "El constructor sin argumentos debería crear una instancia válida");
    }
  }

  /**
   * Tests relacionados con los tipos de movimiento aceptados.
   */
  @Nested
  @DisplayName("Tipos de movimiento")
  class MovementTypes {

    /**
     * Verifica que la entidad acepte correctamente los dos tipos de movimiento
     * definidos en el dominio: "Ingreso" y "Egreso".
     * 
     * <p>
     * <strong>Definiciones:</strong>
     * <ul>
     * <li><b>Ingreso:</b> Entrada de dinero que aumenta el balance</li>
     * <li><b>Egreso:</b> Salida de dinero que disminuye el balance</li>
     * </ul>
     * 
     * <p>
     * La columna en base de datos está limitada a 7 caracteres:
     * "Ingreso" (7) y "Egreso" (6) cumplen con esta restricción.
     * 
     * <p>
     * <strong>Nota:</strong> En una versión futura, este campo podría
     * convertirse en un Enum para mayor type safety y validación.
     */
    @Test
    @DisplayName("Debe aceptar los tipos 'Ingreso' y 'Egreso' correctamente")
    void testMovementTypes() {
      // Given - Tipos válidos según el dominio
      String tipoIngreso = "Ingreso";
      String tipoEgreso = "Egreso";

      // When & Then - Debe aceptar "Ingreso"
      movement.setMovement_type(tipoIngreso);
      assertEquals(tipoIngreso, movement.getMovement_type(),
          "El tipo 'Ingreso' debería ser aceptado");

      // When & Then - Debe aceptar "Egreso"
      movement.setMovement_type(tipoEgreso);
      assertEquals(tipoEgreso, movement.getMovement_type(),
          "El tipo 'Egreso' debería ser aceptado");
    }

    /**
     * Verifica que los tipos de movimiento no excedan el límite de 7 caracteres
     * definido en la columna {@code movement_type VARCHAR(7)}.
     */
    @Test
    @DisplayName("Debe validar que los tipos no excedan 7 caracteres (restricción de BD)")
    void testMovementTypeLengthConstraint() {
      // Given - Tipos que cumplen con el límite
      String ingreso = "Ingreso"; // 7 caracteres - Límite exacto
      String egreso = "Egreso"; // 6 caracteres - Dentro del límite

      // Then - Verificar longitudes
      assertTrue(ingreso.length() <= 7,
          "'Ingreso' tiene 7 caracteres, debe ser <= 7");
      assertTrue(egreso.length() <= 7,
          "'Egreso' tiene 6 caracteres, debe ser <= 7");

      // When - Se asignan los valores
      movement.setMovement_type(ingreso);
      assertEquals(7, movement.getMovement_type().length(),
          "La longitud de 'Ingreso' debe ser exactamente 7");

      movement.setMovement_type(egreso);
      assertEquals(6, movement.getMovement_type().length(),
          "La longitud de 'Egreso' debe ser exactamente 6");
    }
  }

  /**
   * Tests relacionados con la precisión decimal para montos monetarios.
   */
  @Nested
  @DisplayName("Precisión decimal para montos financieros")
  class AmountPrecision {

    /**
     * Verifica que el campo {@code movement_amount} maneje correctamente
     * montos con precisión decimal para operaciones financieras.
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
     */
    @Test
    @DisplayName("Debe mantener precisión decimal para montos monetarios")
    void testAmountPrecision() {
      // Given - Montos con diferente escala decimal
      BigDecimal amountWithDecimals = new BigDecimal("250.50");
      BigDecimal amountInteger = new BigDecimal("1000");
      BigDecimal amountNegative = new BigDecimal("-50.75");

      // When & Then - Monto con decimales
      movement.setMovement_amount(amountWithDecimals);
      assertEquals(2, movement.getMovement_amount().scale(),
          "El monto con decimales debe mantener escala 2");
      assertEquals(amountWithDecimals, movement.getMovement_amount(),
          "El monto debe coincidir exactamente");

      // When & Then - Monto entero (sin decimales)
      movement.setMovement_amount(amountInteger);
      assertEquals(0, movement.getMovement_amount().scale(),
          "El monto entero tiene escala 0 en Java (la BD lo almacena como .00)");
      assertEquals(amountInteger, movement.getMovement_amount(),
          "El valor debe coincidir exactamente");

      // When & Then - Monto negativo (ej. corrección o reversión)
      movement.setMovement_amount(amountNegative);
      assertEquals(amountNegative, movement.getMovement_amount(),
          "El monto negativo debe preservarse correctamente");
    }
  }

  /**
   * Tests relacionados con las fechas del movimiento.
   */
  @Nested
  @DisplayName("Fechas del movimiento")
  class MovementDates {

    /**
     * Verifica la independencia entre la fecha de registro (automática)
     * y la fecha del movimiento (manual).
     * 
     * <p>
     * Esta separación permite:
     * <ul>
     * <li>Registrar movimientos con fecha pasada (carga retroactiva)</li>
     * <li>Programar movimientos futuros</li>
     * <li>Auditar cuándo se registró vs cuándo ocurrió</li>
     * </ul>
     */
    @Test
    @DisplayName("Debe permitir que fecha de registro y fecha de movimiento sean diferentes")
    void testRegisterDateIndependentFromMovementDate() {
      // Given - Fechas diferentes
      LocalDate movementDate = LocalDate.of(2026, 1, 15); // Fecha del movimiento
      LocalDateTime registerDate = LocalDateTime.of(2026, 4, 16, 10, 30); // Fecha de registro

      // When - Se asignan ambas fechas
      movement.setMovement_date(movementDate);
      movement.setMovement_register_date(registerDate);

      // Then - Deben ser independientes
      assertEquals(movementDate, movement.getMovement_date(),
          "La fecha del movimiento debería ser la asignada");
      assertEquals(registerDate, movement.getMovement_register_date(),
          "La fecha de registro debería ser la asignada");
    }

    /**
     * Verifica que se pueda registrar un movimiento con fecha futura.
     * Útil para programar transacciones o proyecciones.
     */
    @Test
    @DisplayName("Debe aceptar fechas de movimiento futuras para programación")
    void testFutureMovementDate() {
      // Given - Fecha futura
      LocalDate futureDate = LocalDate.now().plusMonths(1);

      // When - Se asigna fecha futura
      movement.setMovement_date(futureDate);

      // Then - Debe aceptarse sin restricciones
      assertEquals(futureDate, movement.getMovement_date(),
          "La fecha de movimiento futura debería ser aceptada");
      assertTrue(movement.getMovement_date().isAfter(LocalDate.now()),
          "La fecha debería ser posterior a hoy");
    }

    /**
     * Verifica que se pueda registrar un movimiento con fecha pasada.
     * Útil para cargas retroactivas o correcciones históricas.
     */
    @Test
    @DisplayName("Debe aceptar fechas de movimiento pasadas para registros retroactivos")
    void testPastMovementDate() {
      // Given - Fecha pasada
      LocalDate pastDate = LocalDate.now().minusMonths(1);

      // When - Se asigna fecha pasada
      movement.setMovement_date(pastDate);

      // Then - Debe aceptarse sin restricciones
      assertEquals(pastDate, movement.getMovement_date(),
          "La fecha de movimiento pasada debería ser aceptada");
      assertTrue(movement.getMovement_date().isBefore(LocalDate.now()),
          "La fecha debería ser anterior a hoy");
    }
  }
}
