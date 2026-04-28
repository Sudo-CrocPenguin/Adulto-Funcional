package org.adultofuncional.main.shared.exception;

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

        super(message, 409);
    }
}
