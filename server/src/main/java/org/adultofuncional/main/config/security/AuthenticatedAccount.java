package org.adultofuncional.main.config.security;

import java.time.Instant;
import java.util.UUID;

/**
 * Principal autenticado que Spring Security expone a los controladores.
 *
 * <p>
 * El identificador estable de autorización es {@code accountId}, tomado del
 * claim {@code sub} del JWT. El email se conserva como dato informativo para
 * compatibilidad y auditoría, pero no debe usarse para decidir ownership porque
 * puede cambiar durante la vida de una cuenta.
 *
 * @param accountId UUID estable de la cuenta autenticada
 * @param email                email incluido en el JWT al momento de emitirlo
 * @param sessionId            familia de autenticación identificada por {@code sid}
 * @param accessTokenId        token concreto identificado por {@code jti}
 * @param accessTokenExpiresAt expiración usada para calcular la revocación
 */
public record AuthenticatedAccount(
    UUID accountId,
    String email,
    UUID sessionId,
    UUID accessTokenId,
    Instant accessTokenExpiresAt) {

  /** Constructor de compatibilidad para pruebas y adaptadores sin sesión. */
  public AuthenticatedAccount(UUID accountId, String email) {
    this(accountId, email, null, null, null);
  }
}
