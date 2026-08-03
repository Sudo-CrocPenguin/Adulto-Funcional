package org.adultofuncional.main.shared.ratelimit;

import java.time.Duration;

/** Puerto de almacenamiento atómico para contadores de abuso. */
public interface RateLimitStore {

  /**
   * Consulta si un sujeto continúa bloqueado sin consumir un intento.
   *
   * @return tiempo restante de bloqueo o {@link Duration#ZERO} si se permite
   */
  Duration check(String key);

  /**
   * Registra un intento fallido y calcula la espera progresiva aplicable.
   *
   * @return tiempo de bloqueo o {@link Duration#ZERO} mientras queden intentos
   */
  Duration recordFailure(String key, RateLimitPolicy policy);

  /** Elimina el historial cuando una credencial se valida correctamente. */
  void reset(String key);
}
