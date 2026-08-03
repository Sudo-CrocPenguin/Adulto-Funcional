package org.adultofuncional.main.shared.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpTraceIdFilterTest {

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void generatesServerTraceIdAndReturnsItInResponseHeader() throws Exception {
    TraceIdProvider provider = new TraceIdProvider();
    HttpTraceIdFilter filter = new HttpTraceIdFilter(provider);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(TraceIdProvider.TRACE_ID_HEADER, "valor-controlado-por-cliente");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, new MockFilterChain());

    String traceId = response.getHeader(TraceIdProvider.TRACE_ID_HEADER);
    assertThat(traceId).hasSize(32).matches("[0-9a-f]{32}");
    assertThat(traceId).isNotEqualTo("valor-controlado-por-cliente");
    assertThat(provider.getOrCreate(request)).isEqualTo(traceId);
    assertThat(MDC.get("traceId")).isNull();
  }

  @Test
  void restoresPreviousMdcValueAfterRequest() throws Exception {
    MDC.put("traceId", "traza-anterior");
    HttpTraceIdFilter filter = new HttpTraceIdFilter(new TraceIdProvider());

    filter.doFilterInternal(
        new MockHttpServletRequest(),
        new MockHttpServletResponse(),
        new MockFilterChain());

    assertThat(MDC.get("traceId")).isEqualTo("traza-anterior");
  }
}
