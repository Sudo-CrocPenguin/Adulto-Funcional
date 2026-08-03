package org.adultofuncional.main.agenda.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.adultofuncional.main.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

class AgendaTimePolicyTest {

  private static final Clock CLOCK = Clock.fixed(
      Instant.parse("2030-06-15T03:00:00Z"),
      ZoneId.of("UTC"));

  @Test
  void usesTheConfiguredZoneWhenLegacyClientsDoNotSendOne() {
    AgendaTimePolicy policy = new AgendaTimePolicy(CLOCK, "America/Bogota");

    assertThat(policy.resolve(null)).isEqualTo(ZoneId.of("America/Bogota"));
    assertThat(policy.resolve("Europe/Madrid")).isEqualTo(ZoneId.of("Europe/Madrid"));
  }

  @Test
  void rejectsUnknownZonesAndPastDatesUsingTheInjectedClock() {
    AgendaTimePolicy policy = new AgendaTimePolicy(CLOCK, "America/Bogota");

    assertThatThrownBy(() -> policy.resolve("Bogota/Desconocida"))
        .isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> policy.requirePresentOrFuture(
        LocalDate.of(2030, 6, 13),
        ZoneId.of("America/Bogota")))
        .isInstanceOf(BusinessException.class);
  }
}
