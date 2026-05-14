package org.adultofuncional.main.security.infrastructure.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
 * <li><strong>Persistencia opcional</strong> — Redis puede persistir los datos
 * en disco (RDB / AOF), permitiendo que las sesiones activas sobrevivan a
 * reinicios del contenedor de Redis.</li>
 * </ul>
 *
 * <h2>Seguridad</h2>
 * <p>
 * La Master Key se almacena en Redis en texto plano durante el TTL
 * configurado. Se recomienda:
 * <ul>
 * <li>Usar una instancia de Redis dedicada (o una base de datos separada con
 * {@code SELECT db}) para las sesiones de Master Key.</li>
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
 * El valor es la Master Key en texto plano. El TTL se define en
 * {@link #TTL_SECONDS} y se refresca en cada llamada a {@link #verify}.
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

  /**
   * Construye el servicio con el template de Redis proporcionado por Spring.
   *
   * @param redisTemplate template inyectado por Spring, configurado
   *                      automáticamente desde {@code spring.data.redis.*}.
   */
  public RedisMasterKeyService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
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
   * Obtiene la Master Key en texto plano desde Redis.
   *
   * @param accountId identificador de la cuenta
   * @return Master Key en texto plano
   * @throws IllegalStateException si la clave no existe o expiró
   */
  @Override
  public String getMasterKey(UUID accountId) {
    String key = buildKey(accountId);
    String masterKey = redisTemplate.opsForValue().get(key);
    if (masterKey == null) {
      throw new IllegalStateException(
          "Master Key no verificada para la cuenta " + accountId);
    }
    return masterKey;
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
        .set(buildKey(accountId), masterKey, TTL_SECONDS, TimeUnit.SECONDS);
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
}
