package org.adultofuncional.main.finances.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Modelo de dominio que representa un gasto fijo recurrente.
 *
 * <p>
 * Un gasto fijo es un egreso que se repite con una frecuencia determinada
 * (mensual, semanal, etc.). El modelo encapsula las invariantes de negocio
 * relacionadas con el ciclo de recurrencia, el estado y los importes.
 *
 * <p>
 * La columna {@code fixed_expense_next_due_date} se recalcula en la capa de
 * aplicación cada vez que se registra un pago, mientras que
 * {@code fixed_expense_reminder_days} indica cuántos días antes debe generarse
 * un aviso.
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

  LocalDate startDate;
  LocalDate nextDueDate;
  int reminderDays;

  /**
   * Constructor privado. Usar los métodos de fábrica.
   */
  private FixedExpense(UUID id, String name, BigDecimal amount,
      UUID categoryId, Frequency frequency, Status status,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    if (id != null) {
      validateId(id);
    }

    validateName(name);
    validateAmount(amount);
    validateFrequency(frequency);
    validateStatus(status);
    validateDates(startDate, nextDueDate);
    validateReminderDays(reminderDays);

    this.id = id;
    this.name = name;
    this.amount = amount;
    this.categoryId = categoryId;
    this.frequency = frequency;
    this.status = status;
    this.startDate = startDate;
    this.nextDueDate = nextDueDate;
    this.reminderDays = reminderDays;
  }

  /**
   * Fábrica para crear un nuevo gasto fijo (antes de persistirlo).
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en la aplicación,
   * garantizando que el dominio sea dueño de su identidad.
   * El estado se establece automáticamente como {@code ACTIVE}.
   *
   * @param name       nombre del gasto fijo (no puede ser nulo ni vacío)
   * @param amount     monto del gasto (debe ser mayor que cero)
   * @param categoryId identificador de la categoría asociada (puede ser nulo
   *                   si no se clasifica, aunque normalmente se espera una
   *                   categoría)
   * @param frequency  frecuencia de recurrencia (no puede ser nula)
   * @param startDate  fecha de inicio del gasto fijo (no puede ser nula)
   * @param endDate    fecha de finalización (opcional, puede ser {@code null}).
   *                   Si se proporciona, no puede ser anterior a
   *                   {@code startDate}
   * @return instancia de FixedExpense lista para persistir
   * @throws IllegalArgumentException si {@code name} es nulo o vacío,
   *                                  si {@code amount} es nulo o ≤ 0,
   *                                  si {@code frequency} es nulo,
   *                                  si {@code startDate} es nulo,
   *                                  o si {@code endDate} es anterior a
   *                                  {@code startDate}
   * 
   */
  public static FixedExpense create(String name, BigDecimal amount,
      UUID categoryId, Frequency frequency,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    UUID id = Generators.timeBasedEpochGenerator().generate();

    return new FixedExpense(
        id,
        name,
        amount,
        categoryId,
        frequency,
        Status.ACTIVE,
        startDate,
        nextDueDate,
        reminderDays);
  }

  /**
   * 
   * Fábrica para reconstituir un gasto fijo desde persistencia.
   * 
   */
  public static FixedExpense reconstitute(UUID id, String name,
      BigDecimal amount, UUID categoryId,
      Frequency frequency, Status status,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    return new FixedExpense(id, name, amount, categoryId, frequency,
        status, startDate, nextDueDate, reminderDays);
  }

  /**
   * Actualiza los datos principales del gasto fijo.
   *
   * <p>
   * Permite modificar todos los campos editables excepto el estado
   * (que se controla con {@link #activate()} y {@link #deactivate()}).
   * Se aplican las mismas validaciones que en la creación.
   *
   * @param name       nuevo nombre (no puede ser nulo ni vacío)
   * @param amount     nuevo monto (debe ser mayor que cero)
   * @param categoryId nuevo identificador de categoría (puede ser nulo)
   * @param frequency  nueva frecuencia (no puede ser nula)
   * @param startDate  nueva fecha de inicio (no puede ser nula)
   * @param endDate    nueva fecha de finalización (puede ser {@code null},
   *                   pero si se especifica no puede ser anterior a
   *                   {@code startDate})
   * @throws IllegalArgumentException si alguna validación falla
   */
  public void update(String name, BigDecimal amount,
      UUID categoryId, Frequency frequency,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    validateName(name);
    validateAmount(amount);
    validateFrequency(frequency);
    validateDates(startDate, nextDueDate);
    validateReminderDays(reminderDays);

    this.name = name;
    this.amount = amount;
    this.categoryId = categoryId;
    this.frequency = frequency;
    this.startDate = startDate;
    this.nextDueDate = nextDueDate;
    this.reminderDays = reminderDays;
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
   * @return {@code true} si el estado es {@code ACTIVE}, {@code false} en caso
   *         contrario
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

  private static void validateDates(LocalDate startDate, LocalDate nextDueDate) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }
    if (nextDueDate == null) {
      throw new IllegalArgumentException("Next due date cannot be null");
    }
    if (nextDueDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Next due date cannot be before start date");
    }
  }

  private static void validateReminderDays(int reminderDays) {
    if (reminderDays < 0) {
      throw new IllegalArgumentException("Reminder days cannot be negative");
    }
  }
}
