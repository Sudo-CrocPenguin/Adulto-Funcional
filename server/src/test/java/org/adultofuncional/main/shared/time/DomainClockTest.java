package org.adultofuncional.main.shared.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.junit.jupiter.api.Test;

/** Verifica que los timestamps técnicos procedan del reloj UTC inyectado. */
class DomainClockTest {

  @Test
  void createsAccountsAndMovementsAtTheInjectedInstant() {
    Instant instant = Instant.parse("2026-08-03T17:30:45Z");
    Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
    LocalDateTime expectedMovement = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

    Account account = Account.create(
        "Ada",
        "Lovelace",
        "ada@example.com",
        "+573001234567",
        "hash",
        null,
        clock);
    Movement movement = Movement.create(
        MovementType.EXPENSE,
        new BigDecimal("10.00"),
        UUID.randomUUID(),
        account.getId(),
        "Prueba",
        LocalDate.of(2026, 8, 3),
        clock);

    assertThat(account.getCreatedAt()).isEqualTo(instant);
    assertThat(movement.getCreatedAt()).isEqualTo(expectedMovement);
  }
}
