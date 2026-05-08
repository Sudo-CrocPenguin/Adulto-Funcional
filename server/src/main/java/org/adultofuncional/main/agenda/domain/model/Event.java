package org.adultofuncional.main.agenda.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * coherentes, la frecuencia no sea negativa y la fecha de creación no sea
 * futura.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create} para que
 * el dominio sea dueño de su identidad.</li>
 * <li>Permitir la actualización de todos sus campos editables mediante
 * {@link #update}, con revalidación de invariantes.</li>
 * <li>Proveer el método {@link #reconstitute} para reconstruir instancias
 * desde persistencia sin regenerar UUID ni marcas de tiempo.</li>
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

  /** Hora de inicio del evento. */
  LocalDateTime startHour;

  /** Hora de finalización del evento. */
  LocalDateTime endHour;

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
   * Fecha y hora exacta en que el evento fue registrado en el sistema.
   * Se genera automáticamente en {@link #create} y es inmutable.
   */
  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private Event(UUID id, String title, String description,
      String priority, LocalDate date, int frequency,
      LocalDateTime reminder, LocalDateTime startHour,
      LocalDateTime endHour, String status,
      UUID categoryId, UUID accountId, LocalDateTime createdAt) {

    validateId(id);
    validateTitle(title);
    validatePriority(priority);
    validateDate(date);
    validateFrequency(frequency);
    validateReminder(reminder);
    validateHours(startHour, endHour);
    validateStatus(status);
    validateCategoryId(categoryId);
    validateAccountId(accountId);
    validateCreatedAt(createdAt);

    this.id = id;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.date = date;
    this.frequency = frequency;
    this.reminder = reminder;
    this.startHour = startHour;
    this.endHour = endHour;
    this.status = status;
    this.categoryId = categoryId;
    this.accountId = accountId;
    this.createdAt = createdAt;
  }

  /**
   * Método de fábrica para crear un nuevo evento antes de persistirlo.
   *
   * <p>
   * Genera un UUID v7 y establece {@code createdAt} con la fecha y hora
   * actuales. La cuenta y la categoría deben haber sido validadas en la capa
   * de aplicación.
   *
   * @param title       título del evento (no nulo ni vacío).
   * @param description descripción opcional.
   * @param priority    prioridad (no nula ni vacía).
   * @param date        fecha calendario del evento (no nula).
   * @param frequency   días entre repeticiones (0 = único, no negativo).
   * @param reminder    fecha y hora del recordatorio (no nula).
   * @param startHour   hora de inicio (no nula).
   * @param endHour     hora de finalización (no nula, no anterior a
   *                    {@code startHour}).
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

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

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
        status,
        categoryId,
        accountId,
        now);
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
   * @param createdAt   fecha de registro original.
   * @return instancia reconstituida.
   */
  public static Event reconstitute(UUID id, String title,
      String description, String priority, LocalDate date,
      int frequency, LocalDateTime reminder,
      LocalDateTime startHour, LocalDateTime endHour,
      String status, UUID categoryId, UUID accountId,
      LocalDateTime createdAt) {

    return new Event(id, title, description, priority, date,
        frequency, reminder, startHour, endHour, status,
        categoryId, accountId, createdAt);
  }

  /**
   * Actualiza todos los datos editables del evento.
   *
   * <p>
   * Se aplican las mismas validaciones que en la creación. La fecha de
   * registro {@code createdAt} no se modifica.
   *
   * @param title       nuevo título (no nulo ni vacío).
   * @param description nueva descripción (puede ser nula).
   * @param priority    nueva prioridad (no nula ni vacía).
   * @param date        nueva fecha calendario (no nula).
   * @param frequency   nueva frecuencia (no negativa).
   * @param reminder    nuevo recordatorio (no nulo).
   * @param startHour   nueva hora de inicio (no nula).
   * @param endHour     nueva hora de fin (no nula, no anterior a
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

    validateTitle(title);
    validatePriority(priority);
    validateDate(date);
    validateFrequency(frequency);
    validateReminder(reminder);
    validateHours(startHour, endHour);
    validateStatus(status);
    validateCategoryId(categoryId);

    this.title = title;
    this.description = description;
    this.priority = priority;
    this.date = date;
    this.frequency = frequency;
    this.reminder = reminder;
    this.startHour = startHour;
    this.endHour = endHour;
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
    if (priority == null || priority.isBlank()) {
      throw new IllegalArgumentException("Priority cannot be null or empty");
    }
  }

  private static void validateDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
  }

  private static void validateFrequency(int frequency) {
    if (frequency < 0) {
      throw new IllegalArgumentException("Frequency cannot be negative");
    }
  }

  private static void validateReminder(LocalDateTime reminder) {
    if (reminder == null) {
      throw new IllegalArgumentException("Reminder cannot be null");
    }
  }

  private static void validateHours(LocalDateTime startHour, LocalDateTime endHour) {
    if (startHour == null) {
      throw new IllegalArgumentException("Start hour cannot be null");
    }
    if (endHour == null) {
      throw new IllegalArgumentException("End hour cannot be null");
    }
    if (endHour.isBefore(startHour)) {
      throw new IllegalArgumentException("End hour cannot be before start hour");
    }
  }

  private static void validateStatus(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("Status cannot be null or empty");
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

  private static void validateCreatedAt(LocalDateTime createdAt) {
    if (createdAt == null) {
      throw new IllegalArgumentException("CreatedAt cannot be null");
    }
    if (createdAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("CreatedAt cannot be in the future");
    }
  }
}
