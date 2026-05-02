package org.adultofuncional.main.auth.application.dto;

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
 * mediante Bean Validation.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Correo electrónico del usuario.
     * Obligatorio, debe tener formato válido y máximo 255 caracteres.
     * Se usa como username para la autenticación con Spring Security.
     *
     * //TODO: Considerar agregar validación de dominios permitidos (ej. solo correos corporativos)
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    /**
     * Contraseña del usuario en texto plano.
     * Obligatoria, mínimo 8 caracteres por seguridad.
     * Se comparará con el hash almacenado en {@code account_password} usando Argon2.
     *
     * <p><strong>⚠️ IMPORTANTE:</strong>
     * Este campo NUNCA debe ser logueado ni almacenado. Solo se usa para
     * verificar la contraseña durante el proceso de autenticación.
     *
     * //TODO: Agregar validación de fortaleza de contraseña (mayúsculas, números, símbolos)
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
    private String password;
}