package org.adultofuncional.main.auth.application.dto;

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
   * Obligatorio. Mínimo 1 carácter, máximo 50.
   *
   * //TODO: Considerar agregar validación de caracteres especiales (evitar
   * números o símbolos)
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  private String names;

  /**
   * Apellidos del titular de la cuenta.
   * Obligatorio. Mínimo 1 carácter, máximo 50.
   */
  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
  private String lastnames;

  /**
   * Número de teléfono de contacto.
   * Obligatorio. Máximo 20 caracteres.
   * Se acepta formato internacional (ej. +573001234567).
   *
   * //TODO: Agregar patrón @Pattern para validar formato internacional estándar
   */
  @NotBlank(message = "El teléfono es obligatorio")
  @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
  private String phone;

  /**
   * Correo electrónico del usuario.
   * Obligatorio, único en el sistema. Se valida formato y longitud.
   * Será el username para la autenticación JWT.
   *
   * //TODO: Agregar validación de dominios permitidos (según políticas del
   * sistema)
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Debe ser un email válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  private String email;

  /**
   * Contraseña del usuario en texto plano.
   * Obligatoria, mínimo 8 caracteres por seguridad.
   * Se almacenará como hash Argon2 en {@code account_password}.
   *
   * <p>
   * <strong>⚠️ IMPORTANTE:</strong>
   * Este campo NUNCA debe ser logueado ni almacenado en texto plano.
   * Solo se usa para generar el hash durante el registro.
   *
   * //TODO: Agregar validación de fortaleza de contraseña (mayúsculas, números,
   * símbolos)
   */
  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 8, max = 24, message = "La contraseña debe tener entre 8 y 24 caracteres")
  private String password;

  /**
   * Clave maestra del usuario para el gestor de contraseñas.
   * Opcional. Si se proporciona, debe tener mínimo 8 caracteres.
   * Se almacenará como hash Argon2 en {@code account_master_key}.
   *
   * <p>
   * <strong>¿Para qué sirve?</strong><br>
   * La Master Key actúa como un segundo factor de autenticación específico
   * para acceder al módulo de gestor de contraseñas. Si el usuario no la
   * proporciona en el registro, puede configurarla más tarde.
   *
   * //TODO: Agregar validación de fortaleza para Master Key cuando sea
   * obligatoria
   */
  @Size(min = 12, max = 24, message = "La clave maestra debe tener entre 12 y 24 caracteres")
  private String masterKey;
}
