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
 * asociado a una cuenta y opcionalmente a una categoría.
 *
 * <p>
 * Schema de la tabla {@code fixed_expenses}:
 * 
 * <pre>
 * fixed_expense_id              CHAR(36)      NOT NULL PRIMARY KEY
 * fixed_expense_name            VARCHAR(20)   NOT NULL
 * fixed_expense_frequency       VARCHAR(15)   NOT NULL      -- "Mensual", "Anual", etc.
 * fixed_expense_amount          DECIMAL(10,2) NOT NULL
 * fixed_expense_status          VARCHAR(15)   NOT NULL      -- "Activo" o "Inactivo"
 * fixed_expense_closing_date    DATE          NOT NULL
 * fixed_expense_fk_category_id  CHAR(36)      FK → categories(category_id)
 * fixed_expense_fk_account_id   CHAR(36)      FK → accounts(account_id)
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
   * Columna: {@code fixed_expense_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())}.
   */
  @Id
  @Column(name = "fixed_expense_id", columnDefinition = "CHAR(36)")
  private UUID fixedExpenseId;

  /**
   * Nombre descriptivo del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_name VARCHAR(20) NOT NULL}.
   * Ejemplos: "Netflix", "Alquiler", "Gimnasio".
   */
  @Column(name = "fixed_expense_name", length = 20, nullable = false)
  private String fixedExpenseName;

  /**
   * Frecuencia de recurrencia del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_frequency VARCHAR(15) NOT NULL}.
   * Valores típicos: {@code "Mensual"}, {@code "Anual"}, {@code "Semanal"},
   * {@code "Quincenal"}, {@code "Trimestral"}.
   */
  @Column(name = "fixed_expense_frequency", length = 15, nullable = false)
  private String fixedExpenseFrequency;

  /**
   * Monto del gasto fijo.
   *
   * <p>
   * Columna: {@code fixed_expense_amount DECIMAL(10,2) NOT NULL}.
   * Se usa {@link BigDecimal} para precisión exacta.
   */
  @Column(name = "fixed_expense_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal fixedExpenseAmount;

  /**
   * Estado actual del gasto.
   *
   * <p>
   * Columna: {@code fixed_expense_status VARCHAR(15) NOT NULL}.
   * Valores: {@code "Activo"} o {@code "Inactivo"}.
   */
  @Column(name = "fixed_expense_status", length = 15, nullable = false)
  private String fixedExpenseStatus;

  /**
   * Fecha de cierre o finalización del gasto. Después de esta fecha el sistema
   * deja de generar movimientos automáticos.
   *
   * <p>
   * Columna: {@code fixed_expense_closing_date DATE NOT NULL}.
   */
  @Column(name = "fixed_expense_closing_date", nullable = false)
  private LocalDate fixedExpenseClosingDate;

  /**
   * Categoría asociada al gasto fijo (opcional).
   *
   * <p>
   * FK: {@code fixed_expense_fk_category_id CHAR(36)} →
   * {@code categories(category_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fixed_expense_fk_category_id")
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
