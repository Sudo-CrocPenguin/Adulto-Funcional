package org.adultofuncional.main.auth.infrastructure.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.adultofuncional.main.auth.domain.service.AccessTokenRevocationService;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Lista distribuida de access tokens revocados respaldada por Redis. */
@Component
@Profile("prod")
public class RedisAccessTokenRevocationService implements AccessTokenRevocationService {

  private static final String KEY_PREFIX = "revoked-jti:";

  private final StringRedisTemplate redisTemplate;
  private final Clock clock;

  public RedisAccessTokenRevocationService(StringRedisTemplate redisTemplate, Clock clock) {
    this.redisTemplate = redisTemplate;
    this.clock = clock;
  }

  @Override
  public void revoke(UUID tokenId, Instant expiresAt) {
    if (tokenId == null || expiresAt == null) {
      return;
    }
    Duration ttl = Duration.between(clock.instant(), expiresAt);
    if (ttl.isPositive()) {
      redisTemplate.opsForValue().set(KEY_PREFIX + tokenId, "revoked", ttl);
    }
  }

  @Override
  public boolean isRevoked(UUID tokenId) {
    return tokenId != null && Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + tokenId));
  }
}
