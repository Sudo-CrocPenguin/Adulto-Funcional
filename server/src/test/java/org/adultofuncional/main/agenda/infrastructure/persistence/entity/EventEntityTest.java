package org.adultofuncional.main.agenda.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la entidad JPA {@link EventEntity}.
 *
 * <p>Casos cubiertos:
 * <ul>
 *   <li>Valores por defecto: prioridad {@code "Media"}, estado {@code "Pendiente"}</li>
 *   <li>Getters y setters de todos los campos</li>
 *   <li>Manejo de campos {@code LocalDate} y {@code LocalDateTime}</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@DisplayName("EventEntity")
class EventEntityTest {

  private EventEntity event;

  @BeforeEach
  void setUp() {
    event = new EventEntity();
  }

  @Nested
  @DisplayName("Valores por defecto")
  class DefaultValues {

    /**
     * Verifica que la prioridad es {@code "Media"} y el estado es {@code "Pendiente"}.
     */
    @Test
    @DisplayName("Debe inicializar prioridad como 'Media' y estado como 'Pendiente'")
    void testDefaultValues() {
      assertEquals("Media", event.getEvent_priority());
      assertEquals("Pendiente", event.getEvent_status());
    }

    /**
     * Verifica que el constructor sin argumentos crea una instancia no null.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      assertNotNull(event);
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
      LocalDate date = LocalDate.now();
      LocalDateTime reminder = LocalDateTime.now();
      LocalDateTime startHour = LocalDateTime.now();
      LocalDateTime endHour = startHour.plusHours(1);

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

    /**
     * Verifica el manejo correcto de campos {@code LocalDate} y {@code LocalDateTime}.
     */
    @Test
    @DisplayName("Debe manejar correctamente campos de fecha y hora")
    void testDateTimeFields() {
      LocalDate fechaEvento = LocalDate.of(2026, 12, 31);
      LocalDateTime recordatorio = LocalDateTime.of(2026, 12, 30, 9, 0);
      LocalDateTime horaInicio = LocalDateTime.of(2026, 12, 31, 10, 0);
      LocalDateTime horaFin = LocalDateTime.of(2026, 12, 31, 11, 30);

      event.setEvent_date(fechaEvento);
      event.setEvent_reminder(recordatorio);
      event.setEvent_start_hour(horaInicio);
      event.setEvent_end_hour(horaFin);

      assertEquals(fechaEvento, event.getEvent_date());
      assertEquals(recordatorio, event.getEvent_reminder());
      assertEquals(horaInicio, event.getEvent_start_hour());
      assertEquals(horaFin, event.getEvent_end_hour());
    }
  }
}
