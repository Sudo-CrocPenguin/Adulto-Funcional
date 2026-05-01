package org.adultofuncional.main.security.infrastructure.persistence.entity;

import java.time.LocalDate;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que mapea la tabla {@code passwords} de MariaDB.
 *
 * <p>
 * Almacena las credenciales de servicios y aplicaciones del usuario.
 * La contraseña se guarda encriptada con AES-256 usando la Master Key de la
 * cuenta. El acceso al gestor de contraseñas requiere verificar la Master Key
 * ({@code account_master_key} en {@link AccountEntity}).
 *
 * <p>
 * Schema de la tabla {@code passwords}:
 * 
 * <pre>
 * password_id               CHAR(36)  PRIMARY KEY DEFAULT(UUID_V7())
 * password_application_name VARCHAR(35) NOT NULL
 * password_application      TEXT        NOT NULL     -- AES-256 en Base64
 * password_last_change_date DATE        NULL
 * passwords_fk_account_id   CHAR(36)    FK → accounts(account_id)
 * </pre>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see AccountEntity
 */
@Entity
@Table(name = "passwords")
@Getter
@Setter
@NoArgsConstructor
public class PasswordEntity {

  /**
   * Identificador único del registro de contraseña.
   *
   * <p>
   * Columna: {@code password_id CHAR(36) PRIMARY KEY DEFAULT(UUID_V7())}.
   */
  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "password_id", columnDefinition = "CHAR(36)")
  private UUID passwordId;

  /**
   * Nombre del servicio o aplicación.
   *
   * <p>
   * Columna: {@code password_application_name VARCHAR(35) NOT NULL}.
   * Ejemplos: "Gmail", "Netflix", "GitHub".
   */
  @Column(name = "password_application_name", length = 35, nullable = false)
  private String passwordApplicationName;

  /**
   * Contraseña encriptada con AES-256 (codificada en Base64).
   *
   * <p>
   * Columna: {@code password_application TEXT NOT NULL}.
   * Se desencripta únicamente cuando el usuario proporciona su Master Key.
   */
  @Column(name = "password_application", columnDefinition = "TEXT", nullable = false)
  private String passwordApplication;

  /**
   * Fecha del último cambio de la contraseña.
   *
   * <p>
   * Columna: {@code password_last_change_date DATE NULL}.
   * Útil para recordar rotaciones de contraseñas.
   */
  @Column(name = "password_last_change_date")
  private LocalDate passwordLastChangeDate;

  /**
   * Cuenta propietaria de esta contraseña.
   *
   * <p>
   * FK: {@code passwords_fk_account_id CHAR(36)} → {@code accounts(account_id)}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "passwords_fk_account_id", nullable = false)
  private AccountEntity account;
}
