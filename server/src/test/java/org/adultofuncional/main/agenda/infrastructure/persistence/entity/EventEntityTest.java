package org.adultofuncional.main.agenda.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventEntityTest {

  private EventEntity event;

  @BeforeEach
  void setUp() {
    event = new EventEntity();
  }

  @Test
  void testDefaultValues() {
    assertEquals("Media", event.getEvent_priority());
    assertEquals("Pendiente", event.getEvent_status());
  }

  @Test
  void testSettersAndGetters() {
    UUID id = UUID.randomUUID();
    LocalDate date = LocalDate.now();
    LocalDateTime reminder = LocalDateTime.now();
    LocalDateTime startHour = LocalDateTime.now();
    LocalDateTime endHour = LocalDateTime.now().plusHours(1);

    event.setEvent_id(id);
    event.setEvent_title("Reunión importante");
    event.setEvent_priority("Alta");
    event.setEvent_date(date);
    event.setEvent_frequency(1);
    event.setEvent_reminder(reminder);
    event.setEvent_start_hour(startHour);
    event.setEvent_end_hour(endHour);
    event.setEvent_description("Descripción de la reunión");
    event.setEvent_status("Completado");

    assertEquals(id, event.getEvent_id());
    assertEquals("Reunión importante", event.getEvent_title());
    assertEquals("Alta", event.getEvent_priority());
    assertEquals(date, event.getEvent_date());
    assertEquals(1, event.getEvent_frequency());
    assertEquals(reminder, event.getEvent_reminder());
    assertEquals(startHour, event.getEvent_start_hour());
    assertEquals(endHour, event.getEvent_end_hour());
    assertEquals("Descripción de la reunión", event.getEvent_description());
    assertEquals("Completado", event.getEvent_status());
  }
}
