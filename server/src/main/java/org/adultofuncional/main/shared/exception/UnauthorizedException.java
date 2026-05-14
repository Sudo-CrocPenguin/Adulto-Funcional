package org.adultofuncional.main.shared.exception;

/**
 * Excepción que representa un acceso no autorizado a la aplicación (HTTP 401).
 *
 * <p>Se lanza cuando un usuario intenta realizar una operación sin estar
 * autenticado, o cuando las credenciales proporcionadas son incorrectas.
 * El caso más común en esta aplicación es cuando el correo o la contraseña
 * no coinciden durante el inicio de sesión.</p>
 *
 * <p>Extiende {@link BusinessException} con un código de estado fijo de 401.</p>
 */

public class UnauthorizedException extends BusinessException {
    
    /**
     * Construye una nueva excepción de acceso no autorizado.
     *
     * @param message mensaje descriptivo que explica por qué no se autorizó el acceso
     */
    
    public UnauthorizedException(String message) {

        super(message, 401);
    }
}
