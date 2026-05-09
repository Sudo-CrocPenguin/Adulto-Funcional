package org.adultofuncional.main.security.infrastructure.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.springframework.stereotype.Component;

/**
 * Implementación en memoria de {@link MasterKeySessionService} para desarrollo.
 *
 * <h2>¿Por qué en memoria y no en sesión HTTP?</h2>
 * <p>
 * La sesión HTTP ({@code HttpSession}) depende del contenedor web y complica
 * las pruebas unitarias. Además, nuestra aplicación es stateless (JWT). Esta
 * implementación es simple y efectiva para desarrollo. En producción se puede
 * reemplazar por un almacén distribuido (Redis) o un token efímero sin cambiar
 * el dominio.
 *
 * <h2>Thread-safety</h2>
 * <p>
 * {@link ConcurrentHashMap} garantiza que múltiples hilos puedan acceder
 * simultáneamente sin corrupción de datos.
 *
 * <h2>Limitaciones</h2>
 * <ul>
 * <li>No expira automáticamente: las claves quedan hasta que se llame a
 * {@link #clear} o se reinicie el servidor.</li>
 * <li>No sobrevive a reinicios: todas las sesiones se pierden al reiniciar
 * la aplicación.</li>
 * <li>No es escalable horizontalmente: en múltiples instancias, cada una
 * tendría su propio mapa.</li>
 * </ul>
 *
 * @author Equipo de desarrollo Adulto Funcional
 * @since 0.0.1
 */
@Component
public class InMemoryMasterKeyService implements MasterKeySessionService {

  // Mapa concurrente: accountId → Master Key en texto plano
  private final Map<UUID, String> store = new ConcurrentHashMap<>();

  /**
   * Verifica si la Master Key está presente para la cuenta.
   * Complejidad: O(1).
   */
  @Override
  public boolean isVerified(UUID accountId) {
    return store.containsKey(accountId);
  }

  /**
   * Obtiene la Master Key en texto plano.
   *
   * @throws IllegalStateException si no ha sido verificada antes.
   */
  @Override
  public String getMasterKey(UUID accountId) {
    String key = store.get(accountId);
    if (key == null) {
      throw new IllegalStateException(
          "Master Key no verificada para la cuenta " + accountId);
    }
    return key;
  }

  /**
   * Almacena la Master Key después de la verificación exitosa.
   * Si ya existía, la sobrescribe.
   */
  @Override
  public void verify(UUID accountId, String masterKey) {
    store.put(accountId, masterKey);
  }

  /**
   * Elimina la Master Key (logout del gestor).
   * Si no existía, no hace nada.
   */
  @Override
  public void clear(UUID accountId) {
    store.remove(accountId);
  }
}
