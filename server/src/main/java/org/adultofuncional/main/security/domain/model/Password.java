package org.adultofuncional.main.security.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa una credencial almacenada de forma segura.
 *
 * <p>
 * Cada instancia corresponde a las credenciales de una cuenta en un servicio
 * externo (por ejemplo, Netflix, GitHub, Gmail) asociadas a una cuenta de
 * usuario del sistema. La contraseña se almacena cifrada (AES‑256), nunca en
 * texto plano.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Validar que la plataforma, el usuario y la contraseña cifrada no estén
 * vacíos.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create} y mantener
 * la fecha de creación inmutable.</li>
 * <li>Permitir la actualización de los datos mediante {@link #update},
 * registrando automáticamente la fecha de modificación.</li>
 * <li>Proveer {@link #reconstitute} para reconstruir instancias desde
 * persistencia.</li>
 * </ul>
 *
 * <p>
 * Las validaciones de formato y seguridad (longitud de los textos, contenido
 * HTML) pertenecen a los DTOs de la capa de aplicación. El campo
 * {@code encryptedPassword} contiene el texto cifrado y nunca se expone sin
 * control.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "encryptedPassword")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Password {

  /**
   * Identificador único de la credencial (UUID v7).
   * Generado en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /**
   * Plataforma o servicio al que pertenece la credencial. No puede ser nulo ni
   * vacío.
   */
  String platform;

  /** Nombre de usuario o correo electrónico en el servicio externo. */
  String username;

  /**
   * Contraseña cifrada con AES‑256. Nunca se expone ni se almacena en texto
   * plano.
   */
  String encryptedPassword;

  /** Notas o información adicional (opcional). */
  String notes;

  /**
   * Identificador de la cuenta propietaria dentro del sistema (FK a
   * {@code accounts}).
   */
  UUID accountId;

  /**
   * Fecha y hora de creación de la credencial.
   * Se asigna automáticamente en {@link #create} y es inmutable.
   */
  final LocalDateTime createdAt;

  /**
   * Fecha y hora de la última modificación.
   * Se actualiza automáticamente al invocar {@link #update}.
   */
  LocalDateTime updatedAt;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private Password(UUID id, String platform, String username,
      String encryptedPassword, String notes,
      UUID accountId, LocalDateTime createdAt, LocalDateTime updatedAt) {

    validateId(id);
    validatePlatform(platform);
    validateUsername(username);
    validateEncryptedPassword(encryptedPassword);
    validateAccountId(accountId);
    validateCreatedAt(createdAt);

    this.id = id;
    this.platform = platform;
    this.username = username;
    this.encryptedPassword = encryptedPassword;
    this.notes = notes;
    this.accountId = accountId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Método de fábrica para crear una nueva credencial antes de persistirla.
   *
   * <p>
   * Genera un UUID v7 y establece {@code createdAt} con la fecha y hora
   * actuales. La cuenta y la contraseña cifrada deben haber sido validadas
   * en la capa de aplicación.
   *
   * @param platform          plataforma o servicio (no nulo ni vacío).
   * @param username          usuario o correo asociado (no nulo ni vacío).
   * @param encryptedPassword contraseña cifrada (no nula ni vacía).
   * @param notes             notas adicionales (puede ser {@code null}).
   * @param accountId         identificador de la cuenta propietaria.
   * @return instancia de {@code Password} lista para persistir.
   * @throws IllegalArgumentException si algún parámetro obligatorio es nulo o
   *                                  vacío.
   */
  public static Password create(String platform, String username,
      String encryptedPassword, String notes, UUID accountId) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Password(id, platform, username, encryptedPassword,
        notes, accountId, now, null);
  }

  /**
   * Método de fábrica para reconstituir una credencial desde persistencia.
   *
   * @param id                identificador existente.
   * @param platform          plataforma.
   * @param username          usuario.
   * @param encryptedPassword contraseña cifrada.
   * @param notes             notas.
   * @param accountId         cuenta propietaria.
   * @param createdAt         fecha de creación original.
   * @param updatedAt         fecha de última modificación.
   * @return instancia reconstituida.
   */
  public static Password reconstitute(UUID id, String platform,
      String username, String encryptedPassword,
      String notes, UUID accountId,
      LocalDateTime createdAt, LocalDateTime updatedAt) {

    return new Password(id, platform, username, encryptedPassword,
        notes, accountId, createdAt, updatedAt);
  }

  /**
   * Actualiza los datos de la credencial.
   *
   * <p>
   * Modifica la plataforma, el usuario, la contraseña cifrada y las notas.
   * La fecha {@code updatedAt} se establece automáticamente al momento actual.
   *
   * @param platform          nueva plataforma (no nula ni vacía).
   * @param username          nuevo usuario (no nulo ni vacío).
   * @param encryptedPassword nueva contraseña cifrada (no nula ni vacía).
   * @param notes             nuevas notas (puede ser {@code null}).
   * @throws IllegalArgumentException si algún campo obligatorio es nulo o
   *                                  vacío.
   */
  public void update(String platform, String username,
      String encryptedPassword, String notes) {

    validatePlatform(platform);
    validateUsername(username);
    validateEncryptedPassword(encryptedPassword);

    this.platform = platform;
    this.username = username;
    this.encryptedPassword = encryptedPassword;
    this.notes = notes;
    this.updatedAt = LocalDateTime.now();
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validatePlatform(String platform) {
    if (platform == null || platform.isBlank()) {
      throw new IllegalArgumentException("Platform cannot be null or empty");
    }
  }

  private static void validateUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
  }

  private static void validateEncryptedPassword(String encryptedPassword) {
    if (encryptedPassword == null || encryptedPassword.isBlank()) {
      throw new IllegalArgumentException("Encrypted password cannot be null or empty");
    }
  }

  private static void validateAccountId(UUID accountId) {
    if (accountId == null) {
      throw new IllegalArgumentException("AccountId cannot be null");
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
