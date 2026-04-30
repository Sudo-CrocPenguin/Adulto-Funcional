package org.adultofuncional.main.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

/**
 * Modelo de dominio que representa una cuenta de usuario en el sistema.
 *
 * <p>
 * Esta clase pertenece a la capa de dominio en Clean Architecture y encapsula
 * la lógica de negocio relacionada con la cuenta. No contiene dependencias de
 * infraestructura (JPA, base de datos) ni datos sensibles como contraseñas.
 *
 * <p>
 * La base de datos (tabla {@code accounts}) almacena campos adicionales como
 * {@code account_password} y {@code account_master_key}, pero estos permanecen
 * en la capa de infraestructura ({@code AccountEntity}) por separación de
 * responsabilidades.
 *
 * @author Jeronimo Ospina
 * @since 0.0.1
 */
@Getter
public class Account {

  private final UUID id;
  private String name;
  private final String email;
  private String phone;
  private final LocalDateTime createdAt;
  private boolean active;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Account))
      return false;
    Account account = (Account) o;
    return id != null && id.equals(account.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  /**
   * Constructor privado. Las instancias se crean mediante los factory methods
   * {@link #create(String, String, String)} o
   * {@link #reconstitute(UUID, String, String, String, boolean, LocalDateTime)}.
   *
   * @param id        UUID de la cuenta
   * @param name      nombre completo del titular
   * @param email     correo electrónico
   * @param phone     número de teléfono
   * @param active    estado de la cuenta
   * @param createdAt fecha y hora de creación
   * @throws IllegalArgumentException si name o email son inválidos,
   *                                  o si createdAt es una fecha futura
   */
  private Account(UUID id, String name, String email, String phone, boolean active, LocalDateTime createdAt) {
    validateName(name);
    validateEmail(email);
    validateCreatedAt(createdAt);

    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.active = active;
    this.createdAt = createdAt;
  }

  /**
   * Fábrica para crear una cuenta nueva. Genera un UUID aleatorio,
   * establece la fecha de creación actual y la marca como activa.
   *
   * @param name  nombre completo (no vacío)
   * @param email correo electrónico (debe contener '@')
   * @param phone teléfono de contacto
   * @return nueva instancia de Account
   */
  public static Account create(String name, String email, String phone) {
    return new Account(
        UUID.randomUUID(),
        name,
        email,
        phone,
        true,
        LocalDateTime.now());
  }

  /**
   * Fábrica para reconstituir una cuenta existente desde persistencia.
   *
   * @param id        UUID tal como está en la base de datos
   * @param name      nombre completo
   * @param email     correo electrónico
   * @param phone     teléfono
   * @param active    estado de actividad
   * @param createdAt fecha de creación original
   * @return instancia de Account reconstituida
   */
  public static Account reconstitute(UUID id, String name, String email, String phone, boolean active,
      LocalDateTime createdAt) {
    return new Account(id, name, email, phone, active, createdAt);
  }

  /**
   * Actualiza el nombre y teléfono de la cuenta.
   *
   * @param name  nuevo nombre completo (no vacío)
   * @param phone nuevo teléfono
   */
  // TODO: Agregar validación para el campo phone (no null, formato válido, etc.)
  public void updateDetails(String name, String phone) {
    validateName(name);
    this.name = name;
    this.phone = phone;
  }

  /** Desactiva la cuenta. */
  public void deactivate() {
    this.active = false;
  }

  /** Activa la cuenta. */
  public void activate() {
    this.active = true;
  }

  /**
   * Indica si la cuenta está activa.
   *
   * @return true si la cuenta está activa
   */
  public boolean isActive() {
    return active;
  }

  private static void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
  }

  // TODO: Mejorar validación de email usando @Email de Jakarta Validation o una regex más robusta
  private static void validateEmail(String email) {
    if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
      throw new IllegalArgumentException("Invalid email");
    }
  }

  /**
   * Valida que la fecha de creación no sea futura.
   *
   * @param createdAt fecha a validar
   * @throws IllegalArgumentException si createdAt es posterior al momento actual
   */
  private static void validateCreatedAt(LocalDateTime createdAt) {
    if (createdAt != null && createdAt.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("CreatedAt cannot be in the future");
    }
  }
}
