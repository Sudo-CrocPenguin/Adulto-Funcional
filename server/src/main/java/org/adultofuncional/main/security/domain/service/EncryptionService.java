package org.adultofuncional.main.security.domain.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Puerto de dominio para el cifrado y descifrado de contraseñas del gestor.
 *
 * <p>
 * Abstrae el algoritmo criptográfico y la derivación de clave desde la
 * Master Key del usuario. La implementación concreta (AES‑256‑GCM) reside
 * en la capa de infraestructura.
 *
 * <h2>Funcionamiento general</h2>
 * <ol>
 * <li>El usuario ingresa su Master Key una vez por sesión.</li>
 * <li>Al guardar una credencial, se llama a {@link #encrypt} con la
 * contraseña en texto plano y la Master Key.</li>
 * <li>El método genera un salt aleatorio, deriva una clave AES de 256 bits
 * usando PBKDF2 con la Master Key y el salt, y cifra la contraseña en
 * modo GCM (que incluye integridad).</li>
 * <li>Los resultados (salt, IV, ciphertext) se almacenan en la base de
 * datos.</li>
 * <li>Para descifrar, se llama a {@link #decrypt} con los mismos parámetros y
 * la Master Key.</li>
 * </ol>
 *
 * <p>
 * <strong>¿Por qué un salt por credencial?</strong><br>
 * Si dos usuarios tienen la misma Master Key (poco probable, pero posible) y
 * guardan la misma contraseña, el ciphertext sería idéntico sin salt. Con un
 * salt único por credencial, cada cifrado produce un resultado distinto aunque
 * la entrada sea igual.
 *
 * @author Juan Sebastian Rioss
 * @since 0.0.1
 */
public interface EncryptionService {

  /**
   * Cifra una contraseña en texto plano.
   *
   * @param plainPassword contraseña que el usuario quiere guardar
   * @param masterKey     Master Key del usuario en texto plano
   * @return contenedor con salt (Base64), IV (12 bytes) y ciphertext
   */
  EncryptedData encrypt(String plainPassword, String masterKey);

  /** Cifra vinculando el resultado a su cuenta y credencial mediante AAD. */
  EncryptedData encrypt(
      String plainPassword,
      String masterKey,
      EncryptionContext context);

  /**
   * Descifra una contraseña previamente cifrada.
   *
   * @param salt       salt usado en el cifrado (Base64)
   * @param iv         vector de inicialización (12 bytes)
   * @param ciphertext texto cifrado (incluye tag de autenticación GCM)
   * @param masterKey  Master Key del usuario en texto plano
   * @return contraseña original en texto plano
   */
  String decrypt(String salt, byte[] iv, byte[] ciphertext, String masterKey);

  /** Descifra con los parámetros de la versión persistida y su AAD estable. */
  String decrypt(
      String salt,
      byte[] iv,
      byte[] ciphertext,
      String masterKey,
      int cryptoVersion,
      EncryptionContext context);

  /**
   * Contenedor inmutable de los datos generados durante el cifrado.
   *
   * @param salt       salt aleatorio codificado en Base64
   * @param iv         vector de inicialización de 12 bytes
   * @param ciphertext texto cifrado (incluye tag GCM)
   * @param cryptoVersion versión de algoritmo y parámetros
   */
  record EncryptedData(String salt, byte[] iv, byte[] ciphertext, int cryptoVersion) {
  }

  /** Identidad inmutable autenticada junto con el ciphertext. */
  record EncryptionContext(UUID accountId, UUID credentialId) {
    public EncryptionContext {
      Objects.requireNonNull(accountId, "accountId is required");
      Objects.requireNonNull(credentialId, "credentialId is required");
    }
  }
}
