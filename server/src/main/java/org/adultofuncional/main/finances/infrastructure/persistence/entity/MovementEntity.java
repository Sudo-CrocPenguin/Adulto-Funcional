package org.adultofuncional.main.finances.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code movements} de MariaDB.
 *
 * <p>
 * Representa una transacción financiera (ingreso o egreso) asociada a una
 * cuenta y opcionalmente a una categoría.
 *
 * <p>
 * Schema de la tabla {@code movements}:
 * 
 * <pre>
 * movement_id             CHAR(36)      PRIMARY KEY DEFAULT(UUID_V7())
 * movement_type           VARCHAR(7)    NOT NULL          -- "Ingreso" o "Egreso"
 * movement_amount         DECIMAL(10,2) NOT NULL
 * movement_register_date  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
 * movement_description    TEXT          NULL
 * movement_date           DATE          NOT NULL
 * movement_fk_account_id  CHAR(36)      FK → accounts(account_id)
 * movement_fk_category_id CHAR(36)      FK → categories(category_id)
 * </pre>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see AccountEntity
 * @see CategoryEntity
 */
@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
public class MovementEntity {

  /**
   * Identificador único del movimiento.
   *
   * <p>
   * Columna: {@code movement_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())}.
   */
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "movement_id", columnDefinition = "CHAR(36)")
  private UUID movement_id;

  /**
   * Tipo de movimiento.
   *
   * <p>
   * Columna: {@code movement_type VARCHAR(7) NOT NULL}.
   * Valores: {@code "Ingreso"} o {@code "Egreso"}.
   */
  @Column(name = "movement_type", length = 7, nullable = false)
  private String movement_type;

  /**
   * Monto del movimiento.
   *
   * <p>
   * Columna: {@code movement_amount DECIMAL(10,2) NOT NULL}.
   * Se usa {@link BigDecimal} para precisión exacta.
   */
  @Column(name = "movement_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal movement_amount;

  /**
   * Fecha y hora en que se registró el movimiento en el sistema.
   *
   * <p>
   * Columna:
   * {@code movement_register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP}.
   * Se establece automáticamente en {@link #onCreate()} y no es modificable.
   */
  @Column(name = "movement_register_date", nullable = false, updatable = false)
  private LocalDateTime movement_register_date;

  /**
   * Descripción opcional del movimiento.
   *
   * <p>
   * Columna: {@code movement_description TEXT NULL}.
   */
  @Column(name = "movement_description", columnDefinition = "TEXT")
  private String movement_description;

  /**
   * Fecha calendario en que ocurrió la transacción.
   *
   * <p>
   * Columna: {@code movement_date DATE NOT NULL}.
   * Puede ser pasada, presente o futura.
   */
  @Column(name = "movement_date", nullable = false)
  private LocalDate movement_date;

  /**
   * Cuenta propietaria del movimiento.
   *
   * <p>
   * FK: {@code movement_fk_account_id CHAR(36)} → {@code accounts(account_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_account_id", nullable = false)
  private AccountEntity account;

  /**
   * Categoría asociada al movimiento (opcional).
   *
   * <p>
   * FK: {@code movement_fk_category_id CHAR(36)} →
   * {@code categories(category_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "movement_fk_category_id")
  private CategoryEntity category;

  /**
   * Callback JPA que establece {@code movement_register_date} antes del primer
   * {@code INSERT}.
   */
  @PrePersist
  protected void onCreate() {
    movement_register_date = LocalDateTime.now();
  }
}
