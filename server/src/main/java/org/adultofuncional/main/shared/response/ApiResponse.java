package org.adultofuncional.main.shared.response;

/**
 * Clase genérica que representa la estructura estándar de todas las respuestas
 * de la API.
 *
 * <p>Envuelve cualquier respuesta del sistema en un formato uniforme que incluye
 * un código de estado HTTP, un mensaje descriptivo y los datos del resultado.
 * Al ser genérica, puede adaptarse a cualquier tipo de dato que se necesite
 * retornar.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 *   new ApiResponse&lt;&gt;(200, "Usuario encontrado", usuarioDto);
 *   new ApiResponse&lt;&gt;(404, "Usuario no encontrado", null);
 * </pre>
 *
 * @param <T> tipo del dato que se incluye en la respuesta
 */

public class ApiResponse<T> {

    /** Código de estado HTTP de la respuesta (ej. 200, 404, 500). */
    private final int status;

    /** Mensaje descriptivo sobre el resultado de la operación. */
    private final String  message;

     /** Dato principal de la respuesta. Puede ser null si no hay datos que retornar. */
    private final T data;


     /**
     * Construye una nueva respuesta estándar de la API.
     *
     * @param status  código de estado HTTP de la respuesta
     * @param message mensaje descriptivo del resultado
     * @param data    dato principal de la respuesta, o {@code null} si no aplica
     */

    public ApiResponse(int status, String message, T data) {

        this.status = status;
        this.message = message;
        this.data = data;

    }

    /**
     * Retorna el código de estado HTTP de la respuesta.
     *
     * @return código de estado HTTP
     */

    public int  getStatus() {
        return status;
    }

    /**
     * Retorna el mensaje descriptivo del resultado de la operación.
     *
     * @return mensaje de la respuesta
     */

    public  String getMessage() {
        return message;
    }

    /**
     * Retorna el dato principal incluido en la respuesta.
     *
     * @return dato de la respuesta, o {@code null} si no hay datos
     */
    
    public T getData() {
        return data;
    }
}
