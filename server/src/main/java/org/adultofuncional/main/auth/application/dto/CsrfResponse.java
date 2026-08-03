package org.adultofuncional.main.auth.application.dto;

/** Metadatos que permiten a un navegador devolver el token CSRF. */
public record CsrfResponse(String token, String headerName, String parameterName) {
}
