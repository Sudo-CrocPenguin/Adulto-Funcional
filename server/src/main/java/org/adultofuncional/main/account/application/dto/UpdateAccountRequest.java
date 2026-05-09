package org.adultofuncional.main.account.application.dto;

import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para modificar
 * la información de una cuenta existente.
 *
 * <p>
 * El ID de la cuenta NO se incluye aquí — se recibe por separado
 * como {@code @PathVariable} en el controlador.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code names} y {@code lastnames} — obligatorios, máximo 50
 * caracteres.</li>
 * <li>{@code phone} — obligatorio, máximo 20 caracteres.</li>
 * <li>{@code email} — obligatorio, formato válido, máximo 255 caracteres.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * Todos los campos de texto están anotados con {@link NoHtml}.
 * Esto asegura que cualquier petición que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazada con un error 400, evitando
 * el almacenamiento de scripts maliciosos en la base de datos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()},
 * es decir, no se permite ningún tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes, Juan Sebastian Rios
 * @since 0.0.1
 * @see UpdateAccountUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class UpdateAccountRequest {

  /**
   * Nombres del titular.
   * Obligatorio, máximo 50 caracteres, sin HTML.
   *
   * TODO: Añadir validación de formato (solo letras, espacios, guiones)
   * mediante {@code @Pattern}.
   */
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  @NoHtml
  private final String names;

  /**
   * Apellidos del titular.
   * Obligatorio, máximo 50 caracteres, sin HTML.
   *
   * TODO: Añadir validación de formato (solo letras, espacios, guiones)
   * mediante {@code @Pattern}.
   */
  @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
  @NoHtml
  private final String lastnames;

  /**
   * Número de teléfono de contacto.
   * Obligatorio, máximo 20 caracteres, sin HTML.
   *
   * TODO: Agregar {@code @Pattern} para validar formato internacional
   * (ej. +573001234567).
   */
  @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
  @NoHtml
  private final String phone;

  /**
   * Correo electrónico del usuario (usado también como username).
   * Obligatorio, debe ser un email válido, máximo 255 caracteres, sin HTML.
   *
   * TODO: Agregar validación de dominios permitidos según políticas del sistema
   * (ej. solo ciertos dominios corporativos).
   */
  @Email(message = "Debe ser un email válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  @NoHtml
  private final String email;
}
