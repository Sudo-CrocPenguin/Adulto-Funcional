package org.adultofuncional.main.config.security;

import java.io.IOException;

import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Devuelve el contrato uniforme cuando un principal carece de permisos.
 */
@Component
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ApiErrorResponseWriter errorResponseWriter;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    errorResponseWriter.write(
        request,
        response,
        HttpStatus.FORBIDDEN.value(),
        ApiErrorCode.ACCESS_DENIED,
        "No tienes permiso para realizar esta operación");
  }
}
