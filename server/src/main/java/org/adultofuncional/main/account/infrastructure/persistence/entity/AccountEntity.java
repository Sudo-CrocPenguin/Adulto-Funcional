package org.adultofuncional.main.account.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.FixedExpensesEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.MovementEntity;
import org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code accounts} de MariaDB.
 *
 * <p>
 * Es la entidad central del sistema. Todas las demás entidades (movimientos,
 * categorías, gastos fijos, eventos y contraseñas) referencian una cuenta
 * mediante su {@code account_id}.
 *
 * <p>
 * Schema de la tabla {@code accounts}:
 * 
 * <pre>
 * account_id          CHAR(36)     PRIMARY KEY DEFAULT(UUID_V7())
 * account_names       VARCHAR(50)  NOT NULL
 * account_lastnames   VARCHAR(50)  NOT NULL
 * account_email       VARCHAR(255) NOT NULL UNIQUE
 * account_phone       VARCHAR(20)  NOT NULL
 * account_password    VARCHAR(255) NOT NULL
 * account_master_key  VARCHAR(255) NULL
 * account_created_at  TIMESTAMP    NOT NULL
 * </pre>
 *
 * <p>
 * Seguridad:
 * <ul>
 * <li>{@code account_password}: hash Argon2, nunca texto plano</li>
 * <li>{@code account_master_key}: hash Argon2 opcional, protege el acceso al
 * gestor de contraseñas</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see MovementEntity
 * @see FixedExpensesEntity
 * @see EventEntity
 * @see PasswordEntity
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {
  /**
   * Identificador único de la cuenta.
   *
   * <p>
   * Columna: {@code account_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())}.
   * Generado como UUID v7 (ordenable temporalmente) mediante
   * {@code UuidGenerator.Style.TIME}.
   */
  @Id
  @Column(name = "account_id", columnDefinition = "CHAR(36)")
  private UUID accountId;
  /**
   * Nombres del titular.
   *
   * <p>
   * Columna: {@code account_names VARCHAR(50) NOT NULL}.
   */
  @Column(name = "account_names", length = 50, nullable = false)
  private String accountNames;
  /**
   * Apellidos del titular.
   *
   * <p>
   * Columna: {@code account_lastnames VARCHAR(50) NOT NULL}.
   */
  @Column(name = "account_lastnames", length = 50, nullable = false)
  private String accountLastNames;
  /**
   * Correo electrónico. Único en todo el sistema, usado como username
   * para autenticación con Spring Security.
   *
   * <p>
   * Columna: {@code account_email VARCHAR(255) NOT NULL UNIQUE}.
   */
  @Column(name = "account_email", length = 255, nullable = false, unique = true)
  private String accountEmail;
  /**
   * Número de teléfono de contacto.
   *
   * <p>
   * Columna: {@code account_phone VARCHAR(20) NOT NULL}.
   */
  @Column(name = "account_phone", length = 20, nullable = false)
  private String accountPhone;
  /**
   * Hash de la contraseña de inicio de sesión (Argon2).
   *
   * <p>
   * Columna: {@code account_password VARCHAR(255) NOT NULL}.
   * Nunca debe contener texto plano.
   */
  @Column(name = "account_password", length = 255, nullable = false)
  private String accountPassword;
  /**
   * Hash de la clave maestra para acceder al gestor de contraseñas.
   * Es opcional y se verifica con Argon2 de forma independiente a la
   * contraseña de login.
   *
   * <p>
   * Columna: {@code account_master_key VARCHAR(255) NULL}.
   */
  @Column(name = "account_master_key", length = 255)
  private String accountMasterKey;
  /**
   * Fecha y hora de creación de la cuenta.
   *
   * <p>
   * Columna: {@code account_created_at TIMESTAMP NOT NULL}.
   * Se establece automáticamente en {@link #onCreate()} y no es modificable.
   */
  @Column(name = "account_created_at", updatable = false)
  private LocalDateTime accountCreatedAt;
  /**
   * Movimientos financieros asociados a esta cuenta.
   *
   * <p>
   * Relación {@code @OneToMany} con {@code movement_fk_account_id} como FK.
   * La eliminación de una cuenta elimina en cascada todos sus movimientos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MovementEntity> movements = new ArrayList<>();
  /**
   * Gastos fijos asociados a esta cuenta.
   *
   * <p>
   * Relación {@code @OneToMany} con {@code fixed_expense_fk_account_id} como FK.
   * La eliminación de una cuenta elimina en cascada todos sus gastos fijos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FixedExpensesEntity> fixedExpenses = new ArrayList<>();
  /**
   * Eventos de agenda asociados a esta cuenta.
   *
   * <p>
   * Relación {@code @OneToMany} con {@code event_fk_account_id} como FK.
   * La eliminación de una cuenta elimina en cascada todos sus eventos.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EventEntity> events = new ArrayList<>();
  /**
   * Contraseñas almacenadas asociadas a esta cuenta.
   *
   * <p>
   * Relación {@code @OneToMany} con {@code passwords_fk_account_id} como FK.
   * La eliminación de una cuenta elimina en cascada todas sus contraseñas.
   */
  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PasswordEntity> passwords = new ArrayList<>();

  /**
   * Callback JPA que establece {@code account_created_at} antes del primer
   * {@code INSERT}.
   */
  @PrePersist
  public void onCreate() {
    accountCreatedAt = LocalDateTime.now();
  }
}
