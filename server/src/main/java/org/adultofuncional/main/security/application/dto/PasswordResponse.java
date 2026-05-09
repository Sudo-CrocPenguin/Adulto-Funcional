package org.adultofuncional.main.security.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta que expone los datos no sensibles de una credencial
 * almacenada.
 *
 * <p>
 * Nunca expone material criptográfico ({@code salt}, {@code iv},
 * {@code ciphertext}) ni la contraseña en texto plano. Solo muestra
 * información identificativa y la fecha del último cambio.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase
 * @see org.adultofuncional.main.security.application.usecase.GetPasswordUseCase
 * @see org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase
 */
@Getter
@Builder
public class PasswordResponse {

  /** Identificador único de la credencial (UUID v7). */
  private UUID id;

  /** Nombre de la aplicación o servicio. */
  private String applicationName;

  /** Fecha del último cambio de la contraseña (puede ser {@code null}). */
  private LocalDate lastChangeDate;
}
