package org.adultofuncional.main.account.application.dto;

import org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase;
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
 * El ID de la cuenta NO está incluido aquí — se recibe por separado
 * como {@code @PathVariable} en el controlador.
 *
 * <p>
 * Validaciones aplicadas:
 * <ul>
 * <li>{@code names} — No vacío, máximo 50 caracteres</li>
 * <li>{@code lastnames} — No vacío, máximo 50 caracteres</li>
 * <li>{@code phone} — No vacío, máximo 20 caracteres</li>
 * <li>{@code email} — No vacío, formato válido, máximo 255 caracteres</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see UpdateAccountUseCase
 */
@Getter
@Builder
public class UpdateAccountRequest {

  // TODO: añadir validacion de nombres
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  private final String names;

  // TODO: añadir validacion de apellidos
  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
  private final String lastnames;

  // TODO: Agregar @Pattern para validar formato internacional (ej. +573001234567)
  @NotBlank(message = "El teléfono es obligatorio")
  @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
  private final String phone;

  // TODO: Agregar validación de dominios permitidos según políticas del sistema
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Debe ser un email válido")
  @Size(max = 255, message = "El email no puede exceder 255 caracteres")
  private final String email;
}
