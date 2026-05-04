package org.adultofuncional.main.config.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad para identificar el tipo de cliente que origina un request HTTP.
 *
 * <p>
 * Aplica tres capas de detección en orden descendente de confiabilidad
 * para determinar si un request proviene de una app nativa (móvil o desktop)
 * o de un navegador web. Esta distinción define si el token JWT puede
 * exponerse en el body de la respuesta o debe mantenerse exclusivamente
 * en la HttpOnly cookie.
 *
 * <p>
 * Capas de detección:
 * <ol>
 * <li><b>User-Agent nativo conocido</b> — señal pasiva de alta confianza.
 * Las apps nativas usan identificadores propios (MiApp/, okhttp,
 * Alamofire).</li>
 * <li><b>Ausencia de Origin + UA no-browser</b> — señal pasiva complementaria.
 * Requests desde JS en un navegador SIEMPRE incluyen Origin (política del
 * browser,
 * no suprimible por código). Su ausencia junto con UA no-Mozilla indica cliente
 * nativo.</li>
 * <li><b>Header X-Client-Type</b> — señal activa declarativa (menor confianza).
 * Controlable por el cliente, por tanto falsificable. Se mantiene como capa
 * adicional de intención explícita, pero nunca actúa sola: debe coincidir
 * con al menos una señal pasiva para ser considerada válida.</li>
 * </ol>
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 */
@Component
public class ClientTypeResolver {

  /**
   * Header declarativo que los clientes nativos deben incluir para
   * identificarse explícitamente. Valores esperados: {@code mobile},
   * {@code desktop}.
   */
  private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

  /**
   * Determina si el request proviene de un cliente nativo (app móvil o desktop),
   * lo que habilita incluir el token JWT en el body de la respuesta.
   *
   * @param request el request HTTP entrante
   * @return {@code true} si se detecta un cliente nativo con señales pasivas
   *         confirmadas
   */
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
