package org.adultofuncional.main.auth.application.dto;

import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cuerpo opcional de refresh usado exclusivamente por clientes nativos. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {

  @Size(min = 40, max = 256, message = "El refresh token tiene una longitud inválida")
  @NoHtml
  private String refreshToken;
}
