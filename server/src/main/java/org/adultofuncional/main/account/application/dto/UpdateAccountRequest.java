package org.adultofuncional.main.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code UpdateAccountRequest} es un DTO que representa los datos enviados por el cliente
 * (frontend) para actualizar la información de una cuenta existente.
 *
 * <p><strong>Propósito:</strong>
 * Recibir solo los campos que el usuario final puede modificar, manteniendo la seguridad
 * y evitando la exposición de atributos sensibles (como contraseña o master key).
 *
 * <p><strong>Flujo típico:</strong>
 * <ol>
 *   <li>El cliente envía una petición HTTP PUT/PATCH con un cuerpo JSON que se deserializa en este objeto.</li>
 *   <li>El controlador valida las anotaciones (ej. {@code @NotBlank}, {@code @Email}).</li>
 *   <li>El objeto se pasa al caso de uso {@link UpdateAccountUseCase} para procesar la actualización.</li>
 * </ol>
 *
 * <p><strong>Validaciones incluidas:</strong>
 * <ul>
 *   <li>{@code names} - No puede estar vacío y máximo 50 caracteres.</li>
 *   <li>{@code lastnames} - No puede estar vacío y máximo 50 caracteres.</li>
 *   <li>{@code phone} - No puede estar vacío y máximo 20 caracteres.</li>
 *   <li>{@code email} - Debe ser un email válido, no vacío, y máximo 255 caracteres.</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> El identificador de la cuenta se recibe por separado (normalmente en la URL)
 * y no forma parte de este objeto.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see UpdateAccountUseCase
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    /**
     * Nuevo nombre del titular (obligatorio).
     * Longitud máxima: 50 caracteres.
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String names;

    /**
     * Nuevos apellidos del titular (obligatorio).
     * Longitud máxima: 50 caracteres.
     */
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
    private String lastnames;

    /**
     * Nuevo número de teléfono (obligatorio).
     * Longitud máxima: 20 caracteres.
     */
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String phone;

    /**
     * Nuevo correo electrónico (obligatorio y debe ser único en el sistema).
     * Se valida el formato de email y la longitud máxima de 255 caracteres.
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;
}