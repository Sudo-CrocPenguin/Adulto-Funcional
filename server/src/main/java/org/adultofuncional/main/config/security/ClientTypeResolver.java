package org.adultofuncional.main.config.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientTypeResolver {

  private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

  public boolean isNativeClient(HttpServletRequest request) {
    String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
    String origin = request.getHeader("Origin");
    String referer = request.getHeader("Referer");
    String clientType = request.getHeader(CLIENT_TYPE_HEADER);

    // Capa 1: User-Agent de app nativa conocida
    boolean isKnownNativeAgent = userAgent.startsWith("MiApp/")
        || userAgent.startsWith("MiAppDesktop/")
        || userAgent.contains("okhttp")
        || userAgent.contains("Alamofire");

    // Capa 2: sin origen web + sin patrón de navegador
    boolean hasNoWebOrigin = (origin == null && referer == null);
    boolean looksLikeBrowser = userAgent.contains("Mozilla");
    boolean passiveSignal = isKnownNativeAgent || (hasNoWebOrigin && !looksLikeBrowser);

    // Capa 3: header declarativo — válido solo si una señal pasiva lo confirma
    boolean declaresNative = "mobile".equalsIgnoreCase(clientType)
        || "desktop".equalsIgnoreCase(clientType);

    return passiveSignal && declaresNative;
  }
}
