package org.adultofuncional.main.security.infrastructure.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.adultofuncional.main.security.domain.service.EncryptionException;
import org.adultofuncional.main.security.domain.service.EncryptionException.Reason;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.springframework.stereotype.Component;

/**
 * Cifra secretos con AES-256-GCM y parámetros versionados.
 *
 * <p>La versión 1 conserva compatibilidad con filas históricas: PBKDF2-HMAC-
 * SHA256 con 100.000 iteraciones y sin AAD. La versión 2 es el formato actual:
 * PBKDF2-HMAC-SHA256 con 600.000 iteraciones y AAD formado por la cuenta, la
 * credencial y la versión. El AAD hace detectable mover un payload completo a
 * otra fila aunque salt, IV y ciphertext se sustituyan juntos.</p>
 */
@Component
public class AesEncryptionService implements EncryptionService {

  public static final int CURRENT_VERSION = 2;

  private static final int LEGACY_VERSION = 1;
  private static final int LEGACY_ITERATIONS = 100_000;
  private static final int CURRENT_ITERATIONS = 600_000;
  private static final int KEY_SIZE_BITS = 256;
  private static final int SALT_SIZE_BYTES = 16;
  private static final int IV_SIZE_BYTES = 12;
  private static final int TAG_SIZE_BITS = 128;
  private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
  private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final String AAD_PREFIX = "adulto-funcional:password:v";

  private final SecureRandom secureRandom;

  public AesEncryptionService() {
    this(new SecureRandom());
  }

  AesEncryptionService(SecureRandom secureRandom) {
    this.secureRandom = secureRandom;
  }

  /** Produce deliberadamente v1 solo para conservar el contrato Java antiguo. */
  @Override
  public EncryptedData encrypt(String plainPassword, String masterKey) {
    return encryptVersion(plainPassword, masterKey, LEGACY_VERSION, null);
  }

  @Override
  public EncryptedData encrypt(
      String plainPassword,
      String masterKey,
      EncryptionContext context) {
    requireContext(context);
    return encryptVersion(plainPassword, masterKey, CURRENT_VERSION, context);
  }

  /** Interpreta el contrato Java histórico como una fila v1. */
  @Override
  public String decrypt(String salt, byte[] iv, byte[] ciphertext, String masterKey) {
    return decrypt(salt, iv, ciphertext, masterKey, LEGACY_VERSION, null);
  }

  @Override
  public String decrypt(
      String salt,
      byte[] iv,
      byte[] ciphertext,
      String masterKey,
      int cryptoVersion,
      EncryptionContext context) {
    validateInput(salt, iv, ciphertext, masterKey, cryptoVersion, context);
    try {
      byte[] decodedSalt = Base64.getDecoder().decode(salt);
      SecretKey key = deriveKey(masterKey, decodedSalt, iterations(cryptoVersion));
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));
      applyAad(cipher, cryptoVersion, context);
      return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    } catch (AEADBadTagException exception) {
      throw new EncryptionException(
          Reason.AUTHENTICATION_FAILED,
          "No fue posible autenticar la credencial cifrada",
          exception);
    } catch (IllegalArgumentException exception) {
      throw new EncryptionException(
          Reason.INVALID_INPUT,
          "La credencial cifrada tiene un formato inválido",
          exception);
    } catch (GeneralSecurityException exception) {
      throw new EncryptionException(
          Reason.INTERNAL_FAILURE,
          "Falló el proveedor criptográfico al descifrar",
          exception);
    }
  }

  private EncryptedData encryptVersion(
      String plainPassword,
      String masterKey,
      int version,
      EncryptionContext context) {
    validatePlainInput(plainPassword, masterKey);
    try {
      byte[] salt = randomBytes(SALT_SIZE_BYTES);
      byte[] iv = randomBytes(IV_SIZE_BYTES);
      SecretKey key = deriveKey(masterKey, salt, iterations(version));
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));
      applyAad(cipher, version, context);
      byte[] ciphertext = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));
      return new EncryptedData(
          Base64.getEncoder().encodeToString(salt),
          iv,
          ciphertext,
          version);
    } catch (GeneralSecurityException exception) {
      throw new EncryptionException(
          Reason.INTERNAL_FAILURE,
          "Falló el proveedor criptográfico al cifrar",
          exception);
    }
  }

  private SecretKey deriveKey(String masterKey, byte[] salt, int iterations)
      throws GeneralSecurityException {
    PBEKeySpec spec = new PBEKeySpec(
        masterKey.toCharArray(),
        salt,
        iterations,
        KEY_SIZE_BITS);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(KDF_ALGORITHM);
      return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    } catch (InvalidKeySpecException exception) {
      throw exception;
    } finally {
      spec.clearPassword();
    }
  }

  private void applyAad(Cipher cipher, int version, EncryptionContext context) {
    if (version == CURRENT_VERSION) {
      cipher.updateAAD((AAD_PREFIX + version
          + ":" + context.accountId()
          + ":" + context.credentialId()).getBytes(StandardCharsets.UTF_8));
    }
  }

  private int iterations(int version) {
    return switch (version) {
      case LEGACY_VERSION -> LEGACY_ITERATIONS;
      case CURRENT_VERSION -> CURRENT_ITERATIONS;
      default -> throw new EncryptionException(
          Reason.UNSUPPORTED_VERSION,
          "La versión criptográfica no está soportada");
    };
  }

  private void validateInput(
      String salt,
      byte[] iv,
      byte[] ciphertext,
      String masterKey,
      int version,
      EncryptionContext context) {
    if (salt == null || salt.isBlank()
        || iv == null || iv.length != IV_SIZE_BYTES
        || ciphertext == null || ciphertext.length == 0
        || masterKey == null || masterKey.isEmpty()) {
      throw new EncryptionException(Reason.INVALID_INPUT, "El material criptográfico es inválido");
    }
    iterations(version);
    if (version == CURRENT_VERSION) {
      requireContext(context);
    }
  }

  private void validatePlainInput(String plainPassword, String masterKey) {
    if (plainPassword == null || masterKey == null || masterKey.isEmpty()) {
      throw new EncryptionException(Reason.INVALID_INPUT, "El texto plano o la clave son inválidos");
    }
  }

  private void requireContext(EncryptionContext context) {
    if (context == null) {
      throw new EncryptionException(
          Reason.INVALID_INPUT,
          "El contexto de cifrado es obligatorio para la versión actual");
    }
  }

  private byte[] randomBytes(int size) {
    byte[] value = new byte[size];
    secureRandom.nextBytes(value);
    return value;
  }
}
