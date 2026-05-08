package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.adultofuncional.main.shared.security.NoHtml;

/**
 * DTO para actualizar una contraseña existente (PATCH).
 *
 * <p><strong>¿Qué es?</strong><br>
 * Objeto que encapsula los campos modificables de una contraseña almacenada.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Permite actualizar el nombre de la aplicación, la contraseña (texto plano),
 * la categoría o la fecha de último cambio.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Se recibe en el cuerpo de la petición PATCH a {@code /api/security/passwords/{id}}.
 * Todos los campos son opcionales, permitiendo actualizaciones parciales.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase
 */
@Getter
@Builder
public class PasswordUpdateRequest {

    @Size(max = 35, message = "El nombre no puede exceder 35 caracteres")
    @NoHtml
    private String applicationName;

    @Size(min = 1, message = "La contraseña no puede estar vacía")
    private String password;

    @Size(max = 50, message = "La categoría es demasiado larga")
    @NoHtml
    private String category;

    private java.time.LocalDate lastChangeDate;
}
