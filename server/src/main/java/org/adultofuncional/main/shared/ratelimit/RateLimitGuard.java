package org.adultofuncional.main.shared.ratelimit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import org.adultofuncional.main.shared.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Fachada que anonimiza sujetos, aplica la política y publica métricas.
 *
 * <p>Emails, IP y UUID nunca forman parte de nombres de métricas ni claves
 * legibles. Solo el nombre de política se usa como etiqueta de baja
 * cardinalidad.</p>
 */
@Service
@RequiredArgsConstructor
public class RateLimitGuard {

  private final RateLimitStore store;
  private final MeterRegistry meterRegistry;

  /** Rechaza antes de ejecutar una operación si el sujeto sigue bloqueado. */
  public void check(RateLimitPolicy policy, String subject) {
    Duration retryAfter = store.check(key(policy, subject));
    recordMetric(policy, retryAfter.isZero() ? "allowed" : "blocked");
    rejectIfBlocked(retryAfter);
  }

  /** Consume una solicitud para políticas de volumen independientes del resultado. */
  public void consume(RateLimitPolicy policy, String subject) {
    check(policy, subject);
    recordFailure(policy, subject);
    check(policy, subject);
  }

  /** Registra un fallo; una comprobación posterior aplica el nuevo bloqueo. */
  public void recordFailure(RateLimitPolicy policy, String subject) {
    Duration retryAfter = store.recordFailure(key(policy, subject), policy);
    recordMetric(policy, retryAfter.isZero() ? "failure" : "blocked");
  }

  /** Reinicia el contador tras validar correctamente la credencial. */
  public void reset(RateLimitPolicy policy, String subject) {
    store.reset(key(policy, subject));
    recordMetric(policy, "success");
  }

  private void rejectIfBlocked(Duration retryAfter) {
    if (!retryAfter.isZero()) {
      throw new RateLimitExceededException(retryAfter);
    }
  }

  private void recordMetric(RateLimitPolicy policy, String outcome) {
    meterRegistry.counter(
        "security.rate_limit.attempts",
        "policy", policy.name(),
        "outcome", outcome).increment();
  }

  private String key(RateLimitPolicy policy, String subject) {
    return policy.name().toLowerCase() + ":{" + digest(subject) + "}";
  }

  private String digest(String value) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256")
          .digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 no está disponible", exception);
    }
  }
}
