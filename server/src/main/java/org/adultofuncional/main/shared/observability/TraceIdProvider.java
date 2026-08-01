package org.adultofuncional.main.shared.observability;

import java.util.UUID;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Genera y conserva el identificador de trazabilidad de una petición HTTP.
 *
 * <p>El identificador siempre es generado por el servidor. No se confía en un
 * valor aportado por el cliente, evitando inyección de contenido arbitrario en
 * logs y correlaciones.</p>
 */
@Component
public class TraceIdProvider {

  /** Cabecera pública que permite correlacionar la respuesta con los logs. */
  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  private static final String TRACE_ID_ATTRIBUTE =
      TraceIdProvider.class.getName() + ".traceId";

  /**
   * Obtiene el identificador de la petición o crea uno de 128 bits.
   */
  public String getOrCreate(HttpServletRequest request) {
    Object current = request.getAttribute(TRACE_ID_ATTRIBUTE);
    if (current instanceof String traceId && !traceId.isBlank()) {
      return traceId;
    }

    String traceId = UUID.randomUUID().toString().replace("-", "");
    request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
    return traceId;
  }
}
