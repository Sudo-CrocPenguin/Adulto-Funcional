package org.adultofuncional.main.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtils {

  @Value("${COOKIE_SECURE}") // TODO: true en produccion
  private boolean secure;

  public void addTokenCookie(HttpServletResponse response, String token, long expirationMs) {
    response.addHeader("Set-Cookie",
        String.format("token=%s; HttpOnly; %sPath=/; Max-Age=%d; SameSite=Strict",
            token,
            secure ? "Secure; " : "",
            (int) (expirationMs / 1000)));
  }

  public void clearTokenCookie(HttpServletResponse response) {
    response.addHeader("Set-Cookie",
        "token=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict");
  }
}
