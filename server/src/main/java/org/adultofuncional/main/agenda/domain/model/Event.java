package org.adultofuncional.main.agenda.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa un evento de la agenda personal.
 *
 * <p>
 * Un evento permite organizar actividades, reuniones, recordatorios o
 * compromisos, con soporte para recurrencia, prioridad, estado y
 * categorización. Está asociado obligatoriamente a una cuenta propietaria
 * y a una categoría del ámbito {@code AGENDA}.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Validar que el título no esté vacío, las horas de inicio y fin sean
 * coherentes, la frecuencia pertenezca al catálogo soportado y el
 * recordatorio sea anterior al inicio.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create} para que
 * el dominio sea dueño de su identidad.</li>
 * <li>Permitir la actualización de todos sus campos editables mediante
 * {@link #update}, con revalidación de invariantes.</li>
 * <li>Proveer el método {@link #reconstitute} para reconstruir instancias
 * desde persistencia sin regenerar UUID.</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato y seguridad (longitud de textos, contenido
 * HTML) se aplican en los DTOs de la capa de aplicación. La entidad solo
 * garantiza que los valores obligatorios estén presentes y que las fechas
 * sean coherentes.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

  public static final ZoneId LEGACY_DEFAULT_ZONE = ZoneId.of("America/Bogota");

  private static final Set<String> ALLOWED_PRIORITIES = Set.of("Baja", "Media", "Alta");
  private static final Set<String> ALLOWED_STATUSES =
      Set.of("Pendiente", "Completado", "Cancelado", "Pospuesto");
  private static final Set<Integer> ALLOWED_FREQUENCIES = Set.of(0, 1, 7, 30, 365);

  /**
   * Identificador único del evento (UUID v7).
   * Generado en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /** Título del evento. No puede ser nulo ni vacío. */
  String title;

  /** Descripción detallada opcional. */
  String description;

  /**
   * Prioridad del evento.
   * Valores típicos: {@code "Baja"}, {@code "Media"}, {@code "Alta"}.
   * Por defecto {@code "Media"} en la base de datos.
   */
  String priority;

  /** Fecha calendario del evento. */
  LocalDate date;

  /**
   * Frecuencia de repetición en días.
   * {@code 0} indica evento único; valores positivos indican intervalo
   * de repetición (1 = diario, 7 = semanal, 30 = mensual, 365 = anual).
   */
  int frequency;

  /** Fecha y hora del recordatorio programado. */
  LocalDateTime reminder;

  /** Instante UTC normalizado del recordatorio. */
  Instant reminderInstant;

  /** Hora de inicio del evento. */
  LocalDateTime startHour;

  /** Instante UTC normalizado del inicio. */
  Instant startInstant;

  /** Hora de finalización del evento. */
  LocalDateTime endHour;

  /** Instante UTC normalizado del fin. */
  Instant endInstant;

  /** Zona IANA usada para interpretar y reconstruir las horas civiles. */
  ZoneId zoneId;

  /**
   * Estado del evento.
   * Valores típicos: {@code "Pendiente"}, {@code "Completado"},
   * {@code "Cancelado"}, {@code "Pospuesto"}.
   * Por defecto {@code "Pendiente"} en la base de datos.
   */
  String status;

  /** Identificador de la categoría asociada (FK a {@code categories}). */
  UUID categoryId;

  /** Identificador de la cuenta propietaria (FK a {@code accounts}). */
  UUID accountId;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private Event(UUID id, String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, ZoneId zoneId,
      Instant reminderInstant, Instant startInstant, Instant endInstant,
      String status,
      UUID categoryId, UUID accountId) {

    validateId(id);
    validateTitle(title);
    validatePriority(priority);
    validateDate(date);
    validateFrequency(frequency);
    validateZoneId(zoneId);
    ScheduleInstants schedule = normalizeSchedule(date, reminder, startHour, endHour, zoneId);
    validatePersistedInstants(schedule, reminderInstant, startInstant, endInstant);
    validateStatus(status);
    validateCategoryId(categoryId);
    validateAccountId(accountId);

    this.id = id;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.date = date;
    this.frequency = frequency;
    this.reminder = reminder;
    this.reminderInstant = reminderInstant == null ? schedule.reminder() : reminderInstant;
    this.startHour = startHour;
    this.startInstant = startInstant == null ? schedule.start() : startInstant;
    this.endHour = endHour;
    this.endInstant = endInstant == null ? schedule.end() : endInstant;
    this.zoneId = zoneId;
    this.status = status;
    this.categoryId = categoryId;
    this.accountId = accountId;
  }

  /**
   * Método de fábrica para crear un nuevo evento antes de persistirlo.
   *
   * <p>
   * Genera un UUID v7. La cuenta y la categoría deben haber sido validadas
   * en la capa de aplicación.
   *
   * @param title       título del evento (no nulo ni vacío).
   * @param description descripción opcional.
   * @param priority    prioridad (no nula ni vacía).
   * @param date        fecha calendario del evento (no nula).
   * @param frequency   días entre repeticiones (0, 1, 7, 30 o 365).
   * @param reminder    fecha y hora del recordatorio (no nula).
   * @param startHour   hora de inicio (no nula).
   * @param endHour     hora de finalización (no nula y estrictamente posterior
   *                    a {@code startHour}).
   * @param status      estado inicial (no nulo ni vacío).
   * @param categoryId  identificador de la categoría asociada (no nulo).
   * @param accountId   identificador de la cuenta propietaria (no nulo).
   * @return instancia de {@code Event} lista para persistir.
   * @throws IllegalArgumentException si algún parámetro obligatorio es nulo o
   *                                  incumple las invariantes.
   */
  public static Event create(String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, String status,
      UUID categoryId, UUID accountId) {

    return create(title, description, priority, date, frequency,
        reminder, startHour, endHour, LEGACY_DEFAULT_ZONE, status,
        categoryId, accountId);
  }

  /** Crea un evento interpretando sus horas civiles en una zona IANA. */
  public static Event create(String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, ZoneId zoneId, String status,
      UUID categoryId, UUID accountId) {

    UUID id = Generators.timeBasedEpochGenerator().generate();

    return new Event(
        id,
        title,
        description,
        priority,
        date,
        frequency,
        reminder,
        startHour,
        endHour,
        zoneId,
        null,
        null,
        null,
        status,
        categoryId,
        accountId);
  }

  /**
   * Método de fábrica para reconstituir un evento desde la capa de
   * persistencia.
   *
   * @param id          identificador existente.
   * @param title       título.
   * @param description descripción.
   * @param priority    prioridad.
   * @param date        fecha calendario.
   * @param frequency   frecuencia.
   * @param reminder    recordatorio.
   * @param startHour   hora de inicio.
   * @param endHour     hora de fin.
   * @param status      estado.
   * @param categoryId  categoría asociada.
   * @param accountId   cuenta propietaria.
   * @return instancia reconstituida.
   */
  public static Event reconstitute(UUID id, String title,
      String description, String priority, LocalDate date,
      int frequency, LocalDateTime reminder,
      LocalDateTime startHour, LocalDateTime endHour,
      String status, UUID categoryId, UUID accountId) {

    return reconstitute(id, title, description, priority, date, frequency,
        reminder, startHour, endHour, LEGACY_DEFAULT_ZONE,
        null, null, null, status, categoryId, accountId);
  }

  /** Reconstruye un evento con sus horas civiles, zona e instantes persistidos. */
  public static Event reconstitute(UUID id, String title,
      String description, String priority, LocalDate date,
      int frequency, LocalDateTime reminder,
      LocalDateTime startHour, LocalDateTime endHour,
      ZoneId zoneId, Instant reminderInstant, Instant startInstant, Instant endInstant,
      String status, UUID categoryId, UUID accountId) {

    return new Event(id, title, description, priority, date,
        frequency, reminder, startHour, endHour, zoneId,
        reminderInstant, startInstant, endInstant, status,
        categoryId, accountId);
  }

  /**
   * Actualiza todos los datos editables del evento.
   *
   * <p>
   * Se aplican las mismas validaciones que en la creación.
   *
   * @param title       nuevo título (no nulo ni vacío).
   * @param description nueva descripción (puede ser nula).
   * @param priority    nueva prioridad (no nula ni vacía).
   * @param date        nueva fecha calendario (no nula).
   * @param frequency   nueva frecuencia (0, 1, 7, 30 o 365).
   * @param reminder    nuevo recordatorio (no nulo).
   * @param startHour   nueva hora de inicio (no nula).
   * @param endHour     nueva hora de fin (no nula y estrictamente posterior a
   *                    {@code startHour}).
   * @param status      nuevo estado (no nulo ni vacío).
   * @param categoryId  nuevo identificador de categoría (no nulo).
   * @throws IllegalArgumentException si alguna validación falla.
   */
  public void update(String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, String status,
      UUID categoryId) {

    update(title, description, priority, date, frequency,
        reminder, startHour, endHour, zoneId, status, categoryId);
  }

  /** Actualiza el evento y recalcula sus instantes al cambiar horario o zona. */
  public void update(String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, ZoneId zoneId, String status,
      UUID categoryId) {

    validateTitle(title);
    validatePriority(priority);
    validateDate(date);
    validateFrequency(frequency);
    validateZoneId(zoneId);
    ScheduleInstants schedule = normalizeSchedule(date, reminder, startHour, endHour, zoneId);
    validateStatus(status);
    validateCategoryId(categoryId);

    this.title = title;
    this.description = description;
    this.priority = priority;
    this.date = date;
    this.frequency = frequency;
    this.reminder = reminder;
    this.reminderInstant = schedule.reminder();
    this.startHour = startHour;
    this.startInstant = schedule.start();
    this.endHour = endHour;
    this.endInstant = schedule.end();
    this.zoneId = zoneId;
    this.status = status;
    this.categoryId = categoryId;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or empty");
    }
  }

  private static void validatePriority(String priority) {
    if (!ALLOWED_PRIORITIES.contains(priority)) {
      throw new IllegalArgumentException("Priority is not allowed");
    }
  }

  private static void validateDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
  }

  private static void validateFrequency(int frequency) {
    if (!ALLOWED_FREQUENCIES.contains(frequency)) {
      throw new IllegalArgumentException("Frequency is not allowed");
    }
  }

  private static ScheduleInstants normalizeSchedule(
      LocalDate date,
      LocalDateTime reminder,
      LocalDateTime startHour,
      LocalDateTime endHour,
      ZoneId zoneId) {
    if (reminder == null) {
      throw new IllegalArgumentException("Reminder cannot be null");
    }
    if (startHour == null) {
      throw new IllegalArgumentException("Start hour cannot be null");
    }
    if (endHour == null) {
      throw new IllegalArgumentException("End hour cannot be null");
    }
    if (reminder.getNano() % 1_000 != 0
        || startHour.getNano() % 1_000 != 0
        || endHour.getNano() % 1_000 != 0) {
      throw new IllegalArgumentException("Event times support at most microsecond precision");
    }
    if (!startHour.toLocalDate().equals(date) || !endHour.toLocalDate().equals(date)) {
      throw new IllegalArgumentException("Event hours must match event date");
    }
    Instant reminderInstant = toInstant(reminder, zoneId);
    Instant startInstant = toInstant(startHour, zoneId);
    Instant endInstant = toInstant(endHour, zoneId);
    if (!startInstant.isBefore(endInstant)) {
      throw new IllegalArgumentException("Start hour must be before end hour");
    }
    if (!reminderInstant.isBefore(startInstant)) {
      throw new IllegalArgumentException("Reminder must be before start hour");
    }
    return new ScheduleInstants(reminderInstant, startInstant, endInstant);
  }

  private static Instant toInstant(LocalDateTime localDateTime, ZoneId zoneId) {
    List<ZoneOffset> validOffsets = zoneId.getRules().getValidOffsets(localDateTime);
    if (validOffsets.isEmpty()) {
      throw new IllegalArgumentException("Event time does not exist in the selected time zone");
    }
    return localDateTime.toInstant(validOffsets.getFirst());
  }

  private static void validatePersistedInstants(
      ScheduleInstants normalized,
      Instant reminderInstant,
      Instant startInstant,
      Instant endInstant) {
    if (reminderInstant != null && !reminderInstant.equals(normalized.reminder())) {
      throw new IllegalArgumentException("Reminder instant does not match local schedule");
    }
    if (startInstant != null && !startInstant.equals(normalized.start())) {
      throw new IllegalArgumentException("Start instant does not match local schedule");
    }
    if (endInstant != null && !endInstant.equals(normalized.end())) {
      throw new IllegalArgumentException("End instant does not match local schedule");
    }
  }

  private static void validateZoneId(ZoneId zoneId) {
    if (zoneId == null) {
      throw new IllegalArgumentException("ZoneId cannot be null");
    }
  }

  private static void validateStatus(String status) {
    if (!ALLOWED_STATUSES.contains(status)) {
      throw new IllegalArgumentException("Status is not allowed");
    }
  }

  private static void validateCategoryId(UUID categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("CategoryId cannot be null");
    }
  }

  private static void validateAccountId(UUID accountId) {
    if (accountId == null) {
      throw new IllegalArgumentException("AccountId cannot be null");
    }
  }

  private record ScheduleInstants(Instant reminder, Instant start, Instant end) {
  }
}
