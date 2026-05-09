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
 * (mensual, semanal, etc.) y está asociado a una cuenta y a una categoría.
 * El modelo encapsula las invariantes de negocio relacionadas con el ciclo
 * de recurrencia, el estado operativo y los importes monetarios.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Validar que el nombre no esté vacío, el monto sea mayor a cero, las
 * fechas sean coherentes y los días de recordatorio no sean negativos.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create} para que
 * el dominio sea dueño de su identidad.</li>
 * <li>Gestionar el estado ({@link Status#ACTIVE} / {@link Status#INACTIVE})
 * mediante los métodos {@link #activate()} y {@link #deactivate()}.</li>
 * <li>Permitir la actualización parcial de sus campos a través de
 * {@link #update} y la reconstitución desde persistencia mediante
 * {@link #reconstitute}.</li>
 * </ul>
 *
 * <p>
 * El campo {@code nextDueDate} representa la próxima fecha de vencimiento
 * y es responsabilidad de la capa de aplicación recalcularlo cuando se
 * registra un pago. {@code reminderDays} indica cuántos días antes debe
 * generarse un aviso.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see Frequency
 * @see Status
 * @see org.adultofuncional.main.finances.application.dto.fixedexpense.FixedExpenseResponse
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FixedExpense {

  /**
   * Identificador único del gasto fijo (UUID v7).
   * Generado en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /** Nombre descriptivo del gasto fijo. No puede ser nulo ni vacío. */
  String name;

  /** Monto monetario del gasto. Debe ser mayor que cero. */
  BigDecimal amount;

  /** Identificador de la categoría asociada. No puede ser nulo. */
  UUID categoryId;

  /** Identificador de la cuenta propietaria. No puede ser nulo. */
  UUID accountId;

  /** Frecuencia de recurrencia del gasto. */
  Frequency frequency;

  /** Estado operativo actual ({@code ACTIVE} o {@code INACTIVE}). */
  Status status;

  /** Fecha de inicio del gasto fijo. */
  LocalDate startDate;

  /** Próxima fecha de vencimiento. */
  LocalDate nextDueDate;

  /** Días de antelación para el recordatorio. No puede ser negativo. */
  int reminderDays;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private FixedExpense(UUID id, String name, BigDecimal amount,
      UUID categoryId, UUID accountId, Frequency frequency, Status status,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    validateId(id);
    validateCategoryId(categoryId);
    validateAccountId(accountId);
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
    this.accountId = accountId;
    this.frequency = frequency;
    this.status = status;
    this.startDate = startDate;
    this.nextDueDate = nextDueDate;
    this.reminderDays = reminderDays;
  }

  /**
   * Método de fábrica para crear un nuevo gasto fijo antes de persistirlo.
   *
   * <p>
   * Genera un UUID v7 y establece el estado inicial como
   * {@link Status#ACTIVE}. La cuenta debe existir y la categoría también
   * (validado en la capa de aplicación antes de invocar este método).
   *
   * @param name         nombre del gasto fijo (no nulo ni vacío).
   * @param amount       monto del gasto (mayor a cero).
   * @param categoryId   identificador de la categoría asociada (requerido).
   * @param accountId    identificador de la cuenta propietaria (requerido).
   * @param frequency    frecuencia de recurrencia (no nula).
   * @param startDate    fecha de inicio (no nula).
   * @param nextDueDate  fecha de próximo vencimiento (no nula, no anterior a
   *                     {@code startDate}).
   * @param reminderDays días de recordatorio (no negativo).
   * @return instancia de {@code FixedExpense} lista para persistir.
   * @throws IllegalArgumentException si algún parámetro no cumple las
   *                                  invariantes.
   */
  public static FixedExpense create(String name, BigDecimal amount,
      UUID categoryId, UUID accountId, Frequency frequency,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    UUID id = Generators.timeBasedEpochGenerator().generate();

    return new FixedExpense(
        id,
        name,
        amount,
        categoryId,
        accountId,
        frequency,
        Status.ACTIVE,
        startDate,
        nextDueDate,
        reminderDays);
  }

  /**
   * Método de fábrica para reconstituir un gasto fijo desde la capa de
   * persistencia.
   *
   * @param id           identificador existente.
   * @param name         nombre del gasto fijo.
   * @param amount       monto.
   * @param categoryId   categoría asociada.
   * @param accountId    cuenta propietaria.
   * @param frequency    frecuencia.
   * @param status       estado actual.
   * @param startDate    fecha de inicio.
   * @param nextDueDate  fecha de próximo vencimiento.
   * @param reminderDays días de recordatorio.
   * @return instancia reconstituida.
   */
  public static FixedExpense reconstitute(UUID id, String name,
      BigDecimal amount, UUID categoryId, UUID accountId,
      Frequency frequency, Status status,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    return new FixedExpense(id, name, amount, categoryId, accountId, frequency,
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
   * @param name         nuevo nombre (no nulo ni vacío).
   * @param amount       nuevo monto (mayor a cero).
   * @param categoryId   nuevo identificador de categoría.
   * @param frequency    nueva frecuencia.
   * @param startDate    nueva fecha de inicio.
   * @param nextDueDate  nueva fecha de próximo vencimiento.
   * @param reminderDays nuevos días de recordatorio.
   * @throws IllegalArgumentException si alguna validación falla.
   */
  public void update(String name, BigDecimal amount, UUID categoryId, Frequency frequency,
      LocalDate startDate, LocalDate nextDueDate,
      int reminderDays) {

    validateName(name);
    validateAmount(amount);
    validateCategoryId(categoryId);
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
   * Activa el gasto fijo, cambiando su estado a {@link Status#ACTIVE}.
   */
  public void activate() {
    this.status = Status.ACTIVE;
  }

  /**
   * Desactiva el gasto fijo, cambiando su estado a {@link Status#INACTIVE}.
   */
  public void deactivate() {
    this.status = Status.INACTIVE;
  }

  /**
   * Indica si el gasto fijo está actualmente activo.
   *
   * @return {@code true} si el estado es {@link Status#ACTIVE}.
   */
  public boolean isActive() {
    return Status.ACTIVE.equals(this.status);
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID fixedExpenseId) {
    if (fixedExpenseId == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateCategoryId(UUID categoryId) {
    if (categoryId == null) {
      throw new IllegalArgumentException("categoryId cannot be null");
    }
  }

  private static void validateAccountId(UUID accountId) {
    if (accountId == null) {
      throw new IllegalArgumentException("accountId cannot be null");
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
