package org.adultofuncional.main.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) que representa la solicitud de actualización de una cuenta.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula los datos que el cliente (frontend) envía al servidor
 * para modificar la información de una cuenta existente.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para recibir y validar solo los campos que el usuario final puede editar,
 * manteniendo la seguridad y evitando la modificación de atributos sensibles
 * (como contraseña o clave maestra) a través de este endpoint.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * El controlador REST recibe un objeto JSON en el cuerpo de la petición HTTP (PUT/PATCH),
 * lo deserializa a {@code UpdateAccountRequest} y aplica las validaciones de Bean Validation
 * ({@code @NotBlank}, {@code @Email}, {@code @Size}) automáticamente.
 * Si pasa las validaciones, se pasa al caso de uso {@code UpdateAccountUseCase} para procesar.
 *
 * <p><strong>Validaciones aplicadas:</strong>
 * <ul>
 *   <li>{@code names} - No vacío, máximo 50 caracteres</li>
 *   <li>{@code lastnames} - No vacío, máximo 50 caracteres</li>
 *   <li>{@code phone} - No vacío, máximo 20 caracteres</li>
 *   <li>{@code email} - No vacío, formato válido, máximo 255 caracteres</li>
 * </ul>
 *
 * <p><strong>Nota de diseño:</strong> El ID de la cuenta NO está incluido en este DTO,
 * se recibe por separado como variable de ruta {@code @PathVariable} en el controlador.
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
     * Nombres del titular.
     * Obligatorio. Mínimo 1 carácter, máximo 50.
     *
     * @see jakarta.validation.constraints.NotBlank
     * @see jakarta.validation.constraints.Size
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String names;

    /**
     * Apellidos del titular.
     * Obligatorio. Mínimo 1 carácter, máximo 50.
     *
     * //TODO: Considerar agregar validación de caracteres especiales (ej. evitar números o símbolos no permitidos)
     */
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 50, message = "Los apellidos no pueden exceder 50 caracteres")
    private String lastnames;

    /**
     * Número de teléfono de contacto.
     * Obligatorio. Máximo 20 caracteres.
     * Se aceptan formato internacional (ej. +573001234567).
     *
     * //TODO: Agregar patrón @Pattern para validar formato internacional estándar
     */
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String phone;

    /**
     * Correo electrónico del usuario.
     * Obligatorio, único en el sistema. Se valida formato y longitud.
     *
     * //TODO: Agregar validación de dominios permitidos (según políticas del sistema)
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;
}