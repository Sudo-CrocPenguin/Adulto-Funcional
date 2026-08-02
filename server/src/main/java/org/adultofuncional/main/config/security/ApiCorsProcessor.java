package org.adultofuncional.main.config.security;

import java.io.IOException;

import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Aplica la validación CORS de Spring y uniforma sus rechazos como errores API.
 *
 * <p>Spring rechaza un origen, método o header no permitido antes de llegar a
 * los handlers de seguridad y MVC. Este adaptador conserva toda la validación
 * del procesador oficial, pero sustituye su cuerpo de texto plano por el
 * contrato trazable de la aplicación.</p>
 */
@Component
@RequiredArgsConstructor
public class ApiCorsProcessor implements CorsProcessor {

  private final ApiErrorResponseWriter errorResponseWriter;

  @Override
  public boolean processRequest(
      @Nullable CorsConfiguration configuration,
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    DefaultCorsProcessor delegate = new DefaultCorsProcessor() {
      @Override
      protected void rejectRequest(ServerHttpResponse ignoredResponse) throws IOException {
        errorResponseWriter.write(
            request,
            response,
            HttpStatus.FORBIDDEN.value(),
            ApiErrorCode.CORS_REQUEST_REJECTED,
            "La solicitud CORS no está permitida");
      }
    };

    return delegate.processRequest(configuration, request, response);
  }
}
