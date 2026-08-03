package org.adultofuncional.main.agenda.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Verifica las invariantes que protegen todo evento, sin depender de HTTP ni
 * persistencia.
 *
 * <p><strong>Qué es:</strong> una prueba unitaria del agregado {@link Event}.</p>
 *
 * <p><strong>Para qué sirve:</strong> garantiza que ningún caso de uso pueda
 * crear o actualizar estados cronológicamente incoherentes o valores fuera
 * del catálogo permitido.</p>
 *
 * <p><strong>Cómo funciona:</strong> construye un evento válido y altera una
 * regla por vez, tanto durante la creación como durante la actualización.</p>
 */
class EventTest {

  private static final UUID CATEGORY_ID = UUID.randomUUID();
  private static final UUID ACCOUNT_ID = UUID.randomUUID();
  private static final LocalDate EVENT_DATE = LocalDate.of(2030, 6, 15);
  private static final LocalDateTime START = EVENT_DATE.atTime(10, 0);
  private static final LocalDateTime END = EVENT_DATE.atTime(11, 0);

  @Test
  void rejectsInvalidCatalogAndScheduleDuringCreation() {
    assertThatThrownBy(() -> create(2, START.minusHours(1), START, END, "Media", "Pendiente"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> create(0, START, START, END, "Media", "Pendiente"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> create(0, START.minusHours(1), START, START, "Media", "Pendiente"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> create(0, START.minusHours(1), START.plusDays(1), END.plusDays(1),
        "Media", "Pendiente"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> create(0, START.minusHours(1), START, END, "Urgente", "Pendiente"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> create(0, START.minusHours(1), START, END, "Media", "Desconocido"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void revalidatesTheCombinedStateDuringPartialUpdates() {
    Event event = create(0, START.minusHours(1), START, END, "Media", "Pendiente");

    assertThatThrownBy(() -> event.update(
        "Reunión", null, "Media", EVENT_DATE.plusDays(1), 0,
        START.minusHours(1), START, END, "Pendiente", CATEGORY_ID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> event.update(
        "Reunión", null, "Media", EVENT_DATE, 0,
        START, START, END, "Pendiente", CATEGORY_ID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> event.update(
        "Reunión", null, "Media", EVENT_DATE, 365,
        START.minusHours(1), START, START, "Pendiente", CATEGORY_ID))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void normalizesCivilHoursUsingTheSelectedIanaZone() {
    Event event = Event.create(
        "Reunión",
        null,
        "Media",
        EVENT_DATE,
        0,
        START.minusHours(1),
        START,
        END,
        ZoneId.of("America/Bogota"),
        "Pendiente",
        CATEGORY_ID,
        ACCOUNT_ID);

    assertThat(event.getZoneId()).isEqualTo(ZoneId.of("America/Bogota"));
    assertThat(event.getStartInstant()).isEqualTo(Instant.parse("2030-06-15T15:00:00Z"));
    assertThat(event.getEndInstant()).isEqualTo(Instant.parse("2030-06-15T16:00:00Z"));
    assertThat(event.getReminderInstant()).isEqualTo(Instant.parse("2030-06-15T14:00:00Z"));
  }

  private Event create(
      int frequency,
      LocalDateTime reminder,
      LocalDateTime start,
      LocalDateTime end,
      String priority,
      String status) {
    return Event.create(
        "Reunión",
        null,
        priority,
        EVENT_DATE,
        frequency,
        reminder,
        start,
        end,
        status,
        CATEGORY_ID,
        ACCOUNT_ID);
  }
}
