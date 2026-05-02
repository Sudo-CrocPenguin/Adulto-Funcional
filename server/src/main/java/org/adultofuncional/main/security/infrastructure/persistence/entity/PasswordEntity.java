package org.adultofuncional.main.security.infrastructure.persistence.entity;

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
 * Entidad JPA que mapea la tabla {@code passwords} de MariaDB.
 *
 * <p>
 * Almacena las credenciales de servicios y aplicaciones del usuario.
 * La contraseña se guarda encriptada con AES-256 utilizando una clave derivada
 * de la Master Key de la cuenta. Cada registro tiene su propio {@code salt}
 * para derivar una clave AES única, un {@code IV} (vector de inicialización)
 * de 16 bytes, y el texto cifrado ({@code ciphertext}) que contiene el par
 * usuario/contraseña (o cualquier otro secreto).
 *
 * <p>
 * El acceso al gestor de contraseñas requiere verificar la Master Key
 * ({@code account_master_key} en {@link AccountEntity}).
 *
 * <p>
 * Schema de la tabla {@code passwords}:
 * 
 * <pre>
 * password_id               CHAR(36)      NOT NULL PRIMARY KEY
 * password_application_name VARCHAR(35)   NOT NULL
 * password_salt             VARCHAR(255)  NOT NULL   -- salt para derivar clave AES (Base64)
 * password_iv               BINARY(16)    NOT NULL   -- 16 bytes de IV
 * password_ciphertext       VARBINARY(2048) NOT NULL -- ciphertext + tag (AES-GCM)
 * password_last_change_date DATE          NULL
 * passwords_fk_account_id   CHAR(36)      NOT NULL   -- FK a accounts(account_id)
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
   * Columna: {@code password_id CHAR(36) NOT NULL PRIMARY KEY}.
   * El UUID v7 es generado por la aplicación, no por la base de datos.
   */
  @Id
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
   * Salt único utilizado para derivar la clave AES a partir de la Master Key.
   *
   * <p>
   * Columna: {@code password_salt VARCHAR(255) NOT NULL}.
   * Se almacena generalmente en formato Base64.
   */
  @Column(name = "password_salt", length = 255, nullable = false)
  private String passwordSalt;

  /**
   * Vector de inicialización (IV) de 16 bytes utilizado en el cifrado AES.
   *
   * <p>
   * Columna: {@code password_iv BINARY(16) NOT NULL}.
   * Debe ser aleatorio para cada cifrado.
   */
  @Column(name = "password_iv", columnDefinition = "BINARY(16)", nullable = false)
  private byte[] passwordIv;

  /**
   * Texto cifrado (ciphertext) que contiene los datos protegidos.
   * Si se usa AES-GCM, aquí se incluye también el tag de autenticación.
   *
   * <p>
   * Columna: {@code password_ciphertext VARBINARY(2048) NOT NULL}.
   */
  @Column(name = "password_ciphertext", columnDefinition = "VARBINARY(2048)", nullable = false)
  private byte[] passwordCiphertext;

  /**
   * Fecha del último cambio de esta contraseña.
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
   * Relación obligatoria (no puede ser nula).
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "passwords_fk_account_id", nullable = false)
  private AccountEntity account;
}
