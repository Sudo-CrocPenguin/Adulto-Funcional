package org.adultofuncional.main.shared.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Adaptador local thread-safe con la misma espera progresiva que Redis. */
@Component
@Profile({"dev", "test"})
public class InMemoryRateLimitStore implements RateLimitStore {

  private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryRateLimitStore(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Duration check(String key) {
    Instant now = clock.instant();
    AttemptState state = attempts.get(key);
    if (state == null) {
      return Duration.ZERO;
    }
    if (!state.windowExpiresAt().isAfter(now)) {
      attempts.remove(key, state);
      return Duration.ZERO;
    }
    return remainingBlock(state, now);
  }

  @Override
  public Duration recordFailure(String key, RateLimitPolicy policy) {
    Instant now = clock.instant();
    AttemptState state = attempts.compute(key, (ignored, current) -> next(current, policy, now));
    return remainingBlock(state, now);
  }

  @Override
  public void reset(String key) {
    attempts.remove(key);
  }

  private AttemptState next(AttemptState current, RateLimitPolicy policy, Instant now) {
    if (current == null || !current.windowExpiresAt().isAfter(now)) {
      return new AttemptState(1, now.plus(policy.window()), Instant.EPOCH);
    }
    if (current.blockedUntil().isAfter(now)) {
      return current;
    }
    int count = current.count() + 1;
    if (count <= policy.attempts()) {
      return new AttemptState(count, current.windowExpiresAt(), Instant.EPOCH);
    }
    int exponent = Math.min(20, count - policy.attempts() - 1);
    long delay = Math.min(
        policy.maxBackoff().toMillis(),
        policy.baseBackoff().toMillis() * (1L << exponent));
    return new AttemptState(count, current.windowExpiresAt(), now.plusMillis(delay));
  }

  private Duration remainingBlock(AttemptState state, Instant now) {
    return state.blockedUntil().isAfter(now)
        ? Duration.between(now, state.blockedUntil())
        : Duration.ZERO;
  }

  private record AttemptState(int count, Instant windowExpiresAt, Instant blockedUntil) {
  }
}
