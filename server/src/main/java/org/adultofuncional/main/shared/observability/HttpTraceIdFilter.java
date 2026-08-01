package org.adultofuncional.main.shared.observability;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Asigna un trace ID antes de entrar a seguridad o a Spring MVC.
 *
 * <p>El valor se expone en {@code X-Trace-Id}, se guarda como atributo del
 * request y se incorpora temporalmente al MDC con la clave {@code traceId}.
 * De este modo, filtros, controladores y handlers comparten la misma
 * correlación.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HttpTraceIdFilter extends OncePerRequestFilter {

  private static final String MDC_KEY = "traceId";

  private final TraceIdProvider traceIdProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String traceId = traceIdProvider.getOrCreate(request);
    String previousTraceId = MDC.get(MDC_KEY);

    response.setHeader(TraceIdProvider.TRACE_ID_HEADER, traceId);
    MDC.put(MDC_KEY, traceId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      if (previousTraceId == null) {
        MDC.remove(MDC_KEY);
      } else {
        MDC.put(MDC_KEY, previousTraceId);
      }
    }
  }
}
