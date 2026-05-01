package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) que representa la solicitud para guardar o actualizar
 * una contraseña en el gestor de contraseñas.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto que encapsula los datos que el cliente (frontend) envía al servidor
 * para almacenar una nueva contraseña de un servicio/aplicación, o para actualizar
 * una existente en el gestor de contraseñas.
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para recibir y validar la información de una credencial (nombre del servicio,
 * contraseña en texto plano, fecha de último cambio) antes de que sea procesada
 * por los casos de uso correspondientes ({@code SavePasswordUseCase} o
 * {@code UpdatePasswordUseCase}). Los datos serán encriptados con AES-256.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * El controlador {@code SecurityController} recibe un objeto JSON en el cuerpo de la
 * petición (POST a {@code /api/security/passwords}), lo deserializa a
 * {@code PasswordRequest} y aplica las validaciones de Bean Validation automáticamente.
 * Si pasa las validaciones, se pasa al caso de uso que se encargará de encriptar
 * la contraseña (usando la Master Key del usuario) y almacenarla en la base de datos.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code applicationName} - Nombre del servicio o aplicación (obligatorio, máx 35 caracteres)</li>
 *   <li>{@code password} - Contraseña en texto plano (obligatorio, será encriptada con AES-256)</li>
 *   <li>{@code lastChangeDate} - Fecha del último cambio de esta contraseña (opcional)</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>La contraseña viaja en texto plano dentro del cuerpo JSON, pero se asume que
 *       la comunicación es por HTTPS.</li>
 *   <li>Nunca se almacena en texto plano: se encripta con AES-256 antes de persistir.</li>
 *   <li>La encriptación/desencriptación requiere la Master Key del usuario.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.security.application.usecase.SavePasswordUseCase
 * @see org.adultofuncional.main.security.application.usecase.UpdatePasswordUseCase
 * @see PasswordResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {

    /**
     * Nombre del servicio o aplicación a la que pertenece la contraseña.
     * Obligatorio. Mínimo 1 carácter, máximo 35.
     * Ejemplos: "Gmail", "Netflix", "GitHub", "Banco XYZ".
     *
     * //TODO: Considerar agregar validación para evitar nombres duplicados por cuenta
     */
    @NotBlank(message = "El nombre de la aplicación es obligatorio")
    @Size(max = 35, message = "El nombre de la aplicación no puede exceder 35 caracteres")
    private String applicationName;

    /**
     * Contraseña del servicio/aplicación en texto plano.
     * Obligatoria. Mínimo 1 carácter (aunque se recomienda mayor longitud).
     * Será encriptada con AES-256 usando la Master Key del usuario antes de almacenarse.
     *
     * <p><strong>Flujo de encriptación:</strong>
     * <ol>
     *   <li>El usuario proporciona su Master Key (verificada previamente en sesión)</li>
     *   <li>Se deriva una clave de encriptación a partir de la Master Key</li>
     *   <li>La contraseña se encripta con AES-256 en modo GCM o CBC</li>
     *   <li>El resultado se codifica en Base64 y se almacena en {@code password_application}</li>
     * </ol>
     *
     * <p><strong>⚠️ IMPORTANTE:</strong>
     * Este campo NUNCA debe ser logueado ni almacenado en texto plano.
     * Solo se usa temporalmente para la encriptación.
     *
     * //TODO: Agregar validación de fortaleza de contraseña (recomendaciones de seguridad)
     * //TODO: Agregar validación para evitar contraseñas comunes o débiles
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 1, message = "La contraseña no puede estar vacía")
    private String password;

    /**
     * Fecha del último cambio de esta contraseña.
     * Opcional. Si no se proporciona, se usará la fecha actual.
     * Útil para recordar al usuario cuándo fue la última vez que cambió
     * esta contraseña específica.
     *
     * <p><strong>Formato esperado:</strong>
     * ISO-8601 sin hora: {@code YYYY-MM-DD} (ej. 2025-05-01)
     *
     * //TODO: Agregar validación para que la fecha no sea futura
     */
    private LocalDate lastChangeDate;
}