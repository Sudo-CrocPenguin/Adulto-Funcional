package org.adultofuncional.main.auth.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Par de tokens emitido para una familia de autenticación. */
public record SessionTokens(
    UUID accountId,
    UUID sessionId,
    String accessToken,
    Instant accessExpiresAt,
    String refreshToken,
    Instant refreshExpiresAt,
    List<String> roles) {

  public long accessExpiresInMillis(Instant now) {
    return Math.max(0L, accessExpiresAt.toEpochMilli() - now.toEpochMilli());
  }

  public long refreshExpiresInMillis(Instant now) {
    return Math.max(0L, refreshExpiresAt.toEpochMilli() - now.toEpochMilli());
  }
}
