package org.adultofuncional.main.account.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de una cuenta de usuario.
 *
 * <p><strong>¿Qué es?</strong><br>
 * Es un objeto de transferencia de datos que se utiliza para enviar información
 * de una cuenta desde el servidor hacia el cliente (frontend).
 *
 * <p><strong>¿Para qué sirve?</strong><br>
 * Sirve para encapsular únicamente los datos no sensibles de una cuenta que el
 * frontend necesita mostrar (consulta) o confirmar (post-actualización).
 * Excluye campos como contraseñas, claves maestras o relaciones internas.
 *
 * <p><strong>¿Cómo funciona?</strong><br>
 * Un caso de uso (ej. {@code GetAccountUseCase}) recupera una entidad
 * {@code AccountEntity} del repositorio, la mapea a este DTO y la retorna al controlador.
 * El controlador la serializa a JSON y la envía como respuesta HTTP.
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code id} - Identificador UUID v7 de la cuenta</li>
 *   <li>{@code names} - Nombres del usuario</li>
 *   <li>{@code lastnames} - Apellidos del usuario</li>
 *   <li>{@code email} - Correo electrónico (único en el sistema)</li>
 *   <li>{@code phone} - Número de teléfono de contacto</li>
 *   <li>{@code createdAt} - Fecha y hora de creación de la cuenta</li>
 * </ul>
 *
 * <p><strong>Seguridad:</strong> Este DTO nunca expone {@code password} ni {@code masterKey}.
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 * @see org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity
 * @see GetAccountUseCase
 * @see UpdateAccountUseCase
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    /**
     * Identificador único de la cuenta.
     * Tipo UUID v7 generado automáticamente por la base de datos.
     * Corresponde al campo {@code account_id} de la entidad y la tabla {@code accounts}.
     */
    private UUID id;

    /**
     * Nombres del titular de la cuenta.
     * Obligatorio, máximo 50 caracteres.
     * Corresponde a {@code account_names} en la entidad y base de datos.
     */
    private String names;

    /**
     * Apellidos del titular de la cuenta.
     * Obligatorio, máximo 50 caracteres.
     * Corresponde a {@code account_lastnames} en la entidad y base de datos.
     */
    private String lastnames;

    /**
     * Correo electrónico del usuario.
     * Obligatorio, único en todo el sistema. Se usa como username para autenticación JWT.
     * Corresponde a {@code account_email}.
     */
    private String email;

    /**
     * Número de teléfono de contacto.
     * Obligatorio, máximo 20 caracteres. Puede incluir código de país.
     * Corresponde a {@code account_phone}.
     */
    private String phone;

    /**
     * Fecha y hora en que la cuenta fue creada.
     * Se genera automáticamente al persistir la entidad (auditoría).
     * Corresponde a {@code account_created_at}.
     */
    private LocalDateTime createdAt;
}