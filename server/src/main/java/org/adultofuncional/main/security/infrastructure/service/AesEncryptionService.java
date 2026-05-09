package org.adultofuncional.main.security.infrastructure.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.springframework.stereotype.Component;

/**
 * Implementación concreta de {@link EncryptionService} usando AES‑256 en modo
 * GCM con derivación de clave PBKDF2 (HMAC‑SHA256).
 *
 * <h2>¿Por qué AES‑256‑GCM?</h2>
 * <ul>
 * <li><strong>AES</strong> es el estándar de cifrado simétrico más usado.</li>
 * <li><strong>256 bits</strong> ofrece un margen de seguridad alto.</li>
 * <li><strong>GCM (Galois/Counter Mode)</strong> proporciona cifrado
 * autenticado: además de ocultar el texto, garantiza que no ha sido
 * modificado (integridad). Si alguien altera el ciphertext, el descifrado
 * fallará.</li>
 * </ul>
 *
 * <h2>¿Por qué derivamos la clave con PBKDF2?</h2>
 * <p>
 * La Master Key del usuario puede no tener exactamente 256 bits o puede tener
 * poca entropía. PBKDF2 aplica un hash iterativo (100 000 rondas) con un salt
 * para producir una clave robusta de 256 bits. Esto hace que sea
 * computacionalmente costoso para un atacante probar claves por fuerza bruta.
 *
 * <h2>Parámetros fijos</h2>
 * <ul>
 * <li>Algoritmo: AES/GCM/NoPadding</li>
 * <li>Tamaño de clave: 256 bits</li>
 * <li>Tamaño de IV: 12 bytes (recomendado por NIST para GCM)</li>
 * <li>Tag de autenticación: 128 bits</li>
 * <li>PBKDF2 iteraciones: 100 000</li>
 * </ul>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Component
public class AesEncryptionService implements EncryptionService {

  // Algoritmo de cifrado: AES en modo GCM sin padding (GCM no necesita padding)
  private static final String ALGORITHM = "AES/GCM/NoPadding";

  // Tamaño de la clave AES en bits (256 bits = 32 bytes)
  private static final int KEY_SIZE = 256;

  // Tamaño del IV en bytes (12 bytes es lo recomendado por NIST para GCM)
  private static final int IV_SIZE = 12;

  // Tamaño del tag de autenticación en bits (128 bits = 16 bytes)
  private static final int TAG_SIZE = 128;

  // Número de iteraciones para PBKDF2 (100 000 es un valor seguro en 2025)
  private static final int PBKDF2_ITERATIONS = 100_000;

  // Algoritmo de derivación de clave: PBKDF2 con HMAC-SHA256
  private static final String PBKDF2_ALG = "PBKDF2WithHmacSHA256";

  /**
   * Cifra una contraseña en texto plano.
   *
   * <h2>Flujo detallado</h2>
   * <ol>
   * <li><strong>Generar salt aleatorio (16 bytes):</strong> El salt asegura
   * que dos cifrados de la misma contraseña con la misma Master Key
   * produzcan resultados diferentes. Se codifica en Base64 para
   * almacenarlo como texto en la BD.</li>
   * <li><strong>Generar IV aleatorio (12 bytes):</strong> El IV (vector de
   * inicialización) garantiza que el mismo texto cifrado con la misma
   * clave produzca ciphertexts diferentes. Nunca debe reutilizarse con
   * la misma clave.</li>
   * <li><strong>Derivar clave AES:</strong> A partir de la Master Key y el
   * salt, PBKDF2 genera una clave de 256 bits.</li>
   * <li><strong>Inicializar cifrador:</strong> Se crea un Cipher AES-GCM
   * con la clave y el IV. El tag de autenticación se configura a 128
   * bits.</li>
   * <li><strong>Cifrar:</strong> El texto plano se convierte a bytes y se
   * cifra. El resultado incluye automáticamente el tag de autenticación
   * al final.</li>
   * </ol>
   *
   * @param plainPassword contraseña en texto plano a cifrar
   * @param masterKey     Master Key del usuario en texto plano
   * @return datos cifrados con salt (Base64), IV y ciphertext
   */
  @Override
  public EncryptedData encrypt(String plainPassword, String masterKey) {
    try {
      // 1. Generar salt aleatorio de 16 bytes
      byte[] salt = new byte[16];
      SecureRandom.getInstanceStrong().nextBytes(salt);
      String saltB64 = Base64.getEncoder().encodeToString(salt);

      // 2. Generar IV aleatorio de 12 bytes
      byte[] iv = new byte[IV_SIZE];
      SecureRandom.getInstanceStrong().nextBytes(iv);

      // 3. Derivar clave AES de 256 bits desde la Master Key + salt
      SecretKey key = deriveKey(masterKey, salt);

      // 4. Crear y configurar el cifrador AES-GCM
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
      cipher.init(Cipher.ENCRYPT_MODE, key, spec);

      // 5. Cifrar la contraseña (el resultado incluye el tag al final)
      byte[] ciphertext = cipher.doFinal(plainPassword.getBytes());

      return new EncryptedData(saltB64, iv, ciphertext);
    } catch (Exception e) {
      // Convertir excepciones checked en runtime para no contaminar la interfaz
      throw new RuntimeException("Error al cifrar la contraseña", e);
    }
  }

  /**
   * Descifra una contraseña previamente cifrada.
   *
   * <h2>Flujo detallado</h2>
   * <ol>
   * <li><strong>Decodificar salt:</strong> El salt se recupera de Base64 a
   * bytes.</li>
   * <li><strong>Derivar clave AES:</strong> Se usa la misma Master Key y el
   * mismo salt para regenerar exactamente la misma clave que se usó al
   * cifrar.</li>
   * <li><strong>Inicializar descifrador:</strong> Se configura AES-GCM con
   * la clave y el IV original. El tag de autenticación está incluido en
   * el ciphertext (últimos 16 bytes).</li>
   * <li><strong>Descifrar:</strong> Si el ciphertext fue alterado, GCM
   * lanzará una excepción porque el tag no coincidirá.</li>
   * </ol>
   *
   * @param saltB64    salt usado en el cifrado (Base64)
   * @param iv         vector de inicialización original
   * @param ciphertext texto cifrado (incluye tag)
   * @param masterKey  Master Key del usuario en texto plano
   * @return contraseña original en texto plano
   */
  @Override
  public String decrypt(String saltB64, byte[] iv, byte[] ciphertext, String masterKey) {
    try {
      // 1. Decodificar salt de Base64 a bytes
      byte[] salt = Base64.getDecoder().decode(saltB64);

      // 2. Derivar la misma clave AES
      SecretKey key = deriveKey(masterKey, salt);

      // 3. Crear y configurar el descifrador AES-GCM
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
      cipher.init(Cipher.DECRYPT_MODE, key, spec);

      // 4. Descifrar (si el ciphertext fue modificado, lanzará excepción)
      byte[] plain = cipher.doFinal(ciphertext);
      return new String(plain);
    } catch (Exception e) {
      throw new RuntimeException("Error al descifrar la contraseña", e);
    }
  }

  /**
   * Deriva una clave AES de 256 bits a partir de la Master Key y un salt.
   *
   * <h2>¿Cómo funciona PBKDF2?</h2>
   * <p>
   * PBKDF2 (Password-Based Key Derivation Function 2) aplica repetidamente
   * una función hash (HMAC-SHA256 en nuestro caso) sobre la combinación de
   * la contraseña y el salt. Cada iteración hace que derivar la clave sea
   * más costoso, dificultando ataques de fuerza bruta.
   *
   * <h2>¿Por qué 100 000 iteraciones?</h2>
   * <p>
   * Es un valor recomendado por OWASP para PBKDF2-HMAC-SHA256 en 2025.
   * A mayor número, más seguro pero más lento. 100 000 balancea seguridad
   * y rendimiento.
   *
   * @param masterKey clave maestra del usuario
   * @param salt      salt aleatorio (16 bytes)
   * @return clave AES de 256 bits lista para usar
   */
  private SecretKey deriveKey(String masterKey, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Configurar PBKDF2: contraseña (Master Key), salt, iteraciones, tamaño
    PBEKeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt,
        PBKDF2_ITERATIONS, KEY_SIZE);

    // Obtener fábrica para PBKDF2 con HMAC-SHA256
    SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALG);

    // Generar clave y obtener sus bytes (256 bits = 32 bytes)
    byte[] keyBytes = factory.generateSecret(spec).getEncoded();

    // Crear clave AES a partir de los bytes derivados
    return new SecretKeySpec(keyBytes, "AES");
  }
}
