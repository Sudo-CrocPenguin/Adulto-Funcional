package org.adultofuncional.main.shared.ratelimit;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** Adaptador Redis que cuenta y bloquea mediante un único script atómico. */
@Component
@Profile("prod")
public class RedisRateLimitStore implements RateLimitStore {

  private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>("""
      local blockedTtl = redis.call('PTTL', KEYS[2])
      if blockedTtl > 0 then
        return blockedTtl
      end

      local count = redis.call('INCR', KEYS[1])
      if count == 1 then
        redis.call('PEXPIRE', KEYS[1], ARGV[2])
      end

      local limit = tonumber(ARGV[1])
      if count <= limit then
        return 0
      end

      local exponent = math.min(20, count - limit - 1)
      local delay = math.min(tonumber(ARGV[4]), tonumber(ARGV[3]) * (2 ^ exponent))
      redis.call('SET', KEYS[2], 'blocked', 'PX', delay)
      return delay
      """, Long.class);

  private final StringRedisTemplate redisTemplate;

  public RedisRateLimitStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Duration check(String key) {
    Long retryMillis = redisTemplate.getExpire(blockedKey(key), TimeUnit.MILLISECONDS);
    return retryMillis == null || retryMillis <= 0
        ? Duration.ZERO
        : Duration.ofMillis(retryMillis);
  }

  @Override
  public Duration recordFailure(String key, RateLimitPolicy policy) {
    Long retryMillis = redisTemplate.execute(
        CONSUME_SCRIPT,
        List.of(attemptsKey(key), blockedKey(key)),
        Integer.toString(policy.attempts()),
        Long.toString(policy.window().toMillis()),
        Long.toString(policy.baseBackoff().toMillis()),
        Long.toString(policy.maxBackoff().toMillis()));
    return retryMillis == null || retryMillis <= 0
        ? Duration.ZERO
        : Duration.ofMillis(retryMillis);
  }

  @Override
  public void reset(String key) {
    redisTemplate.delete(List.of(attemptsKey(key), blockedKey(key)));
  }

  private String attemptsKey(String key) {
    return "rate-limit:" + key + ":attempts";
  }

  private String blockedKey(String key) {
    return "rate-limit:" + key + ":blocked";
  }
}
