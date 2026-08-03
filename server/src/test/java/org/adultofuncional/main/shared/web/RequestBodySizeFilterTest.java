package org.adultofuncional.main.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.adultofuncional.main.shared.response.ApiErrorFactory;
import org.adultofuncional.main.shared.response.ApiErrorResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

class RequestBodySizeFilterTest {

  private RequestBodySizeFilter filter;

  @BeforeEach
  void setUp() {
    TraceIdProvider traceIdProvider = new TraceIdProvider();
    ApiErrorFactory errorFactory = new ApiErrorFactory(traceIdProvider);
    ApiErrorResponseWriter writer = new ApiErrorResponseWriter(new ObjectMapper(), errorFactory);
    filter = new RequestBodySizeFilter(DataSize.ofBytes(8), writer);
  }

  @Test
  void preservesAnAllowedBodyForDownstreamConsumers() throws Exception {
    MockHttpServletRequest request = requestWithBody("12345678");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> receivedBody = new AtomicReference<>();

    filter.doFilter(request, response, (filteredRequest, ignoredResponse) -> receivedBody.set(
        new String(filteredRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8)));

    assertThat(receivedBody).hasValue("12345678");
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void rejectsABodyWhoseDeclaredLengthExceedsTheLimit() throws Exception {
    MockHttpServletRequest request = requestWithBody("123456789");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
      throw new AssertionError("La cadena no debe continuar");
    });

    assertUniformPayloadTooLarge(response);
  }

  @Test
  void rejectsAnOversizedChunkedBodyWithoutContentLength() throws Exception {
    MockHttpServletRequest source = requestWithBody("123456789");
    HttpServletRequest request = new jakarta.servlet.http.HttpServletRequestWrapper(source) {
      @Override
      public int getContentLength() {
        return -1;
      }

      @Override
      public long getContentLengthLong() {
        return -1;
      }
    };
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
      throw new AssertionError("La cadena no debe continuar");
    });

    assertUniformPayloadTooLarge(response);
  }

  private MockHttpServletRequest requestWithBody(String body) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    return request;
  }

  private void assertUniformPayloadTooLarge(MockHttpServletResponse response) throws Exception {
    assertThat(response.getStatus()).isEqualTo(413);
    assertThat(response.getContentType()).startsWith("application/json");
    assertThat(response.getHeader(TraceIdProvider.TRACE_ID_HEADER)).isNotBlank();
    assertThat(response.getContentAsString()).contains("\"code\":\"REQUEST_TOO_LARGE\"");
  }
}
