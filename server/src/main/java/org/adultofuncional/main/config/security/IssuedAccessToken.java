package org.adultofuncional.main.config.security;

import java.time.Instant;
import java.util.UUID;

/** Resultado inmutable de firmar un access token. */
public record IssuedAccessToken(
    String value,
    UUID tokenId,
    Instant issuedAt,
    Instant expiresAt) {
}
