package org.adultofuncional.main.security.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta del gestor de contraseñas.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula la información de una contraseña almacenada que se
 * envía al cliente (frontend) como respuesta a una operación de consulta.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para devolver al usuario la información de sus contraseñas almacenadas
 * en el gestor, incluyendo el nombre del servicio y la contraseña desencriptada
 * (solo cuando el usuario ha verificado su Master Key).
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Un caso de uso (ej. {@code GetPasswordUseCase} o {@code ListPasswordsUseCase})
 * recupera una entidad {@code PasswordEntity} del repositorio, la desencripta
 * (usando la Master Key previamente verificada), la mapea a este DTO y la retorna
 * al controlador. El controlador la serializa a JSON y la envía como respuesta HTTP.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code id} - Identificador UUID de la contraseña</li>
 *   <li>{@code applicationName} - Nombre del servicio/aplicación</li>
 *   <li>{@code password} - Contraseña desencriptada (texto plano)</li>
 *   <li>{@code lastChangeDate} - Fecha del último cambio</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>La contraseña se devuelve en texto plano SOLO después de verificar la Master Key</li>
 *   <li>Si la Master Key no ha sido verificada, se debe lanzar {@code ForbiddenException}</li>
 *   <li>Nunca se devuelven campos sensibles de la cuenta propietaria</li>
 * </ul>
 *
 * <p><strong>⚠️ PRECAUCIÓN:</strong>
 * Este DTO contiene la contraseña en texto plano. Debe transmitirse exclusivamente
 * por HTTPS y no debe ser almacenada en logs ni cachés del servidor.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.GetPasswordUseCase
 * @see org.adultofuncional.main.security.application.usecase.ListPasswordsUseCase
 * @see PasswordRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResponse {

    /**
     * Identificador único de la contraseña almacenada (UUID v7).
     * Corresponde al campo {@code password_id} de la entidad y la tabla {@code passwords}.
     * Útil para operaciones específicas como actualizar o eliminar una contraseña.
     */
    private UUID id;

    /**
     * Nombre del servicio o aplicación a la que pertenece la contraseña.
     * Ejemplos: "Gmail", "Netflix", "GitHub", "Banco XYZ".
     * Corresponde a {@code password_application_name} en la entidad.
     */
    private String applicationName;

    /**
     * Contraseña del servicio/aplicación en texto plano (desencriptada).
     * Corresponde al valor original que el usuario guardó, después de ser
     * desencriptada con AES-256 usando la Master Key del usuario.
     *
     * <p><strong>⚠️ IMPORTANTE - SEGURIDAD:</strong>
     * <ul>
     *   <li>Este campo SOLO debe ser devuelto si el usuario ha verificado su Master Key</li>
     *   <li>La verificación debe ocurrir antes de llamar al caso de uso</li>
     *   <li>Si no se ha verificado la Master Key, el controlador debe retornar HTTP 403</li>
     *   <li>Este campo nunca debe ser logueado por el servidor</li>
     * </ul>
     *
     * 
     */

    //TODO: Agregar campo para indicar si la contraseña necesita ser rotada (antigüedad > 90 días)
    
    private String password;

    /**
     * Fecha del último cambio de esta contraseña.
     * Corresponde a {@code password_last_change_date} en la entidad.
     * Si es {@code null}, significa que nunca se ha registrado una fecha de cambio.
     *
     * <p><strong>Utilidad:</strong>
     * El frontend puede usar este campo para mostrar una alerta al usuario
     * si la contraseña tiene más de 90 días sin cambios (recomendación de seguridad).
     */
    private LocalDate lastChangeDate;
}