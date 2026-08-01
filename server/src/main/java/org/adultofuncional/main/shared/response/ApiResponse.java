package org.adultofuncional.main.shared.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

/**
 * Clase genérica que representa la estructura estándar de todas las respuestas
 * de la API.
 *
 * <p>
 * Envuelve cualquier respuesta del sistema en un formato uniforme. Las
 * respuestas exitosas conservan los campos históricos {@code status},
 * {@code message} y {@code data}. Los errores añaden un código estable, una
 * lista ordenada de errores de campo y un identificador de trazabilidad.
 * </p>
 *
 * <p>
 * Ejemplo de uso:
 * </p>
 *
 * <pre>
 * new ApiResponse&lt;&gt;(200, "Usuario encontrado", usuarioDto);
 * ApiResponse.&lt;Void&gt;builder()
 *     .status(404)
 *     .code("RESOURCE_NOT_FOUND")
 *     .message("Usuario no encontrado")
 *     .fieldErrors(List.of())
 *     .traceId("4c1f5a5e1fc34e0e")
 *     .data(null)
 *     .build();
 * </pre>
 *
 * @param <T> tipo del dato que se incluye en la respuesta
 */
public class ApiResponse<T> {

  /** Código de estado HTTP de la respuesta (ej. 200, 404, 500). */
  private final int status;

  /** Código estable consumible por clientes. Solo aparece en errores. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final String code;

  /** Mensaje descriptivo sobre el resultado de la operación. */
  private final String message;

  /** Errores de validación ordenados. Solo aparece en respuestas de error. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final List<FieldValidationError> fieldErrors;

  /** Identificador correlacionable de la petición. Solo aparece en errores. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final String traceId;

  /**
   * Dato principal de la respuesta. Puede ser null si no hay datos que retornar.
   */
  private final T data;

  /**
   * Construye una nueva respuesta estándar de la API.
   *
   * @param status  código de estado HTTP de la respuesta
   * @param message mensaje descriptivo del resultado
   * @param data    dato principal de la respuesta, o {@code null} si no aplica
   */

  public ApiResponse(int status, String message, T data) {
    this(status, null, message, null, null, data);
  }

  /**
   * Construye una respuesta con todos los campos del contrato.
   *
   * @param status      código HTTP numérico
   * @param code        código estable del error, o {@code null} en éxito
   * @param message     mensaje seguro para una persona
   * @param fieldErrors errores de validación, o {@code null} en éxito
   * @param traceId     identificador de trazabilidad, o {@code null} en éxito
   * @param data        dato de éxito; debe ser {@code null} en errores
   */
  @Builder
  public ApiResponse(
      int status,
      String code,
      String message,
      List<FieldValidationError> fieldErrors,
      String traceId,
      T data) {
    this.status = status;
    this.code = code;
    this.message = message;
    this.fieldErrors = fieldErrors == null ? null : List.copyOf(fieldErrors);
    this.traceId = traceId;
    this.data = data;
  }

  /**
   * Retorna el código de estado HTTP de la respuesta.
   *
   * @return código de estado HTTP
   */

  public int getStatus() {
    return status;
  }

  /**
   * Retorna el código estable del error.
   *
   * @return código del error, o {@code null} en una respuesta exitosa
   */
  public String getCode() {
    return code;
  }

  /**
   * Retorna el mensaje descriptivo del resultado de la operación.
   *
   * @return mensaje de la respuesta
   */

  public String getMessage() {
    return message;
  }

  /**
   * Retorna los errores de validación ordenados.
   *
   * @return errores de campos, o {@code null} en una respuesta exitosa
   */
  public List<FieldValidationError> getFieldErrors() {
    return fieldErrors;
  }

  /**
   * Retorna el identificador de trazabilidad de la petición.
   *
   * @return identificador de trazabilidad, o {@code null} en éxito
   */
  public String getTraceId() {
    return traceId;
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
