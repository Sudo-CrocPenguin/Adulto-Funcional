package org.adultofuncional.main.finances.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.finances.domain.enums.MovementType;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa un movimiento financiero.
 *
 * <p>
 * Un movimiento puede ser un ingreso o un egreso, y está asociado
 * a una categoría dentro del sistema.
 *
 * <p>
 * Este modelo encapsula únicamente las invariantes de negocio.
 * Las validaciones de formato pertenecen a los DTOs de la capa de aplicación.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movement {

  @EqualsAndHashCode.Include
  final UUID id;

  MovementType type;
  BigDecimal amount;
  UUID categoryId;
  String description;
  LocalDateTime date;

  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar métodos de fábrica.
   */
  private Movement(UUID id, MovementType type, BigDecimal amount,
      UUID categoryId, String description, LocalDateTime date,
      LocalDateTime createdAt) {

    if (id != null) {
      validateId(id);
    }

    validateAmount(amount);
    validateDate(date);
    validateCreatedAt(createdAt);

    this.id = id;
    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.description = description;
    this.date = date;
    this.createdAt = createdAt;
  }

  /**
   * Fábrica para crear un nuevo movimiento (antes de persistir).
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en el dominio,
   * garantizando que este sea dueño de su identidad.
   *
   * @param type        tipo de movimiento (Ingreso o Egreso)
   * @param amount      monto del movimiento
   * @param categoryId  identificador de la categoría
   * @param description descripción opcional
   * @param date        fecha del movimiento
   * @return instancia de Movement lista para persistir
   */
  public static Movement create(MovementType type, BigDecimal amount,
      UUID categoryId, String description, LocalDateTime date) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Movement(id, type, amount, categoryId, description, date, now);
  }

  /**
   * Fábrica para reconstituir un movimiento desde persistencia.
   *
   * @param id          identificador del movimiento
   * @param type        tipo
   * @param amount      monto
   * @param categoryId  categoría asociada
   * @param description descripción
   * @param date        fecha del movimiento
   * @param createdAt   fecha de creación original
   * @return instancia reconstituida
   */
  public static Movement reconstitute(UUID id, MovementType type,
      BigDecimal amount, UUID categoryId, String description,
      LocalDateTime date, LocalDateTime createdAt) {

    return new Movement(id, type, amount, categoryId, description, date, createdAt);
  }

  /**
   * Actualiza los datos del movimiento.
   *
   * @param type        nuevo tipo
   * @param amount      nuevo monto
   * @param categoryId  nueva categoría
   * @param description nueva descripción
   * @param date        nueva fecha
   */
  public void update(MovementType type, BigDecimal amount,
      UUID categoryId, String description, LocalDateTime date) {

    validateAmount(amount);
    validateDate(date);

    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.description = description;
    this.date = date;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
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

  private static void validateDate(LocalDateTime date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
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
