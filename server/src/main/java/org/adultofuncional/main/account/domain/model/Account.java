package org.adultofuncional.main.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Modelo de dominio que representa una cuenta de usuario en el sistema.
 *
 * <p>
 * Encapsula únicamente las invariantes de negocio relacionadas con la cuenta.
 * Las validaciones de formato (email, teléfono, longitud) pertenecen
 * a los DTOs de la capa de aplicación.
 *
 * <p>
 * Campos sensibles como {@code account_password} y {@code account_master_key}
 * permanecen en la capa de infraestructura ({@code AccountEntity}).
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "passwordHash")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

  @EqualsAndHashCode.Include
  final UUID id;

  String names;
  String lastnames;
  String email;
  String phone;
  String passwordHash;
  String masterKeyHash;

  final LocalDateTime createdAt;

  /**
   * Constructor privado. Usar los métodos de fábrica.
   */
  private Account(UUID id, String names, String lastnames, String email,
      String phone, LocalDateTime createdAt, String passwordHash, String masterKeyHash) {

    if (id != null) {
      validateId(id);
    }

    validateCreatedAt(createdAt);

    this.id = id;
    this.names = names;
    this.lastnames = lastnames;
    this.email = email;
    this.phone = phone;
    this.createdAt = createdAt;
    this.passwordHash = passwordHash;
    this.masterKeyHash = masterKeyHash;
  }

  /**
   * Fábrica para crear una cuenta nueva (antes de persistirla).
   *
   * <p>
   * Genera el UUID v7 y el {@code createdAt} en la aplicación,
   * garantizando que el dominio sea dueño de su identidad.
   *
   * @param names        nombre(s) del titular
   * @param lastnames    apellido(s) del titular
   * @param email        correo electrónico
   * @param phone        teléfono
   * @param passwordHash hash Argon2 de la contraseña (generado en el use case)
   * @return instancia de Account lista para persistir
   */
  public static Account create(String names, String lastnames,
      String email, String phone, String passwordHash) {
    UUID id = Generators.timeBasedEpochGenerator().generate(); // UUID v7
    LocalDateTime now = LocalDateTime.now();
    return new Account(id, names, lastnames, email, phone, now, passwordHash, null);
  }

  public static Account create(String names, String lastnames, String email,
      String phone, String passwordHash, String masterKeyHash) {
    UUID id = Generators.timeBasedEpochGenerator().generate();
    LocalDateTime now = LocalDateTime.now();
    return new Account(id, names, lastnames, email, phone, now, passwordHash, masterKeyHash);
  }

  /**
   * Fábrica para reconstituir una cuenta existente desde persistencia.
   *
   * @param id           UUID tal como está en la base de datos
   * @param names        nombre(s) del titular
   * @param lastnames    apellido(s) del titular
   * @param email        correo electrónico
   * @param phone        teléfono
   * @param createdAt    fecha de creación original
   * @param passwordHash hash Argon2 almacenado
   * @return instancia de Account reconstituida
   */
  public static Account reconstitute(UUID id, String names, String lastnames,
      String email, String phone, LocalDateTime createdAt, String passwordHash, String masterKeyHash) {
    return new Account(id, names, lastnames, email, phone, createdAt, passwordHash, masterKeyHash);
  }

  /**
   * Actualiza el nombre, apellidos y teléfono de la cuenta.
   *
   * @param names     nuevos nombres
   * @param lastnames nuevos apellidos
   * @param phone     nuevo teléfono
   */
  public void updateDetails(String names, String lastnames, String phone) {
    this.names = names;
    this.lastnames = lastnames;
    this.phone = phone;
  }

  /**
   * Actualiza el correo electrónico de la cuenta.
   *
   * <p>
   * La unicidad del email debe verificarse en el use case
   * antes de llamar a este método.
   *
   * @param email nuevo correo electrónico
   */
  public void updateEmail(String email) {
    this.email = email;
  }

  /**
   * Retorna el nombre completo concatenado (nombres + apellidos).
   *
   * @return nombre completo del titular
   */
  public String getFullName() {
    return names + " " + lastnames;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
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
