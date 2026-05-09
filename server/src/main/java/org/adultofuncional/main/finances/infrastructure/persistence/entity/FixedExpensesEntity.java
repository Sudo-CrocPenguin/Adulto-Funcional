package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code fixed_expenses} de MariaDB.
 *
 * <p>
 * Representa un gasto recurrente (suscripciones, servicios, alquileres, etc.)
 * asociado a una cuenta y <b>obligatoriamente</b> a una categoría. Incluye los
 * campos necesarios para el cálculo automático de la próxima fecha de
 * vencimiento y la generación de recordatorios.
 *
 * <p>
 * Schema de la tabla {@code fixed_expenses}:
 * 
 * <pre>
 * fixed_expense_id              CHAR(36)      NOT NULL PRIMARY KEY
 * fixed_expense_name            VARCHAR(50)   NOT NULL
 * fixed_expense_frequency       VARCHAR(15)   NOT NULL      -- "Mensual", "Semanal", etc.
 * fixed_expense_amount          DECIMAL(10,2) NOT NULL
 * fixed_expense_status          VARCHAR(15)   NOT NULL      -- "Activo" o "Inactivo"
 * fixed_expense_start_date      DATE          NOT NULL      -- inicio de la recurrencia
 * fixed_expense_next_due_date   DATE          NOT NULL      -- próximo vencimiento
 * fixed_expense_reminder_days   INT           NOT NULL      -- días de antelación para aviso
 * fixed_expense_fk_category_id  CHAR(36)      NOT NULL      -- FK obligatoria a categories
 * fixed_expense_fk_account_id   CHAR(36)      NOT NULL      -- FK a accounts
 * </pre>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see AccountEntity
 * @see CategoryEntity
 */
@Entity
@Table(name = "fixed_expenses")
@Getter
@Setter
@NoArgsConstructor
public class FixedExpensesEntity {

  /**
   * Identificador único del gasto fijo.
   *
   * <p>
   * Columna: {@code fixed_expense_id CHAR(36) PRIMARY KEY}.
   * Generado por la aplicación como UUID v7.
   */
  @Id
  @Column(name = "fixed_expense_id", columnDefinition = "CHAR(36)")
  private UUID fixedExpenseId;

  /**
   * Nombre descriptivo del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_name VARCHAR(50) NOT NULL}.
   * Ejemplos: "Netflix", "Alquiler oficina", "Seguro del coche".
   */
  @Column(name = "fixed_expense_name", length = 50, nullable = false)
  private String fixedExpenseName;

  /**
   * Frecuencia de recurrencia del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_frequency VARCHAR(15) NOT NULL}.
   * Almacena el valor en español tal como lo usa la base de datos
   * (ej. {@code "Mensual"}, {@code "Semanal"}).
   */
  @Column(name = "fixed_expense_frequency", length = 15, nullable = false)
  private String fixedExpenseFrequency;

  /**
   * Monto del gasto fijo.
   *
   * <p>
   * Columna: {@code fixed_expense_amount DECIMAL(10,2) NOT NULL}.
   * Se utiliza {@link BigDecimal} para evitar errores de redondeo.
   */
  @Column(name = "fixed_expense_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal fixedExpenseAmount;

  /**
   * Estado actual del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_status VARCHAR(15) NOT NULL}.
   * Valores admitidos: {@code "Activo"} o {@code "Inactivo"}.
   */
  @Column(name = "fixed_expense_status", length = 15, nullable = false)
  private String fixedExpenseStatus;

  /**
   * Fecha desde la cual empieza a contar la recurrencia.
   *
   * <p>
   * Columna: {@code fixed_expense_start_date DATE NOT NULL}.
   * No cambia una vez establecida; sirve de base para calcular los siguientes
   * vencimientos.
   */
  @Column(name = "fixed_expense_start_date", nullable = false)
  private LocalDate fixedExpenseStartDate;

  /**
   * Próxima fecha en que vence el gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_next_due_date DATE NOT NULL}.
   * Se actualiza automáticamente cada vez que el usuario registra el pago,
   * sumando la frecuencia correspondiente a la fecha actual o a la anterior
   * fecha de vencimiento.
   */
  @Column(name = "fixed_expense_next_due_date", nullable = false)
  private LocalDate fixedExpenseNextDueDate;

  /**
   * Días de antelación para generar un recordatorio.
   *
   * <p>
   * Columna: {@code fixed_expense_reminder_days INT NOT NULL}.
   * Indica cuántos días antes de {@code fixed_expense_next_due_date} se debe
   * avisar al usuario. Un valor de {@code 0} significa que el aviso se genera
   * el mismo día del vencimiento (o no se genera aviso previo, según la lógica
   * de negocio).
   */
  @Column(name = "fixed_expense_reminder_days", nullable = false)
  private Integer fixedExpenseReminderDays;

  /**
   * Categoría asociada al gasto fijo (obligatoria).
   *
   * <p>
   * FK: {@code fixed_expense_fk_category_id CHAR(36)} →
   * {@code categories(category_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_category_id", nullable = false)
  private CategoryEntity category;

  /**
   * Cuenta propietaria del gasto fijo.
   *
   * <p>
   * FK: {@code fixed_expense_fk_account_id CHAR(36)} →
   * {@code accounts(account_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_account_id", nullable = false)
  private AccountEntity account;
}
