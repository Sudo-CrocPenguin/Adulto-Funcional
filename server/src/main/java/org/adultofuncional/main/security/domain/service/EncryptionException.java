package org.adultofuncional.main.security.domain.service;

/**
 * Fallo criptográfico clasificado sin incluir secretos en el mensaje.
 *
 * <p>La causa distingue material alterado o clave equivocada, una versión no
 * soportada y un fallo interno del proveedor criptográfico. La API nunca
 * expone detalles del ciphertext ni de la Master Key.</p>
 */
public class EncryptionException extends RuntimeException {

  public enum Reason {
    AUTHENTICATION_FAILED,
    UNSUPPORTED_VERSION,
    INVALID_INPUT,
    INTERNAL_FAILURE
  }

  private final Reason reason;

  public EncryptionException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public EncryptionException(Reason reason, String message, Throwable cause) {
    super(message, cause);
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }
}
