package org.adultofuncional.main.finances.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Modelo de dominio que representa un movimiento financiero (ingreso o egreso).
 *
 * <p>
 * Un movimiento registra un flujo de dinero en una cuenta, con un monto,
 * una fecha de ocurrencia y una categoría opcional. La fecha de registro
 * en el sistema se almacena en {@code createdAt} y se genera automáticamente
 * en el método de fábrica {@link #create}.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Validar que el tipo de movimiento, el monto, la cuenta y la fecha sean
 * valores no nulos y coherentes.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create} para que
 * el dominio sea dueño de su identidad.</li>
 * <li>Permitir la actualización de todos sus campos editables mediante
 * {@link #update}, con revalidación de invariantes.</li>
 * <li>Proveer el método {@link #reconstitute} para reconstruir instancias
 * desde persistencia sin regenerar UUID ni marcas de tiempo.</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato y seguridad (longitud de la descripción,
 * contenido HTML) se aplican en los DTOs de la capa de aplicación, por lo
 * que la entidad solo garantiza que los valores obligatorios estén presentes
 * y que el monto sea positivo.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see MovementType
 * @see org.adultofuncional.main.finances.application.dto.movement.MovementResponse
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movement {

  /**
   * Identificador único del movimiento (UUID v7).
   * Generado en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /**
   * Tipo de movimiento: {@link MovementType#INCOME} o
   * {@link MovementType#EXPENSE}.
   */
  MovementType type;

  /** Monto monetario del movimiento. Debe ser mayor que cero. */
  BigDecimal amount;

  /**
   * Identificador de la categoría asociada. Puede ser nulo si no se clasifica.
   */
  UUID categoryId;

  /** Identificador de la cuenta propietaria. No puede ser nulo. */
  UUID accountId;

  /** Descripción opcional del movimiento. */
  String description;

  /** Fecha calendario en que ocurrió el movimiento. */
  LocalDate date;

  /**
   * Fecha y hora exacta en que el movimiento fue registrado en el sistema.
   * Se genera automáticamente en {@link #create} y es inmutable.
   */
  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private Movement(UUID id, MovementType type, BigDecimal amount,
      UUID categoryId, UUID accountId, String description, LocalDate date,
      LocalDateTime createdAt) {

    validateId(id);
    validateType(type);
    validateCategoryId(categoryId);
    validateAccountId(accountId);
    validateAmount(amount);
    validateDate(date);
    validateCreatedAt(createdAt);

    this.id = id;
    this.type = type;
    this.amount = amount;
    this.categoryId = categoryId;
    this.accountId = accountId;
    this.description = description;
    this.date = date;
    this.createdAt = createdAt;
  }

  /**
   * Método de fábrica para crear un nuevo movimiento antes de persistirlo.
   *
   * <p>
   * Genera un UUID v7 y establece {@code createdAt} con la fecha y hora
   * actuales. La cuenta y la categoría deben haber sido validadas en la capa
   * de aplicación.
   *
   * @param type        tipo de movimiento (no nulo).
   * @param amount      monto del movimiento (mayor a cero).
   * @param categoryId  identificador de la categoría asociada (puede ser
   *                    nulo).
   * @param accountId   identificador de la cuenta propietaria (requerido).
   * @param description descripción opcional.
   * @param date        fecha del movimiento (no nula).
   * @return instancia de {@code Movement} lista para persistir.
   * @throws IllegalArgumentException si algún parámetro obligatorio es nulo o
   *                                  el monto no es positivo.
   */
  public static Movement create(MovementType type, BigDecimal amount,
      UUID categoryId, UUID accountId, String description, LocalDate date) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Movement(id, type, amount, categoryId, accountId, description, date, now);
  }

  /**
   * Método de fábrica para reconstituir un movimiento desde la capa de
   * persistencia.
   *
   * @param id          identificador existente.
   * @param type        tipo de movimiento.
   * @param amount      monto.
   * @param categoryId  categoría asociada.
   * @param accountId   cuenta propietaria.
   * @param description descripción.
   * @param date        fecha del movimiento.
   * @param createdAt   fecha de registro original.
   * @return instancia reconstituida.
   */
  public static Movement reconstitute(UUID id, MovementType type,
      BigDecimal amount, UUID categoryId,
      UUID accountId, String description,
      LocalDate date, LocalDateTime createdAt) {

    return new Movement(id, type, amount, categoryId, accountId,
        description, date, createdAt);
  }

  /**
   * Actualiza los datos del movimiento.
   *
   * <p>
   * Permite modificar todos los campos editables. Se aplican las mismas
   * validaciones que en la creación. La fecha de registro
   * {@code createdAt} no se modifica.
   *
   * @param type        nuevo tipo (no nulo).
   * @param amount      nuevo monto (mayor a cero).
   * @param categoryId  nuevo identificador de categoría (puede ser nulo).
   * @param description nueva descripción (puede ser nula).
   * @param date        nueva fecha del movimiento (no nula).
   * @throws IllegalArgumentException si alguna validación falla.
   */
  public void update(MovementType type, BigDecimal amount,
      UUID categoryId, String description,
      LocalDate date) {

    validateType(type);
    validateAmount(amount);
    validateCategoryId(categoryId);
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

  private static void validateCategoryId(UUID categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("CategoryId cannot be null");
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

  private static void validateAccountId(UUID accountId) {
    if (accountId == null) {
      throw new IllegalArgumentException("AccountId cannot be null");
    }
  }

  private static void validateDate(LocalDate date) {
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

  private static void validateType(MovementType type) {
    if (type == null) {
      throw new IllegalArgumentException("Movement type cannot be null");
    }
  }
}
