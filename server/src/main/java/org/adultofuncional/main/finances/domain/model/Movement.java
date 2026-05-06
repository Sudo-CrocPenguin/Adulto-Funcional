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
 * Encapsula únicamente las invariantes de negocio relacionadas con el movimiento:
 * <ul>
 *   <li>El monto debe ser mayor que cero</li>
 *   <li>La fecha del movimiento no puede ser nula</li>
 *   <li>La fecha de creación no puede ser nula ni futura</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato (longitud de la descripción, caracteres especiales)
 * pertenecen a los DTOs de la capa de aplicación. La descripción es opcional
 * y puede ser {@code null}.
 *
 * <p>
 * Este modelo no contiene campos sensibles, por lo que se puede exponer
 * completamente en los DTOs de respuesta.
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
   * Constructor privado. Usar los métodos de fábrica.
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
   * Fábrica para crear un nuevo movimiento (antes de persistirlo).
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en la aplicación,
   * garantizando que el dominio sea dueño de su identidad.
   *
   * @param type        tipo de movimiento (ingreso o egreso, no puede ser nulo)
   * @param amount      monto del movimiento (debe ser mayor que cero)
   * @param categoryId  identificador de la categoría asociada (puede ser nulo si
   *                    el movimiento no se clasifica, aunque normalmente se espera
   *                    una categoría válida)
   * @param description descripción opcional (puede ser {@code null})
   * @param date        fecha en que ocurrió el movimiento (no puede ser nula)
   * @return instancia de Movement lista para persistir
   * @throws IllegalArgumentException si {@code amount} es nulo o ≤ 0,
   *                                  o si {@code date} es nulo
   */
  public static Movement create(MovementType type, BigDecimal amount,
      UUID categoryId, String description, LocalDateTime date) {

    UUID id = Generators.timeBasedEpochGenerator().generate(); // UUID v7
    LocalDateTime now = LocalDateTime.now();

    return new Movement(id, type, amount, categoryId, description, date, now);
  }

  /**
   * Fábrica para reconstituir un movimiento existente desde persistencia.
   *
   * @param id          UUID tal como está en la base de datos
   * @param type        tipo de movimiento
   * @param amount      monto
   * @param categoryId  UUID de la categoría asociada
   * @param description descripción (puede ser {@code null})
   * @param date        fecha del movimiento
   * @param createdAt   fecha de creación original
   * @return instancia de Movement reconstituida
   */
  public static Movement reconstitute(UUID id, MovementType type,
      BigDecimal amount, UUID categoryId, String description,
      LocalDateTime date, LocalDateTime createdAt) {

    return new Movement(id, type, amount, categoryId, description, date, createdAt);
  }

  /**
   * Actualiza los datos del movimiento.
   *
   * <p>
   * Permite modificar todos los campos editables del movimiento.
   * La validación del monto y la fecha se aplica antes de la actualización.
   *
   * @param type        nuevo tipo (no puede ser nulo)
   * @param amount      nuevo monto (debe ser mayor que cero)
   * @param categoryId  nuevo identificador de categoría (puede ser nulo)
   * @param description nueva descripción (puede ser {@code null})
   * @param date        nueva fecha (no puede ser nula)
   * @throws IllegalArgumentException si {@code amount} es nulo o ≤ 0,
   *                                  o si {@code date} es nulo
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