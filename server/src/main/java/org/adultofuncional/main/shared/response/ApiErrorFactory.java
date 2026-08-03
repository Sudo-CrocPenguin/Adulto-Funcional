package org.adultofuncional.main.shared.response;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Construye todas las respuestas públicas de error de la API.
 *
 * <p>Centraliza la forma del contrato, garantiza que {@code data} sea
 * {@code null}, ordena los errores de campo de forma determinista e incorpora
 * el mismo identificador de trazabilidad usado por los logs de la petición.</p>
 */
@Component
@RequiredArgsConstructor
public class ApiErrorFactory {

  private static final Comparator<FieldValidationError> FIELD_ERROR_ORDER =
      Comparator.comparing(FieldValidationError::field)
          .thenComparing(FieldValidationError::code)
          .thenComparing(FieldValidationError::message);

  private final TraceIdProvider traceIdProvider;

  /**
   * Construye un error sin fallos de campo.
   */
  public ApiResponse<Void> create(
      HttpServletRequest request,
      int status,
      ApiErrorCode code,
      String message) {
    return create(request, status, code, message, List.of());
  }

  /**
   * Construye un error con una lista ordenada e inmutable de fallos de campo.
   */
  public ApiResponse<Void> create(
      HttpServletRequest request,
      int status,
      ApiErrorCode code,
      String message,
      List<FieldValidationError> fieldErrors) {
    Objects.requireNonNull(request, "request no puede ser null");
    Objects.requireNonNull(code, "code no puede ser null");
    Objects.requireNonNull(message, "message no puede ser null");

    List<FieldValidationError> orderedErrors = fieldErrors == null
        ? List.of()
        : fieldErrors.stream().sorted(FIELD_ERROR_ORDER).toList();

    return ApiResponse.<Void>builder()
        .status(status)
        .code(code.name())
        .message(message)
        .fieldErrors(orderedErrors)
        .traceId(traceIdProvider.getOrCreate(request))
        .data(null)
        .build();
  }
}
