package org.adultofuncional.main.shared.response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Serializa el contrato de error desde filtros y componentes de seguridad.
 *
 * <p>Estas capas se ejecutan antes de Spring MVC y no pueden delegar sus
 * respuestas al {@code RestControllerAdvice}. El writer reutiliza la misma
 * fábrica para conservar estructura, códigos y trazabilidad.</p>
 */
@Component
@RequiredArgsConstructor
public class ApiErrorResponseWriter {

  private final ObjectMapper objectMapper;
  private final ApiErrorFactory errorFactory;

  /**
   * Escribe un error JSON completo sobre la respuesta HTTP.
   */
  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      int status,
      ApiErrorCode code,
      String message) throws IOException {
    ApiResponse<Void> body = errorFactory.create(request, status, code, message);

    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader(TraceIdProvider.TRACE_ID_HEADER, body.getTraceId());
    objectMapper.writeValue(response.getWriter(), body);
  }
}
