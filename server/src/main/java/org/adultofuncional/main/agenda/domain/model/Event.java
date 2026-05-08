package org.adultofuncional.main.agenda.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa un evento dentro de la agenda.
 *
 * <p>
 * Un evento permite organizar actividades, reuniones, recordatorios
 * o compromisos personales del usuario.
 *
 * <p>
 * Este modelo encapsula únicamente las invariantes de negocio.
 * Las validaciones de formato pertenecen a los DTOs.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

  @EqualsAndHashCode.Include
  final UUID id;

  String title;
  String description;
  String location;

  LocalDateTime startDate;
  LocalDateTime endDate;

  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar métodos de fábrica.
   */
  private Event(UUID id, String title, String description,
      String location, LocalDateTime startDate,
      LocalDateTime endDate, LocalDateTime createdAt) {

    if (id != null) {
      validateId(id);
    }

    validateTitle(title);
    validateDates(startDate, endDate);
    validateCreatedAt(createdAt);

    this.id = id;
    this.title = title;
    this.description = description;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
    this.createdAt = createdAt;
  }

  /**
   * Fábrica para crear un nuevo evento.
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en el dominio.
   *
   * @param title       título del evento
   * @param description descripción opcional
   * @param location    ubicación opcional
   * @param startDate   fecha de inicio
   * @param endDate     fecha de finalización
   * @return instancia lista para persistir
   */
  public static Event create(String title, String description,
      String location, LocalDateTime startDate,
      LocalDateTime endDate) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Event(
        id,
        title,
        description,
        location,
        startDate,
        endDate,
        now);
  }

  /**
   * Fábrica para reconstituir un evento desde persistencia.
   *
   * @param id          identificador
   * @param title       título
   * @param description descripción
   * @param location    ubicación
   * @param startDate   fecha de inicio
   * @param endDate     fecha de finalización
   * @param createdAt   fecha de creación
   * @return instancia reconstituida
   */
  public static Event reconstitute(UUID id, String title,
      String description, String location,
      LocalDateTime startDate, LocalDateTime endDate,
      LocalDateTime createdAt) {

    return new Event(id, title, description,
        location, startDate, endDate, createdAt);
  }

  /**
   * Actualiza la información principal del evento.
   *
   * @param title       nuevo título
   * @param description nueva descripción
   * @param location    nueva ubicación
   * @param startDate   nueva fecha de inicio
   * @param endDate     nueva fecha de finalización
   */
  public void update(String title, String description,
      String location, LocalDateTime startDate,
      LocalDateTime endDate) {

    validateTitle(title);
    validateDates(startDate, endDate);

    this.title = title;
    this.description = description;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException(
          "Title cannot be null or empty");
    }
  }

  private static void validateDates(LocalDateTime startDate,
      LocalDateTime endDate) {

    if (startDate == null) {
      throw new IllegalArgumentException(
          "Start date cannot be null");
    }

    if (endDate == null) {
      throw new IllegalArgumentException(
          "End date cannot be null");
    }

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException(
          "End date cannot be before start date");
    }
  }

  private static void validateCreatedAt(LocalDateTime createdAt) {
    if (createdAt == null) {
      throw new IllegalArgumentException(
          "CreatedAt cannot be null");
    }

    if (createdAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException(
          "CreatedAt cannot be in the future");
    }
  }
}
