package org.adultofuncional.main.config.security;

import java.io.IOException;

import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Devuelve el contrato uniforme cuando una ruta protegida no tiene principal.
 */
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ApiErrorResponseWriter errorResponseWriter;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authenticationException)
      throws IOException, ServletException {
    errorResponseWriter.write(
        request,
        response,
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorCode.AUTHENTICATION_REQUIRED,
        "Se requiere autenticación para acceder a este recurso");
  }
}
