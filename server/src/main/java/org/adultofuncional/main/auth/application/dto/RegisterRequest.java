package org.adultofuncional.main.auth.application.dto;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la solicitud de registro de un nuevo usuario.
 *
 * <p>
 * Encapsula los datos personales necesarios para crear una cuenta.
 * Las validaciones se aplican automáticamente mediante Bean Validation.
 * El email debe ser único en el sistema.
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Los campos visibles (nombres, apellidos, teléfono y email) están anotados
 * con {@link NoHtml} para rechazar cualquier intento de incluir HTML/scripts.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  /**
   * Nombres del titular de la cuenta.
   * Obligatorio, máximo 50 caracteres y compatible con nombres Unicode.
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  @Pattern(
      regexp = org.adultofuncional.main.shared.validation.InputPatterns.PERSON_NAME,
      message = "El nombre solo admite letras, espacios, apóstrofes y guiones")
  @NoHtml
  private String names;

  /**
   * Apellidos del titular de la cuenta.
   * Obligatorio, máximo 50 caracteres.
   */
  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
  @Pattern(
      regexp = org.adultofuncional.main.shared.validation.InputPatterns.PERSON_NAME,
      message = "Los apellidos solo admiten letras, espacios, apóstrofes y guiones")
  @NoHtml
  private String lastnames;

  /**
   * Número de teléfono de contacto.
   * Obligatorio y expresado en formato internacional E.164.
   */
  @NotBlank(message = "El teléfono es obligatorio")
  @Pattern(
      regexp = org.adultofuncional.main.shared.validation.InputPatterns.E164_PHONE,
      message = "El teléfono debe usar formato E.164, por ejemplo +573001234567")
  @NoHtml
  private String phone;

  /**
   * Correo electrónico del usuario (también usado como username).
   * Obligatorio, único en el sistema, con formato de email válido
   * y máximo 255 caracteres. Se aceptan todos los dominios válidos porque no
   * existe una política corporativa que justifique restringirlos.
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El formato del email no es válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  @NoHtml
  private String email;

  /**
   * Contraseña en texto plano.
   * Obligatoria, entre 15 y 128 caracteres.
   * Se almacenará como hash Argon2. No se aplica {@code @NoHtml} porque
   * las contraseñas pueden contener caracteres como {@code <} o {@code >}
   * y no se renderizan en el frontend.
   *
   * <p>
   * <strong>⚠️ Seguridad:</strong> nunca se debe loguear, almacenar en claro
   * ni exponer en respuestas.
   */
  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 15, max = 128, message = "La contraseña debe tener entre 15 y 128 caracteres")
  private String password;

  /**
   * Clave maestra opcional para el gestor de contraseñas.
   * Si se proporciona, debe tener entre 15 y 128 caracteres.
   * Se almacenará como hash Argon2. Sin {@code @NoHtml} por las mismas
   * razones que la contraseña.
   */
  @Size(min = 15, max = 128, message = "La clave maestra debe tener entre 15 y 128 caracteres")
  private String masterKey;
}
