package org.adultofuncional.main.auth.application.dto;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la solicitud de inicio de sesión.
 *
 * <p>
 * Encapsula las credenciales (email y contraseña) que el cliente envía
 * para autenticar a un usuario. Las validaciones se aplican automáticamente
 * mediante Bean Validation (Jakarta).
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Ambos campos están anotados con {@link NoHtml} para rechazar cualquier
 * intento de incluir HTML/scripts en las credenciales.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  /**
   * Correo electrónico del usuario (usado como username).
   * Obligatorio, formato de email válido y máximo 255 caracteres.
   *
   * // TODO: Validar dominios permitidos según políticas de negocio (ej. solo
   * correos corporativos).
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El formato del email no es válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  @NoHtml
  private String email;

  /**
   * Contraseña en texto plano.
   * Obligatoria, entre 8 y 24 caracteres.
   * Será verificada contra el hash Argon2 almacenado.
   *
   */
  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 8, max = 24, message = "La contraseña debe tener entre 8 y 24 caracteres")
  private String password;
}
