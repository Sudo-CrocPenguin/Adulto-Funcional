package org.adultofuncional.main.shared.exception;

import org.adultofuncional.main.shared.response.ApiErrorCode;

/**
 * Excepción que representa un conflicto con el estado actual de un recurso (HTTP 409).
 *
 * <p>Se lanza cuando una operación no puede completarse porque entra en conflicto
 * con datos existentes. El caso más común en esta aplicación es cuando un usuario
 * intenta registrarse con un correo electrónico que ya está en uso.</p>
 *
 * <p>Extiende {@link BusinessException} con un código de estado fijo de 409.</p>
 */

public class ConflictException extends BusinessException {

    /**
     * Construye una nueva excepción de conflicto de datos.
     *
     * @param message mensaje descriptivo que explica el conflicto ocurrido
     */

    public ConflictException(String message) {
        this(message, ApiErrorCode.RESOURCE_CONFLICT);
    }

    /**
     * Construye una excepción 409 con un código de conflicto específico.
     *
     * @param message mensaje seguro para el cliente
     * @param code    código estable del conflicto
     */
    public ConflictException(String message, ApiErrorCode code) {
        super(message, 409, code);
    }
}
