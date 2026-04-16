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
 * Tests unitarios para la entidad {@link EventEntity}.
 * 
 * <p>
 * Verifica el comportamiento correcto de:
 * <ul>
 * <li>Valores por defecto de campos con inicialización en declaración</li>
 * <li>Funcionamiento de getters y setters para todos los campos</li>
 * <li>Manejo de fechas y horas (LocalDate, LocalDateTime)</li>
 * </ul>
 * 
 * <p>
 * La entidad representa eventos de agenda con soporte para:
 * <ul>
 * <li>Prioridades configurables (Baja, Media, Alta)</li>
 * <li>Estados de seguimiento (Pendiente, Completado, Cancelado)</li>
 * <li>Frecuencia de repetición (0 = sin repetición, 1+ = intervalo en
 * días)</li>
 * <li>Recordatorios y duración con hora de inicio y fin</li>
 * </ul>
 * 
 * @author juan
 * @since 0.0.1
 */
@DisplayName("EventEntity - Tests de entidad JPA para eventos de agenda")
class EventEntityTest {

  private EventEntity event;

  @BeforeEach
  void setUp() {
    event = new EventEntity();
  }

  /**
   * Tests relacionados con los valores por defecto de la entidad.
   */
  @Nested
  @DisplayName("Valores por defecto")
  class DefaultValues {

    /**
     * Verifica que los campos con inicialización en la declaración
     * tengan los valores predeterminados correctos al instanciar la entidad.
     * 
     * <p>
     * Valores esperados:
     * <ul>
     * <li>Prioridad: "Media"</li>
     * <li>Estado: "Pendiente"</li>
     * </ul>
     * 
     * <p>
     * Estos valores por defecto permiten crear eventos sin necesidad
     * de especificar explícitamente prioridad o estado inicial.
     */
    @Test
    @DisplayName("Debe inicializar prioridad como 'Media' y estado como 'Pendiente'")
    void testDefaultValues() {
      // Given - Entidad recién instanciada (en setUp)

      // When - Se consultan los valores por defecto

      // Then - Deben coincidir con los valores definidos en la declaración
      assertEquals("Media", event.getEvent_priority(),
          "La prioridad por defecto debería ser 'Media'");
      assertEquals("Pendiente", event.getEvent_status(),
          "El estado por defecto debería ser 'Pendiente'");
    }

    /**
     * Verifica que el constructor sin argumentos cree una instancia válida.
     * Complementa la verificación de valores por defecto.
     */
    @Test
    @DisplayName("Debe crear una instancia no nula con el constructor vacío")
    void testNoArgsConstructorCreatesValidInstance() {
      // Given - setUp ya creó la instancia

      // Then - La entidad debe existir
      assertNotNull(event,
          "El constructor sin argumentos debería crear una instancia válida");
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
     * <li>event_id (UUID) - Identificador único</li>
     * <li>event_title (String) - Título del evento (máx. 35 caracteres)</li>
     * <li>event_priority (String) - Prioridad (Baja/Media/Alta)</li>
     * <li>event_date (LocalDate) - Fecha del evento</li>
     * <li>event_frequency (Integer) - Frecuencia de repetición en días</li>
     * <li>event_reminder (LocalDateTime) - Fecha/hora del recordatorio</li>
     * <li>event_start_hour (LocalDateTime) - Hora de inicio</li>
     * <li>event_end_hour (LocalDateTime) - Hora de finalización</li>
     * <li>event_description (String) - Descripción detallada (TEXT)</li>
     * <li>event_status (String) - Estado (Pendiente/Completado/Cancelado)</li>
     * </ul>
     * 
     * <p>
     * Nótese que las relaciones (@ManyToOne con AccountEntity y CategoryEntity)
     * no se prueban aquí, sino en tests de integración con base de datos.
     */
    @Test
    @DisplayName("Debe establecer y recuperar correctamente todos los campos")
    void testSettersAndGetters() {
      // Given - Valores de prueba representativos
      UUID id = UUID.randomUUID();
      LocalDate date = LocalDate.now();
      LocalDateTime reminder = LocalDateTime.now();
      LocalDateTime startHour = LocalDateTime.now();
      LocalDateTime endHour = startHour.plusHours(1);

      String titulo = "Reunión importante";
      String prioridad = "Alta";
      Integer frecuencia = 1; // Se repite cada 1 día
      String descripcion = "Descripción de la reunión con cliente";
      String estado = "Completado";

      // When - Se establecen todos los campos
      event.setEvent_id(id);
      event.setEvent_title(titulo);
      event.setEvent_priority(prioridad);
      event.setEvent_date(date);
      event.setEvent_frequency(frecuencia);
      event.setEvent_reminder(reminder);
      event.setEvent_start_hour(startHour);
      event.setEvent_end_hour(endHour);
      event.setEvent_description(descripcion);
      event.setEvent_status(estado);

      // Then - Los getters deben devolver exactamente los valores establecidos
      assertEquals(id, event.getEvent_id(),
          "El ID del evento debería coincidir");
      assertEquals(titulo, event.getEvent_title(),
          "El título del evento debería coincidir");
      assertEquals(prioridad, event.getEvent_priority(),
          "La prioridad del evento debería coincidir");
      assertEquals(date, event.getEvent_date(),
          "La fecha del evento debería coincidir");
      assertEquals(frecuencia, event.getEvent_frequency(),
          "La frecuencia de repetición debería coincidir");
      assertEquals(reminder, event.getEvent_reminder(),
          "La fecha/hora del recordatorio debería coincidir");
      assertEquals(startHour, event.getEvent_start_hour(),
          "La hora de inicio debería coincidir");
      assertEquals(endHour, event.getEvent_end_hour(),
          "La hora de finalización debería coincidir");
      assertEquals(descripcion, event.getEvent_description(),
          "La descripción del evento debería coincidir");
      assertEquals(estado, event.getEvent_status(),
          "El estado del evento debería coincidir");
    }

    /**
     * Verifica el comportamiento específico de los campos de fecha/hora.
     * Dado que son tipos inmutables, se prueba la asignación directa.
     */
    @Test
    @DisplayName("Debe manejar correctamente los campos de tipo LocalDate y LocalDateTime")
    void testDateTimeFields() {
      // Given - Fechas y horas de prueba
      LocalDate fechaEvento = LocalDate.of(2026, 12, 31);
      LocalDateTime recordatorio = LocalDateTime.of(2026, 12, 30, 9, 0);
      LocalDateTime horaInicio = LocalDateTime.of(2026, 12, 31, 10, 0);
      LocalDateTime horaFin = LocalDateTime.of(2026, 12, 31, 11, 30);

      // When - Se asignan los valores
      event.setEvent_date(fechaEvento);
      event.setEvent_reminder(recordatorio);
      event.setEvent_start_hour(horaInicio);
      event.setEvent_end_hour(horaFin);

      // Then - Los valores deben coincidir exactamente
      assertEquals(fechaEvento, event.getEvent_date(),
          "La fecha del evento debería coincidir");
      assertEquals(recordatorio, event.getEvent_reminder(),
          "La fecha/hora del recordatorio debería coincidir");
      assertEquals(horaInicio, event.getEvent_start_hour(),
          "La hora de inicio debería coincidir");
      assertEquals(horaFin, event.getEvent_end_hour(),
          "La hora de finalización debería coincidir");
    }
  }
}
