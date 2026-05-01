package org.adultofuncional.main.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa la solicitud de inicio de sesión.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula las credenciales que el cliente (frontend) envía
 * al servidor para autenticar a un usuario en el sistema.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para recibir y validar las credenciales de acceso (email y contraseña)
 * antes de que sean procesadas por el caso de uso {@code LoginUseCase}.
 * Este DTO asegura que los datos lleguen en el formato correcto y no estén vacíos.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * El controlador {@code AuthController} recibe un objeto JSON en el cuerpo de la
 * petición POST a {@code /api/auth/login}, lo deserializa a {@code LoginRequest}
 * y aplica las validaciones de Bean Validation automáticamente.
 * Si pasa las validaciones, se pasa al caso de uso para autenticar al usuario.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code email} - Correo electrónico del usuario (único en el sistema)</li>
 *   <li>{@code password} - Contraseña en texto plano (se enviará de forma segura por HTTPS)</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong>
 * La contraseña viaja en texto plano dentro del cuerpo JSON, pero se asume que
 * la comunicación es por HTTPS. Nunca se almacena ni se loguea en texto plano.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.auth.application.usecase.LoginUseCase
 * @see AuthResponse
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