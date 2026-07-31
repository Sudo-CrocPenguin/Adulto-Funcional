package org.adultofuncional.main.security.infrastructure.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.adultofuncional.main.config.security.JwtProperties;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementación con Redis de {@link MasterKeySessionService} para
 * producción.
 *
 * <h2>¿Por qué Redis y no ConcurrentHashMap?</h2>
 * <ul>
 * <li><strong>Escalabilidad horizontal</strong> — Varias instancias de la
 * aplicación comparten el mismo Redis, de modo que la Master Key verificada en
 * una instancia es reconocida por las demás sin necesidad de afinidad de
 * sesión.</li>
 * <li><strong>Expiración automática</strong> — Cada clave tiene un TTL
 * (time‑to‑live) configurable tras el cual se elimina automáticamente,
 * liberando memoria aunque el usuario olvide cerrar el gestor.</li>
 * <li><strong>Estado efímero</strong> — Redis mantiene la sesión del gestor con
 * TTL y debe ejecutarse sin persistencia en disco porque la Master Key solo
 * pertenece a la sesión activa.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <p>
 * La Master Key nunca se guarda en Redis en texto plano. Antes de persistir el
 * valor temporal, se cifra con AES-GCM usando una clave derivada del secreto
 * JWT y un IV aleatorio por verificación. Se recomienda:
 * <ul>
 * <li>Usar una instancia de Redis dedicada (o una base de datos separada con
 * {@code SELECT db}) para las sesiones de Master Key.</li>
 * <li>Deshabilitar AOF/RDB para que las sesiones efímeras no sobrevivan a
 * reinicios ni queden volcadas en disco.</li>
 * <li>Habilitar TLS para la conexión entre la aplicación y Redis en entornos
 * donde el tráfico cruce redes no confiables.</li>
 * <li>Configurar una contraseña de acceso a Redis mediante
 * {@code requirepass} y la propiedad
 * {@code spring.data.redis.password}.</li>
 * </ul>
 *
 * <h2>Estructura de claves</h2>
 * <p>
 * Cada Master Key se almacena con la clave:
 * <pre>{@code master-key:<accountId>}</pre>
 * El valor es un payload cifrado con formato {@code base64(iv):base64(ciphertext)}.
 * El TTL se define en {@link #TTL_SECONDS} y se refresca en cada llamada a
 * {@link #verify}.
 *
 * <h2>Thread-safety</h2>
 * <p>
 * Las operaciones de Redis son atómicas por naturaleza, garantizando que
 * múltiples hilos o instancias puedan acceder concurrentemente sin
 * corrupción de datos.
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 * @see StringRedisTemplate
 * @see MasterKeySessionService
 */
@Component
@Profile("prod")
public class RedisMasterKeyService implements MasterKeySessionService {

  /**
   * Prefijo para todas las claves de Master Key en Redis.
   */
  private static final String KEY_PREFIX = "master-key:";

  private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

  private static final int GCM_TAG_LENGTH_BITS = 128;

  private static final int IV_LENGTH_BYTES = 12;

  private static final String KEY_DERIVATION_CONTEXT = "adulto-funcional:redis-master-key-session:v1";

  private static final String PAYLOAD_SEPARATOR = ":";

  /**
   * Tiempo de vida de la Master Key en Redis (1 hora).
   * <p>
   * Pasado este tiempo, el usuario deberá volver a verificar su Master Key
   * para acceder al gestor de contraseñas. Una hora es un valor razonable
   * para una sesión activa, pero puede ajustarse según la política de
   * seguridad del despliegue.
   */
  private static final long TTL_SECONDS = 3_600;

  private final StringRedisTemplate redisTemplate;

  private final SecretKey redisEncryptionKey;

  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Construye el servicio con el template de Redis proporcionado por Spring.
   *
   * @param redisTemplate template inyectado por Spring, configurado
   *                      automáticamente desde {@code spring.data.redis.*}.
   * @param jwtProperties propiedades JWT usadas como material raíz para derivar
   *                      la clave AES que protege la sesión en Redis.
   */
  public RedisMasterKeyService(StringRedisTemplate redisTemplate, JwtProperties jwtProperties) {
    this.redisTemplate = redisTemplate;
    this.redisEncryptionKey = deriveRedisEncryptionKey(jwtProperties.getSecret());
  }

  /**
   * Construye la clave Redis a partir del identificador de cuenta.
   *
   * @param accountId identificador de la cuenta
   * @return clave Redis con el formato {@code master-key:<accountId>}
   */
  private String buildKey(UUID accountId) {
    return KEY_PREFIX + accountId;
  }

  /**
   * Verifica si la Master Key está presente en Redis y no ha expirado.
   *
   * @param accountId identificador de la cuenta
   * @return {@code true} si la clave existe en Redis
   */
  @Override
  public boolean isVerified(UUID accountId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(accountId)));
  }

  /**
   * Obtiene la Master Key descifrada desde Redis.
   *
   * @param accountId identificador de la cuenta
   * @return Master Key en texto plano
   * @throws IllegalStateException si la clave no existe o expiró
   */
  @Override
  public String getMasterKey(UUID accountId) {
    String key = buildKey(accountId);
    String encryptedMasterKey = redisTemplate.opsForValue().get(key);
    if (encryptedMasterKey == null) {
      throw new IllegalStateException(
          "Master Key no verificada para la cuenta " + accountId);
    }
    return decrypt(encryptedMasterKey);
  }

  /**
   * Almacena la Master Key en Redis con TTL.
   * <p>
   * Si ya existía una clave para esta cuenta, el TTL se reinicia al valor
   * completo de {@link #TTL_SECONDS}. Esto permite que el usuario extienda
   * su sesión del gestor simplemente accediendo a él.
   *
   * @param accountId identificador de la cuenta
   * @param masterKey Master Key en texto plano
   */
  @Override
  public void verify(UUID accountId, String masterKey) {
    redisTemplate.opsForValue()
        .set(buildKey(accountId), encrypt(masterKey), TTL_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * Elimina la Master Key de Redis (logout del gestor).
   * <p>
   * Si la clave no existía, la operación no tiene efecto.
   *
   * @param accountId identificador de la cuenta
   */
  @Override
  public void clear(UUID accountId) {
    redisTemplate.delete(buildKey(accountId));
  }

  private SecretKey deriveRedisEncryptionKey(String jwtSecret) {
    if (jwtSecret == null || jwtSecret.length() < 32) {
      throw new IllegalStateException("JWT_SECRET debe tener mínimo 32 caracteres para proteger Redis");
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(KEY_DERIVATION_CONTEXT.getBytes(StandardCharsets.UTF_8));
      byte[] keyBytes = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
      return new SecretKeySpec(keyBytes, "AES");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no está disponible para derivar la clave de Redis", e);
    }
  }

  private String encrypt(String masterKey) {
    try {
      byte[] iv = new byte[IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, redisEncryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] ciphertext = cipher.doFinal(masterKey.getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(iv)
          + PAYLOAD_SEPARATOR
          + Base64.getEncoder().encodeToString(ciphertext);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No fue posible cifrar la Master Key de sesión", e);
    }
  }

  private String decrypt(String encryptedMasterKey) {
    String[] parts = encryptedMasterKey.split(PAYLOAD_SEPARATOR, 2);
    if (parts.length != 2) {
      throw new IllegalStateException("La Master Key de sesión tiene un formato inválido");
    }

    try {
      byte[] iv = Base64.getDecoder().decode(parts[0]);
      byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

      Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, redisEncryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("No fue posible descifrar la Master Key de sesión", e);
    }
  }
}
