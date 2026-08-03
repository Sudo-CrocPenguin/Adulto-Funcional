package org.adultofuncional.main.auth.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.adultofuncional.main.config.security.AuthSessionProperties;
import org.springframework.stereotype.Service;

/** Genera refresh tokens de alta entropía y calcula su huella persistible. */
@Service
public class RefreshTokenService {

  private final SecureRandom secureRandom = new SecureRandom();
  private final int tokenBytes;

  public RefreshTokenService(AuthSessionProperties properties) {
    if (properties.getRefreshTokenBytes() < 32) {
      throw new IllegalStateException("auth.session.refresh-token-bytes debe ser al menos 32");
    }
    tokenBytes = properties.getRefreshTokenBytes();
  }

  /** Genera un valor opaco apto para transporte URL y cookies. */
  public String generate() {
    byte[] bytes = new byte[tokenBytes];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /** Calcula SHA-256; la entrada ya contiene al menos 256 bits aleatorios. */
  public String hash(String token) {
    if (token == null || token.isBlank()) {
      return "";
    }
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 no está disponible", exception);
    }
  }
}
