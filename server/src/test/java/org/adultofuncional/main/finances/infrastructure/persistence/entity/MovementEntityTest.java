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
 * Pruebas unitarias de la entidad JPA {@link MovementEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Callback {@code @PrePersist} establece {@code movement_register_date}</li>
 *   <li>Getters y setters de todos los campos</li>
 *   <li>Tipos de movimiento: {@code "Ingreso"} y {@code "Egreso"} (máx. 7 chars)</li>
 *   <li>Precisión decimal de {@code movement_amount} (DECIMAL(10,2))</li>
 *   <li>Independencia entre fecha de registro y fecha del movimiento</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("MovementEntity")
class MovementEntityTest {

  private MovementEntity movement;

  @BeforeEach
  void setUp() {
    movement = new MovementEntity();
  }

  @Nested
  @DisplayName("Ciclo de vida JPA")
  class LifecycleCallbacks {

    /**
     * Verifica que {@code onCreate()} establece {@code movement_register_date}.
     */
    @Test
    @DisplayName("Debe establecer movement_register_date en @PrePersist")
    void testPrePersistSetsRegisterDate() {
      movement.onCreate();
      assertNotNull(movement.getMovement_register_date());
      assertTrue(movement.getMovement_register_date() instanceof LocalDateTime);
    }
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
      String tipo = "Ingreso";
      BigDecimal amount = new BigDecimal("250.50");
      LocalDateTime registerDate = LocalDateTime.now();
      String descripcion = "Pago de salario";
      LocalDate date = LocalDate.now();

      movement.setMovement_id(id);
      movement.setMovement_type(tipo);
      movement.setMovement_amount(amount);
      movement.setMovement_register_date(registerDate);
      movement.setMovement_description(descripcion);
      movement.setMovement_date(date);

      assertEquals(id, movement.getMovement_id());
      assertEquals(tipo, movement.getMovement_type());
      assertEquals(amount, movement.getMovement_amount());
      assertEquals(registerDate, movement.getMovement_register_date());
      assertEquals(descripcion, movement.getMovement_description());
      assertEquals(date, movement.getMovement_date());
    }
  }

  @Nested
  @DisplayName("Tipos de movimiento")
  class MovementTypes {

    /**
     * Verifica que se aceptan los tipos {@code "Ingreso"} y {@code "Egreso"}.
     */
    @Test
    @DisplayName("Debe aceptar 'Ingreso' y 'Egreso'")
    void testMovementTypes() {
      movement.setMovement_type("Ingreso");
      assertEquals("Ingreso", movement.getMovement_type());

      movement.setMovement_type("Egreso");
      assertEquals("Egreso", movement.getMovement_type());
    }

    /**
     * Verifica que los tipos no exceden el límite de 7 caracteres
     * de la columna {@code movement_type VARCHAR(7)}.
     */
    @Test
    @DisplayName("Los tipos deben cumplir el límite de 7 caracteres")
    void testMovementTypeLengthConstraint() {
      assertTrue("Ingreso".length() <= 7);
      assertTrue("Egreso".length() <= 7);

      movement.setMovement_type("Ingreso");
      assertEquals(7, movement.getMovement_type().length());
    }
  }

  @Nested
  @DisplayName("Precisión decimal")
  class AmountPrecision {

    /**
     * Verifica que {@code movement_amount} mantiene precisión decimal correcta.
     */
    @Test
    @DisplayName("Debe mantener precisión decimal para montos monetarios")
    void testAmountPrecision() {
      movement.setMovement_amount(new BigDecimal("250.50"));
      assertEquals(2, movement.getMovement_amount().scale());

      movement.setMovement_amount(new BigDecimal("1000"));
      assertEquals(0, movement.getMovement_amount().scale());

      movement.setMovement_amount(new BigDecimal("-50.75"));
      assertEquals(new BigDecimal("-50.75"), movement.getMovement_amount());
    }
  }

  @Nested
  @DisplayName("Fechas del movimiento")
  class MovementDates {

    /**
     * Verifica que {@code movement_date} y {@code movement_register_date}
     * son independientes entre sí.
     */
    @Test
    @DisplayName("Fecha de registro y fecha de movimiento deben ser independientes")
    void testRegisterDateIndependentFromMovementDate() {
      movement.setMovement_date(LocalDate.of(2026, 1, 15));
      movement.setMovement_register_date(LocalDateTime.of(2026, 4, 16, 10, 30));

      assertEquals(LocalDate.of(2026, 1, 15), movement.getMovement_date());
      assertEquals(LocalDateTime.of(2026, 4, 16, 10, 30), movement.getMovement_register_date());
    }

    /**
     * Verifica que se aceptan fechas futuras para programar movimientos.
     */
    @Test
    @DisplayName("Debe aceptar fechas de movimiento futuras")
    void testFutureMovementDate() {
      LocalDate futureDate = LocalDate.now().plusMonths(1);
      movement.setMovement_date(futureDate);

      assertEquals(futureDate, movement.getMovement_date());
      assertTrue(movement.getMovement_date().isAfter(LocalDate.now()));
    }

    /**
     * Verifica que se aceptan fechas pasadas para registros retroactivos.
     */
    @Test
    @DisplayName("Debe aceptar fechas de movimiento pasadas")
    void testPastMovementDate() {
      LocalDate pastDate = LocalDate.now().minusMonths(1);
      movement.setMovement_date(pastDate);

      assertEquals(pastDate, movement.getMovement_date());
      assertTrue(movement.getMovement_date().isBefore(LocalDate.now()));
    }
  }
}
