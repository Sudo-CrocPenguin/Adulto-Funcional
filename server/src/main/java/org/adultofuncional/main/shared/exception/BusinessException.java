package org.adultofuncional.main.shared.exception;

import org.adultofuncional.main.shared.response.ApiErrorCode;

/**
 * Excepción base para representar errores de lógica de negocio de la aplicación.
 *
 * <p>Todas las excepciones personalizadas del sistema extienden de esta clase,
 * lo que permite manejarlas de forma centralizada en el {@code GlobalExceptionHandler}.
 * Cada excepción lleva consigo un estado HTTP y un código público estable.</p>
 *
 * <p>Extiende {@link RuntimeException} para que no sea obligatorio capturarla
 * con try-catch, permitiendo que se propague hasta el manejador global.</p>
 */

public class BusinessException extends RuntimeException {

    /** Código de estado HTTP asociado al error de negocio. */
    private final int status;

    /** Código estable consumido por los clientes de la API. */
    private final ApiErrorCode code;

    /**
     * Construye una excepción de negocio con un mensaje y un código de estado
     * HTTP personalizado.
     *
     * @param message mensaje descriptivo que explica el error ocurrido
     * @param status  código de estado HTTP que representa el tipo de error
     */

    public BusinessException(String message, int status) {
        this(message, status, defaultCodeFor(status));
    }

    /**
     * Construye una excepción con estado y código público explícitos.
     *
     * @param message mensaje seguro para el cliente
     * @param status  estado HTTP numérico
     * @param code    código estable del contrato de API
     */
    public BusinessException(String message, int status, ApiErrorCode code) {
        super(message);
        this.status = status;
        this.code = code;
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

    /**
     * Retorna el código estable asociado al error.
     *
     * @return código público de la API
     */
    public ApiErrorCode getCode() {
        return code;
    }

    private static ApiErrorCode defaultCodeFor(int status) {
        return switch (status) {
            case 401 -> ApiErrorCode.AUTHENTICATION_FAILED;
            case 403 -> ApiErrorCode.ACCESS_DENIED;
            case 404 -> ApiErrorCode.RESOURCE_NOT_FOUND;
            case 409 -> ApiErrorCode.RESOURCE_CONFLICT;
            default -> ApiErrorCode.BUSINESS_RULE_VIOLATION;
        };
    }

}
