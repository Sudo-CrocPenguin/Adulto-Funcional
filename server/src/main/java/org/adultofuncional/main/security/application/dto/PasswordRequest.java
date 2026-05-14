package org.adultofuncional.main.security.application.dto;

import java.time.LocalDate;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para guardar o actualizar
 * una credencial en el gestor de contraseñas.
 *
 * <p>
 * El campo {@code password} contiene la contraseña en texto plano tal como la
 * ingresa el usuario. La encriptación AES‑256 (generación de salt, IV y
 * ciphertext) se realiza en la capa de aplicación usando la Master Key
 * verificada en sesión. Este DTO **nunca** almacena ni expone material
 * criptográfico.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code applicationName} — obligatorio, máximo 35 caracteres, sin
 * HTML.</li>
 * <li>{@code password} — obligatorio, mínimo 1 carácter.</li>
 * <li>{@code lastChangeDate} — opcional; si no se envía, el caso de uso
 * asigna la fecha actual.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code applicationName} está anotado con {@link NoHtml}, que
 * utiliza Jsoup con {@code Safelist.none()} para rechazar cualquier contenido
 * HTML y prevenir Stored XSS.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.CreatePasswordUseCase
 * @see org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class PasswordRequest {

  /**
   * Nombre del servicio o aplicación.
   * Obligatorio, máximo 35 caracteres, sin HTML.
   * Ejemplos: "Gmail", "Netflix", "GitHub".
   */
  @NotBlank(message = "El nombre de la aplicación es obligatorio")
  @Size(max = 35, message = "El nombre no puede exceder 35 caracteres")
  @NoHtml
  private String applicationName;

  /**
   * Contraseña en texto plano tal como la ingresa el usuario.
   * Obligatorio. Será cifrada con AES‑256 antes de persistir.
   *
   * <p>
   * <strong>Importante:</strong> este valor nunca debe loguearse ni
   * almacenarse sin cifrar. Solo se utiliza temporalmente durante la
   * creación o actualización.
   */
  @NotBlank(message = "La contraseña es obligatoria")
  private String password;

  /**
   * Fecha del último cambio de la contraseña (opcional).
   * Si no se proporciona, se asigna la fecha actual en el caso de uso.
   *
   * <p>
   * Formato esperado: {@code YYYY-MM-DD} (ISO‑8601 sin hora).
   */
  private LocalDate lastChangeDate;
}
