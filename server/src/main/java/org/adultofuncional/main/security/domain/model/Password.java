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
 * Modelo de dominio que representa una contraseña almacenada
 * de forma segura dentro del sistema.
 *
 * <p>
 * Una contraseña pertenece a un servicio o plataforma específica
 * (por ejemplo: Netflix, GitHub, Gmail, etc.).
 *
 * <p>
 * Este modelo encapsula únicamente las invariantes de negocio.
 * Las validaciones de formato pertenecen a los DTOs.
 *
 * <p>
 * El valor de la contraseña debe almacenarse cifrado antes
 * de persistirse.
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "encryptedPassword")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Password {

  @EqualsAndHashCode.Include
  final UUID id;

  String platform;
  String username;
  String encryptedPassword;
  String notes;

  final LocalDateTime createdAt;

  LocalDateTime updatedAt;

  /**
   * Constructor privado. Usar métodos de fábrica.
   */
  private Password(UUID id, String platform, String username,
      String encryptedPassword, String notes,
      LocalDateTime createdAt, LocalDateTime updatedAt) {

    if (id != null) {
      validateId(id);
    }

    validatePlatform(platform);
    validateUsername(username);
    validateEncryptedPassword(encryptedPassword);
    validateCreatedAt(createdAt);

    this.id = id;
    this.platform = platform;
    this.username = username;
    this.encryptedPassword = encryptedPassword;
    this.notes = notes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Fábrica para crear una nueva contraseña.
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en el dominio.
   *
   * @param platform          plataforma o servicio
   * @param username          usuario o correo asociado
   * @param encryptedPassword contraseña cifrada
   * @param notes             notas opcionales
   * @return instancia lista para persistir
   */
  public static Password create(String platform, String username,
      String encryptedPassword, String notes) {

    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();

    return new Password(
        id,
        platform,
        username,
        encryptedPassword,
        notes,
        now,
        null);
  }

  /**
   * Fábrica para reconstituir una contraseña desde persistencia.
   *
   * @param id                identificador
   * @param platform          plataforma
   * @param username          usuario asociado
   * @param encryptedPassword contraseña cifrada
   * @param notes             notas
   * @param createdAt         fecha de creación
   * @param updatedAt         fecha de actualización
   * @return instancia reconstituida
   */
  public static Password reconstitute(UUID id, String platform,
      String username, String encryptedPassword,
      String notes, LocalDateTime createdAt,
      LocalDateTime updatedAt) {

    return new Password(id, platform, username,
        encryptedPassword, notes, createdAt, updatedAt);
  }

  /**
   * Actualiza la información de la contraseña almacenada.
   *
   * @param platform          nueva plataforma
   * @param username          nuevo usuario
   * @param encryptedPassword nueva contraseña cifrada
   * @param notes             nuevas notas
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

  private static void validateCreatedAt(LocalDateTime createdAt) {
    if (createdAt == null) {
      throw new IllegalArgumentException("CreatedAt cannot be null");
    }

    if (createdAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("CreatedAt cannot be in the future");
    }
  }
}