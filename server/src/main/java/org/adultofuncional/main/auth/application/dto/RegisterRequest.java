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
   * Obligatorio, máximo 50 caracteres.
   *
   * // TODO: Agregar validación de formato (solo letras, espacios, guiones)
   * mediante {@code @Pattern}.
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  @NoHtml
  private String names;

  /**
   * Apellidos del titular de la cuenta.
   * Obligatorio, máximo 50 caracteres.
   */
  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
  @NoHtml
  private String lastnames;

  /**
   * Número de teléfono de contacto.
   * Obligatorio, máximo 20 caracteres.
   * Actualmente se permite cualquier formato; en el futuro se validará
   * con un patrón internacional.
   *
   * // TODO: Agregar {@code @Pattern} para formato internacional (ej.
   * +573001234567).
   */
  @NotBlank(message = "El teléfono es obligatorio")
  @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
  @NoHtml
  private String phone;

  /**
   * Correo electrónico del usuario (también usado como username).
   * Obligatorio, único en el sistema, con formato de email válido
   * y máximo 255 caracteres.
   *
   * // TODO: Agregar validación de dominios permitidos según políticas de
   * negocio.
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El formato del email no es válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  @NoHtml
  private String email;

  /**
   * Contraseña en texto plano.
   * Obligatoria, entre 8 y 24 caracteres.
   * Se almacenará como hash Argon2. No se aplica {@code @NoHtml} porque
   * las contraseñas pueden contener caracteres como {@code <} o {@code >}
   * y no se renderizan en el frontend.
   *
   * <p>
   * <strong>⚠️ Seguridad:</strong> nunca se debe loguear, almacenar en claro
   * ni exponer en respuestas.
   */
  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 8, max = 24, message = "La contraseña debe tener entre 8 y 24 caracteres")
  private String password;

  /**
   * Clave maestra opcional para el gestor de contraseñas.
   * Si se proporciona, debe tener entre 12 y 24 caracteres.
   * Se almacenará como hash Argon2. Sin {@code @NoHtml} por las mismas
   * razones que la contraseña.
   *
   * // TODO: Agregar validación de fortaleza cuando el gestor esté implementado.
   */
  @Size(min = 12, max = 24, message = "La clave maestra debe tener entre 12 y 24 caracteres")
  private String masterKey;
}
