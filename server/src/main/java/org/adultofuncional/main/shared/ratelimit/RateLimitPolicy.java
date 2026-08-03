package org.adultofuncional.main.shared.ratelimit;

import java.time.Duration;

/** Políticas de abuso con ventana y espera exponencial acotada. */
public enum RateLimitPolicy {
  LOGIN_IP(10, Duration.ofMinutes(15), Duration.ofSeconds(2), Duration.ofMinutes(2)),
  LOGIN_ACCOUNT(5, Duration.ofMinutes(15), Duration.ofSeconds(5), Duration.ofMinutes(5)),
  REGISTER_IP(20, Duration.ofHours(1), Duration.ofSeconds(10), Duration.ofMinutes(10)),
  REFRESH_IP(30, Duration.ofMinutes(1), Duration.ofSeconds(2), Duration.ofMinutes(1)),
  MASTER_KEY_SESSION(5, Duration.ofMinutes(15), Duration.ofSeconds(5), Duration.ofMinutes(5)),
  VAULT_CRYPTO_SESSION(120, Duration.ofMinutes(1), Duration.ofSeconds(1), Duration.ofSeconds(30));

  private final int attempts;
  private final Duration window;
  private final Duration baseBackoff;
  private final Duration maxBackoff;

  RateLimitPolicy(int attempts, Duration window, Duration baseBackoff, Duration maxBackoff) {
    this.attempts = attempts;
    this.window = window;
    this.baseBackoff = baseBackoff;
    this.maxBackoff = maxBackoff;
  }

  public int attempts() {
    return attempts;
  }

  public Duration window() {
    return window;
  }

  public Duration baseBackoff() {
    return baseBackoff;
  }

  public Duration maxBackoff() {
    return maxBackoff;
  }
}
