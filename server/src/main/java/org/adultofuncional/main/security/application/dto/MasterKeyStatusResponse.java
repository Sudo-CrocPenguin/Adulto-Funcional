package org.adultofuncional.main.security.application.dto;

import java.time.Instant;

/** Estado público de configuración y desbloqueo de la sesión actual. */
public record MasterKeyStatusResponse(
    boolean configured,
    boolean verified,
    Instant expiresAt) {

  public static MasterKeyStatusResponse locked(boolean configured) {
    return new MasterKeyStatusResponse(configured, false, null);
  }
}
