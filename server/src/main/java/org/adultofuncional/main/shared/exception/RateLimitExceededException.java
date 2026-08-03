package org.adultofuncional.main.shared.exception;

import java.time.Duration;

import org.adultofuncional.main.shared.response.ApiErrorCode;

/** Error 429 que conserva el tiempo seguro para reintentar. */
public class RateLimitExceededException extends BusinessException {

  private final long retryAfterSeconds;

  public RateLimitExceededException(Duration retryAfter) {
    super(
        "Demasiados intentos; espera antes de volver a intentarlo",
        429,
        ApiErrorCode.RATE_LIMIT_EXCEEDED);
    retryAfterSeconds = Math.max(1L, (retryAfter.toMillis() + 999L) / 1_000L);
  }

  public long getRetryAfterSeconds() {
    return retryAfterSeconds;
  }
}
