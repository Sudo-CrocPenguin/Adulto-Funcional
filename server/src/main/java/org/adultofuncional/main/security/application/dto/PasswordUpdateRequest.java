package org.adultofuncional.main.security.application.dto;

import java.time.LocalDate;

import org.adultofuncional.main.shared.security.NoHtml;
import org.adultofuncional.main.shared.validation.InputPatterns;
import org.adultofuncional.main.shared.validation.Utf8ByteLength;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para modificar
 * parcialmente una credencial existente (comportamiento PATCH).
 *
 * <p>
 * Todos los campos son opcionales. Los campos no incluidos (nulos o vacíos)
 * conservan su valor actual. Si se envía una nueva contraseña, el caso de uso
 * la cifrará y actualizará los parámetros criptográficos.
 *
 * <p>
 * <strong>Validaciones aplicadas:</strong>
 * <ul>
 * <li>{@code applicationName} — opcional, máximo 35 caracteres, sin HTML.</li>
 * <li>{@code password} — opcional; si se envía, debe tener al menos 1
 * carácter.</li>
 * <li>{@code lastChangeDate} — opcional.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code applicationName} está anotado con {@link NoHtml}.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class PasswordUpdateRequest {

  /**
   * Nuevo nombre de la aplicación (opcional).
   * Máximo 35 caracteres, sin HTML.
   */
  @Size(max = 35, message = "El nombre no puede exceder 35 caracteres")
  @Pattern(regexp = InputPatterns.NON_BLANK, message = "El nombre no puede estar vacío")
  @NoHtml
  private String applicationName;

  /**
   * Nueva contraseña en texto plano (opcional).
   * Si se envía, será cifrada con AES‑256 antes de persistir.
   *
   * <p>
   * <strong>Importante:</strong> este valor nunca debe loguearse ni
   * almacenarse sin cifrar.
  */
  @Pattern(regexp = InputPatterns.NON_BLANK, message = "La contraseña no puede estar vacía")
  @Utf8ByteLength(
      max = 2032,
      message = "La contraseña no puede exceder 2032 bytes en UTF-8")
  private String password;

  /**
   * Nueva fecha de último cambio (opcional).
   * Formato esperado: {@code YYYY-MM-DD}.
   */
  private LocalDate lastChangeDate;
}
