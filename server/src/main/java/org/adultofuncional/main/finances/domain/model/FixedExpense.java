package org.adultofuncional.main.finances.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.Frequency;
import org.adultofuncional.main.finances.domain.enums.Status;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa un gasto fijo.
 *
 * <p>
 * Un gasto fijo es un egreso recurrente que ocurre con una frecuencia
 * determinada (mensual, semanal, etc.).
 *
 * <p>
 * Este modelo encapsula las invariantes de negocio relacionadas con
 * recurrencia, estado y validez de fechas.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FixedExpense {

  @EqualsAndHashCode.Include
  final UUID id;

  String name;
  BigDecimal amount;
  UUID categoryId;
  Frequency frequency;
  Status status;

  LocalDateTime startDate;
  LocalDateTime endDate;

  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar métodos de fábrica.
   */
  private FixedExpense(UUID id, String name, BigDecimal amount,
      UUID categoryId, Frequency frequency, Status status,
      LocalDateTime startDate, LocalDateTime endDate,
      LocalDateTime createdAt) {

    if (id != null) {
      validateId(id);
    }

    validateName(name);
    validateAmount(amount);
    validateFrequency(frequency);
    validateStatus(status);
    validateDates(startDate, endDate);
    validateCreatedAt(createdAt);

    this.id = id;
    this.name = name;
    this.amount = amount;
    this.categoryId = categoryId;
    this.frequency = frequency;
    this.status = status;
    this.startDate = startDate;
    this.endDate = endDate;
    this.createdAt = createdAt;
  }

  /**
   * Fábrica para crear un nuevo gasto fijo.
   *
   * @param name        nombre del gasto
   * @param amount      monto
   * @param categoryId  categoría asociada
   * @param frequency   frecuencia
   * @param startDate   fecha de inicio
   * @param endDate     fecha de finalización (opcional)
   * @return instancia lista para persistir
   */
  public static FixedExpense create(String name, BigDecimal amount,
      UUID categoryId, Frequency frequency,
      LocalDateTime startDate, LocalDateTime endDate) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new FixedExpense(
        id,
        name,
        amount,
        categoryId,
        frequency,
        Status.ACTIVE,
        startDate,
        endDate,
        now);
  }

  /**
   * Fábrica para reconstituir desde persistencia.
   */
  public static FixedExpense reconstitute(UUID id, String name,
      BigDecimal amount, UUID categoryId, Frequency frequency,
      Status status, LocalDateTime startDate,
      LocalDateTime endDate, LocalDateTime createdAt) {

    return new FixedExpense(id, name, amount, categoryId, frequency,
        status, startDate, endDate, createdAt);
  }

  /**
   * Actualiza los datos principales del gasto fijo.
   */
  public void update(String name, BigDecimal amount,
      UUID categoryId, Frequency frequency,
      LocalDateTime startDate, LocalDateTime endDate) {

    validateName(name);
    validateAmount(amount);
    validateFrequency(frequency);
    validateDates(startDate, endDate);

    this.name = name;
    this.amount = amount;
    this.categoryId = categoryId;
    this.frequency = frequency;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  /**
   * Activa el gasto fijo.
   */
  public void activate() {
    this.status = Status.ACTIVE;
  }

  /**
   * Desactiva el gasto fijo.
   */
  public void deactivate() {
    this.status = Status.INACTIVE;
  }

  /**
   * Indica si el gasto fijo está activo.
   */
  public boolean isActive() {
    return Status.ACTIVE.equals(this.status);
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  private static void validateAmount(BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
  }

  private static void validateFrequency(Frequency frequency) {
    if (frequency == null) {
      throw new IllegalArgumentException("Frequency cannot be null");
    }
  }

  private static void validateStatus(Status status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
  }

  private static void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    if (endDate != null && endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
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