package org.adultofuncional.main.auth.infrastructure.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.adultofuncional.main.auth.domain.service.AccessTokenRevocationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Lista de revocación con expiración funcional para desarrollo y pruebas. */
@Component
@Profile({"dev", "test"})
public class InMemoryAccessTokenRevocationService implements AccessTokenRevocationService {

  private final Map<UUID, Instant> revokedTokens = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryAccessTokenRevocationService(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void revoke(UUID tokenId, Instant expiresAt) {
    if (tokenId != null && expiresAt != null && expiresAt.isAfter(clock.instant())) {
      revokedTokens.put(tokenId, expiresAt);
    }
  }

  @Override
  public boolean isRevoked(UUID tokenId) {
    Instant expiresAt = revokedTokens.get(tokenId);
    if (expiresAt == null) {
      return false;
    }
    if (!expiresAt.isAfter(clock.instant())) {
      revokedTokens.remove(tokenId, expiresAt);
      return false;
    }
    return true;
  }
}
