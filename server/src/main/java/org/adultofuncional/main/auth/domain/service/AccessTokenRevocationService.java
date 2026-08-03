package org.adultofuncional.main.auth.domain.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Puerto para bloquear access tokens hasta su expiración natural.
 *
 * <p>La lista es temporal: nunca necesita conservar un identificador después
 * de {@code exp}, por lo que los adaptadores deben aplicar ese TTL.</p>
 */
public interface AccessTokenRevocationService {

  void revoke(UUID tokenId, Instant expiresAt);

  boolean isRevoked(UUID tokenId);
}
