package org.adultofuncional.main.shared.exception;

/**
 * Excepción que representa un acceso denegado a un recurso protegido (HTTP 403).
 *
 * <p>Se lanza cuando un usuario autenticado intenta acceder a un recurso para el
 * cual no tiene permisos. En esta aplicación se usa principalmente cuando el usuario
 * intenta acceder al gestor de contraseñas sin haber verificado su contraseña
 * maestra.</p>
 *
 * <p>Extiende {@link BusinessException} con un código de estado fijo de 403.</p>
 */

public class ForbiddenException extends BusinessException {

    /**
     * Construye una nueva excepción de acceso denegado.
     *
     * @param message mensaje descriptivo que explica por qué se denegó el acceso
     */
    
    public ForbiddenException(String message) {
        super (message, 403);
    }
    
}
