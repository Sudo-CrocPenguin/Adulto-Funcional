package org.adultofuncional.main.security.domain.model;

import java.time.LocalDate;
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
 * Cada instancia corresponde a las credenciales de un servicio externo
 * (plataforma o aplicación) asociadas a una cuenta del sistema. Los datos
 * sensibles se almacenan cifrados: {@code salt}, {@code iv} y
 * {@code ciphertext} contienen los parámetros necesarios para el descifrado
 * AES‑256. La contraseña en texto plano **nunca** se guarda en este modelo.
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 * <li>Validar que el nombre de la aplicación y los campos criptográficos
 * obligatorios no sean nulos ni vacíos.</li>
 * <li>Generar su propio identificador UUID v7 en {@link #create}.</li>
 * <li>Permitir la actualización de los datos mediante {@link #update},
 * manteniendo la integridad de los campos obligatorios.</li>
 * <li>Proveer {@link #reconstitute} para reconstruir instancias desde
 * persistencia.</li>
 * </ul>
 *
 * @author Jeronimo Ospina Zapata
 * @since 0.0.1
 * @see org.adultofuncional.main.security.infrastructure.persistence.entity.PasswordEntity
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "salt", "iv", "ciphertext" })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Password {

  /**
   * Identificador único de la credencial (UUID v7). Generado en {@link #create}.
   */
  @EqualsAndHashCode.Include
  final UUID id;

  /**
   * Nombre de la aplicación o servicio (ej. "Gmail", "Netflix"). No puede ser
   * nulo ni vacío.
   */
  String applicationName;

  /**
   * Salt único (Base64) usado para derivar la clave AES a partir de la Master
   * Key.
   */
  String salt;

  /** Vector de inicialización (16 bytes) para el cifrado AES. */
  byte[] iv;

  /**
   * Texto cifrado (ciphertext + tag si es AES‑GCM) que contiene el secreto
   * protegido.
   */
  byte[] ciphertext;

  /** Fecha del último cambio de la contraseña. Opcional. */
  LocalDate lastChangeDate;

  /** Identificador de la cuenta propietaria (FK a {@code accounts}). */
  UUID accountId;

  /**
   * Constructor privado. Usar {@link #create} o {@link #reconstitute}.
   */
  private Password(UUID id, String applicationName, String salt,
      byte[] iv, byte[] ciphertext, LocalDate lastChangeDate,
      UUID accountId) {

    validateId(id);
    validateApplicationName(applicationName);
    validateSalt(salt);
    validateIv(iv);
    validateCiphertext(ciphertext);
    validateAccountId(accountId);

    this.id = id;
    this.applicationName = applicationName;
    this.salt = salt;
    this.iv = iv;
    this.ciphertext = ciphertext;
    this.lastChangeDate = lastChangeDate;
    this.accountId = accountId;
  }

  /**
   * Método de fábrica para crear una nueva credencial antes de persistirla.
   *
   * <p>
   * Genera un UUID v7. Los valores de {@code salt}, {@code iv} y
   * {@code ciphertext} deben ser generados por el servicio de cifrado en la
   * capa de aplicación.
   *
   * @param applicationName nombre de la aplicación (no nulo ni vacío).
   * @param salt            salt en Base64 (no nulo ni vacío).
   * @param iv              vector de inicialización (no nulo, longitud > 0).
   * @param ciphertext      texto cifrado (no nulo, longitud > 0).
   * @param lastChangeDate  fecha de último cambio (puede ser {@code null}).
   * @param accountId       identificador de la cuenta propietaria.
   * @return instancia de {@code Password} lista para persistir.
   * @throws IllegalArgumentException si algún parámetro obligatorio es nulo o
   *                                  vacío.
   */
  public static Password create(String applicationName, String salt,
      byte[] iv, byte[] ciphertext, LocalDate lastChangeDate,
      UUID accountId) {

    UUID id = Generators.timeBasedEpochGenerator().generate();

    return new Password(id, applicationName, salt, iv, ciphertext,
        lastChangeDate, accountId);
  }

  /**
   * Método de fábrica para reconstituir una credencial desde persistencia.
   *
   * @param id              identificador existente.
   * @param applicationName nombre de la aplicación.
   * @param salt            salt.
   * @param iv              vector de inicialización.
   * @param ciphertext      texto cifrado.
   * @param lastChangeDate  fecha de último cambio.
   * @param accountId       cuenta propietaria.
   * @return instancia reconstituida.
   */
  public static Password reconstitute(UUID id, String applicationName,
      String salt, byte[] iv, byte[] ciphertext,
      LocalDate lastChangeDate, UUID accountId) {

    return new Password(id, applicationName, salt, iv, ciphertext,
        lastChangeDate, accountId);
  }

  /**
   * Actualiza los datos de la credencial (tras un cambio de contraseña).
   *
   * <p>
   * Los nuevos valores criptográficos deben ser generados por el servicio de
   * cifrado. La fecha {@code lastChangeDate} se actualiza normalmente a la
   * fecha actual, pero se recibe como parámetro para mantener la flexibilidad.
   *
   * @param applicationName nuevo nombre de aplicación.
   * @param salt            nuevo salt.
   * @param iv              nuevo IV.
   * @param ciphertext      nuevo ciphertext.
   * @param lastChangeDate  nueva fecha de cambio.
   * @throws IllegalArgumentException si algún campo obligatorio es nulo o vacío.
   */
  public void update(String applicationName, String salt,
      byte[] iv, byte[] ciphertext, LocalDate lastChangeDate) {

    validateApplicationName(applicationName);
    validateSalt(salt);
    validateIv(iv);
    validateCiphertext(ciphertext);

    this.applicationName = applicationName;
    this.salt = salt;
    this.iv = iv;
    this.ciphertext = ciphertext;
    this.lastChangeDate = lastChangeDate;
  }

  // ── Invariantes de negocio ────────────────────────────────────────────────

  private static void validateId(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("Id cannot be null");
    }
  }

  private static void validateApplicationName(String applicationName) {
    if (applicationName == null || applicationName.isBlank()) {
      throw new IllegalArgumentException("Application name cannot be null or empty");
    }
  }

  private static void validateSalt(String salt) {
    if (salt == null || salt.isBlank()) {
      throw new IllegalArgumentException("Salt cannot be null or empty");
    }
  }

  private static void validateIv(byte[] iv) {
    if (iv == null || iv.length == 0) {
      throw new IllegalArgumentException("IV cannot be null or empty");
    }
  }

  private static void validateCiphertext(byte[] ciphertext) {
    if (ciphertext == null || ciphertext.length == 0) {
      throw new IllegalArgumentException("Ciphertext cannot be null or empty");
    }
  }

  private static void validateAccountId(UUID accountId) {
    if (accountId == null) {
      throw new IllegalArgumentException("AccountId cannot be null");
    }
  }
}
