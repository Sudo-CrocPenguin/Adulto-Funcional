package org.adultofuncional.main.account.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta que expone únicamente los datos no sensibles de una cuenta.
 * Nunca expone {@code password} ni {@code masterKey}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class AccountResponse {

  /** Identificador UUID v7 de la cuenta. */
  private final UUID id;

  /** Nombres del titular. Corresponde a {@code account_names}. */
  private final String names;

  /** Apellidos del titular. Corresponde a {@code account_lastnames}. */
  private final String lastnames;

  /** Correo electrónico. Usado como username para autenticación JWT. */
  private final String email;

  /** Teléfono de contacto. Corresponde a {@code account_phone}. */
  private final String phone;

  /** Fecha de creación de la cuenta. Corresponde a {@code account_created_at}. */
  private final LocalDateTime createdAt;
}
