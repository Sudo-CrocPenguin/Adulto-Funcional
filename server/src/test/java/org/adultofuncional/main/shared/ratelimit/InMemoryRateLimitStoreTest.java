package org.adultofuncional.main.shared.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class InMemoryRateLimitStoreTest {

  @Test
  void appliesAnIncreasingBackoffAfterTheAttemptLimit() {
    MutableClock clock = new MutableClock(Instant.parse("2026-08-03T15:00:00Z"));
    InMemoryRateLimitStore store = new InMemoryRateLimitStore(clock);
    RateLimitPolicy policy = RateLimitPolicy.LOGIN_ACCOUNT;

    for (int attempt = 0; attempt < policy.attempts(); attempt++) {
      assertThat(store.recordFailure("account:{hash}", policy)).isZero();
    }

    assertThat(store.recordFailure("account:{hash}", policy)).isEqualTo(Duration.ofSeconds(5));
    assertThat(store.check("account:{hash}")).isEqualTo(Duration.ofSeconds(5));
    clock.advance(Duration.ofSeconds(5));
    assertThat(store.recordFailure("account:{hash}", policy)).isEqualTo(Duration.ofSeconds(10));

    store.reset("account:{hash}");

    assertThat(store.check("account:{hash}")).isZero();
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    void advance(Duration duration) {
      instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
