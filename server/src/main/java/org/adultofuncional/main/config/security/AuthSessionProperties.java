package org.adultofuncional.main.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** Propiedades del ciclo de vida de las familias de autenticación. */
@Component
@ConfigurationProperties(prefix = "auth.session")
@Getter
@Setter
public class AuthSessionProperties {

  /** Duración predeterminada del refresh token: treinta días. */
  private long refreshExpiration = 2_592_000_000L;

  /** Ventana tolerada para dos refresh concurrentes legítimos. */
  private long replayWindowSeconds = 5L;

  /** Entropía del refresh token opaco antes de codificarlo en Base64 URL. */
  private int refreshTokenBytes = 32;
}
