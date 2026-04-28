package org.adultofuncional.main.shared.exception;

/**
 * Excepción que representa un recurso no encontrado en el sistema (HTTP 404).
 *
 * <p>Se lanza cuando se intenta acceder o manipular un recurso que no existe
 * en la base de datos, como un movimiento, categoría, evento, gasto fijo o
 * contraseña que no corresponde a ningún registro.</p>
 *
 * <p>Extiende {@link BusinessException} con un código de estado fijo de 404.</p>
 */

public class NotFoundException extends  BusinessException {
    
    /**
     * Construye una nueva excepción de recurso no encontrado.
     *
     * @param message mensaje descriptivo que indica qué recurso no fue encontrado
     */
    
    public NotFoundException(String message){
        super(message, 404);
    }
}
