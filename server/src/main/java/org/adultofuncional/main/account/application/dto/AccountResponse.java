package org.adultofuncional.main.account.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code AccountResponse} es un DTO (Data Transfer Object) que encapsula la información
 * de una cuenta de usuario que se envía al cliente (frontend) como respuesta a una
 * operación exitosa (consulta o actualización).
 *
 * <p>Este objeto excluye deliberadamente campos sensibles como la contraseña ({@code account_password})
 * y la clave maestra ({@code account_master_key}) por razones de seguridad.
 *
 * <p><strong>Flujo típico:</strong>
 * <ol>
 *   <li>Un caso de uso (ej. {@code GetAccountUseCase}) recupera una entidad {@code AccountEntity}.</li>
 *   <li>Los datos relevantes se mapean a una instancia de {@code AccountResponse}.</li>
 *   <li>El controlador REST devuelve este objeto al cliente (normalmente en formato JSON).</li>
 * </ol>
 *
 * <p><strong>Campos incluidos:</strong>
 * <ul>
 *   <li>{@code id} - Identificador único de la cuenta (UUID v7).</li>
 *   <li>{@code names} - Nombres del titular.</li>
 *   <li>{@code lastnames} - Apellidos del titular.</li>
 *   <li>{@code email} - Correo electrónico (único).</li>
 *   <li>{@code phone} - Número de teléfono.</li>
 *   <li>{@code createdAt} - Fecha y hora de creación de la cuenta.</li>
 * </ul>
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
     * Identificador único de la cuenta (UUID v7).
     * Corresponde al campo {@code account_id} de la entidad.
     */
    private UUID id;

    /**
     * Nombres del titular de la cuenta.
     * Corresponde a {@code account_names} en la entidad y base de datos.
     */
    private String names;

    /**
     * Apellidos del titular de la cuenta.
     * Corresponde a {@code account_lastnames} en la entidad y base de datos.
     */
    private String lastnames;

    /**
     * Correo electrónico del usuario. Es único y se usa como username para autenticación.
     * Corresponde a {@code account_email}.
     */
    private String email;

    /**
     * Número de teléfono de contacto.
     * Corresponde a {@code account_phone}.
     */
    private String phone;

    /**
     * Fecha y hora en que la cuenta fue creada.
     * Corresponde a {@code account_created_at} y se genera automáticamente al persistir.
     */
    private LocalDateTime createdAt;
}