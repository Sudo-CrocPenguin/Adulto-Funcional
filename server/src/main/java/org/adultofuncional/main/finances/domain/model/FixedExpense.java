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
 * determinada (mensual, semanal, quincenal, etc.). Por ejemplo: alquiler,
 * suscripciones, servicios públicos.
 *
 * <p>
 * Encapsula únicamente las invariantes de negocio relacionadas con el gasto fijo:
 * <ul>
 *   <li>El nombre no puede ser nulo ni vacío</li>
 *   <li>El monto debe ser mayor que cero</li>
 *   <li>La frecuencia no puede ser nula</li>
 *   <li>El estado no puede ser nulo (se inicializa siempre como ACTIVE)</li>
 *   <li>La fecha de inicio no puede ser nula</li>
 *   <li>La fecha de fin, si existe, no puede ser anterior a la fecha de inicio</li>
 *   <li>La fecha de creación no puede ser nula ni futura</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato (longitud del nombre, caracteres especiales)
 * pertenecen a los DTOs de la capa de aplicación.
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
   * Constructor privado. Usar los métodos de fábrica.
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
   * Fábrica para crear un nuevo gasto fijo (antes de persistirlo).
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en la aplicación,
   * garantizando que el dominio sea dueño de su identidad.
   * El estado se establece automáticamente como {@code ACTIVE}.
   *
   * @param name        nombre del gasto fijo (no puede ser nulo ni vacío)
   * @param amount      monto del gasto (debe ser mayor que cero)
   * @param categoryId  identificador de la categoría asociada (puede ser nulo
   *                    si no se clasifica, aunque normalmente se espera una categoría)
   * @param frequency   frecuencia de recurrencia (no puede ser nula)
   * @param startDate   fecha de inicio del gasto fijo (no puede ser nula)
   * @param endDate     fecha de finalización (opcional, puede ser {@code null}).
   *                    Si se proporciona, no puede ser anterior a {@code startDate}
   * @return instancia de FixedExpense lista para persistir
   * @throws IllegalArgumentException si {@code name} es nulo o vacío,
   *                                  si {@code amount} es nulo o ≤ 0,
   *                                  si {@code frequency} es nulo,
   *                                  si {@code startDate} es nulo,
   *                                  o si {@code endDate} es anterior a {@code startDate}
   */
  public static FixedExpense create(String name, BigDecimal amount,
      UUID categoryId, Frequency frequency,
      LocalDateTime startDate, LocalDateTime endDate) {

    UUID id = Generators.timeBasedEpochGenerator().generate(); // UUID v7
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
   * Fábrica para reconstituir un gasto fijo existente desde persistencia.
   *
   * @param id          UUID tal como está en la base de datos
   * @param name        nombre del gasto fijo
   * @param amount      monto
   * @param categoryId  UUID de la categoría asociada
   * @param frequency   frecuencia
   * @param status      estado actual (ACTIVE o INACTIVE)
   * @param startDate   fecha de inicio
   * @param endDate     fecha de finalización (puede ser {@code null})
   * @param createdAt   fecha de creación original
   * @return instancia de FixedExpense reconstituida
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
   *
   * <p>
   * Permite modificar todos los campos editables excepto el estado
   * (que se controla con {@link #activate()} y {@link #deactivate()}).
   * Se aplican las mismas validaciones que en la creación.
   *
   * @param name        nuevo nombre (no puede ser nulo ni vacío)
   * @param amount      nuevo monto (debe ser mayor que cero)
   * @param categoryId  nuevo identificador de categoría (puede ser nulo)
   * @param frequency   nueva frecuencia (no puede ser nula)
   * @param startDate   nueva fecha de inicio (no puede ser nula)
   * @param endDate     nueva fecha de finalización (puede ser {@code null},
   *                    pero si se especifica no puede ser anterior a {@code startDate})
   * @throws IllegalArgumentException si alguna validación falla
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
   *
   * <p>
   * Cambia el estado a {@code ACTIVE} para que sea tenido en cuenta
   * en los cálculos de gastos recurrentes.
   */
  public void activate() {
    this.status = Status.ACTIVE;
  }

  /**
   * Desactiva el gasto fijo.
   *
   * <p>
   * Cambia el estado a {@code INACTIVE} para que deje de considerarse
   * en los cálculos sin eliminar el registro histórico.
   */
  public void deactivate() {
    this.status = Status.INACTIVE;
  }

  /**
   * Indica si el gasto fijo está activo.
   *
   * @return {@code true} si el estado es {@code ACTIVE}, {@code false} en caso contrario
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