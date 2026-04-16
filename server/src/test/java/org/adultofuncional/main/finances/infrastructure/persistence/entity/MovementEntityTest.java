package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovementEntityTest {

  private MovementEntity movement;

  @BeforeEach
  void setUp() {
    movement = new MovementEntity();
  }

  @Test
  void testPrePersistSetsRegisterDate() {
    movement.onCreate();
    assertNotNull(movement.getMovement_register_date());
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    BigDecimal amount = new BigDecimal("250.50");
    LocalDate date = LocalDate.now();
    LocalDateTime registerDate = LocalDateTime.now();

    movement.setMovement_id(id);
    movement.setMovement_type("Ingreso");
    movement.setMovement_amount(amount);
    movement.setMovement_register_date(registerDate);
    movement.setMovement_description("Pago de salario");
    movement.setMovement_date(date);

    assertEquals(id, movement.getMovement_id());
    assertEquals("Ingreso", movement.getMovement_type());
    assertEquals(amount, movement.getMovement_amount());
    assertEquals(registerDate, movement.getMovement_register_date());
    assertEquals("Pago de salario", movement.getMovement_description());
    assertEquals(date, movement.getMovement_date());
  }

  @Test
  void testMovementTypes() {
    movement.setMovement_type("Ingreso");
    assertEquals("Ingreso", movement.getMovement_type());

    movement.setMovement_type("Egreso");
    assertEquals("Egreso", movement.getMovement_type());
  }
}
