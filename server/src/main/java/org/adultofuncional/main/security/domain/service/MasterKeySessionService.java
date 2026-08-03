package org.adultofuncional.main.security.domain.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto para mantener una Master Key efímera y aislada por sesión.
 *
 * <p>La clave compuesta {@code accountId + sessionId} evita que desbloquear la
 * bóveda en un dispositivo conceda acceso a las demás sesiones de la cuenta.
 * La lectura es atómica desde la perspectiva del consumidor: una expiración se
 * representa como {@link Optional#empty()} y nunca como una comprobación
 * seguida de otra lectura susceptible a carrera.</p>
 */
public interface MasterKeySessionService {

  /** Valor temporal recuperado junto con su expiración observable. */
  record UnlockedMasterKey(String value, Instant expiresAt) {
  }

  Optional<UnlockedMasterKey> find(UUID accountId, UUID sessionId);

  void unlock(UUID accountId, UUID sessionId, String masterKey);

  void clear(UUID accountId, UUID sessionId);

  void clearAll(UUID accountId);
}
