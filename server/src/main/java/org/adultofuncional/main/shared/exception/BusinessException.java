package org.adultofuncional.main.shared.exception;

/**
 * Excepción base para representar errores de lógica de negocio de la aplicación.
 *
 * <p>Todas las excepciones personalizadas del sistema extienden de esta clase,
 * lo que permite manejarlas de forma centralizada en el {@code GlobalExceptionHandler}.
 * Cada excepción lleva consigo un código de estado HTTP que describe el tipo
 * de error ocurrido.</p>
 *
 * <p>Extiende {@link RuntimeException} para que no sea obligatorio capturarla
 * con try-catch, permitiendo que se propague hasta el manejador global.</p>
 */

public class BusinessException extends RuntimeException {

    /** Código de estado HTTP asociado al error de negocio. */
    private final int status;

    /**
     * Construye una excepción de negocio con un mensaje y un código de estado
     * HTTP personalizado.
     *
     * @param message mensaje descriptivo que explica el error ocurrido
     * @param status  código de estado HTTP que representa el tipo de error
     */

    public BusinessException(String message, int status) {

        super(message);
        this.status = status;

    }

     /**
     * Construye una excepción de negocio con un mensaje y un código de estado
     * HTTP por defecto de 400 (Bad Request).
     *
     * <p>Se usa cuando el error es una solicitud inválida del cliente pero no
     * encaja en ninguna de las subclases más específicas.</p>
     *
     * @param message mensaje descriptivo que explica el error ocurrido
     */

    public BusinessException(String message) {
        this(message, 400);
    }

    /**
     * Retorna el código de estado HTTP asociado a esta excepción.
     *
     * <p>Es usado por el {@code GlobalExceptionHandler} para construir la
     * respuesta HTTP con el código correcto.</p>
     *
     * @return código de estado HTTP del error
     */
    
    public int getStatus() {
        return status;
    }
    
}