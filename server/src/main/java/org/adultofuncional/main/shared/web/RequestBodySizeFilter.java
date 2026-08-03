package org.adultofuncional.main.shared.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiErrorResponseWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Limita de forma uniforme el cuerpo de las solicitudes HTTP.
 *
 * <p>Tomcat limita formularios y multipart, pero esos parámetros no protegen
 * los cuerpos JSON. Este filtro lee como máximo un byte adicional al límite,
 * rechaza también transferencias sin {@code Content-Length} y vuelve a exponer
 * el cuerpo en memoria para que Spring MVC pueda deserializarlo normalmente.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestBodySizeFilter extends OncePerRequestFilter {

  private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH", "DELETE");

  private final int maxRequestBodyBytes;
  private final ApiErrorResponseWriter errorResponseWriter;

  public RequestBodySizeFilter(
      @Value("${app.http.max-request-body-size:1MB}") DataSize maxRequestBodySize,
      ApiErrorResponseWriter errorResponseWriter) {
    long bytes = maxRequestBodySize.toBytes();
    if (bytes < 1 || bytes >= Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          "app.http.max-request-body-size debe estar entre 1 byte y 2 GB");
    }
    this.maxRequestBodyBytes = (int) bytes;
    this.errorResponseWriter = errorResponseWriter;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (!mayContainBody(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    long declaredLength = request.getContentLengthLong();
    if (declaredLength > maxRequestBodyBytes) {
      writePayloadTooLarge(request, response);
      return;
    }

    byte[] body = request.getInputStream().readNBytes(maxRequestBodyBytes + 1);
    if (body.length > maxRequestBodyBytes) {
      writePayloadTooLarge(request, response);
      return;
    }

    filterChain.doFilter(new CachedBodyRequest(request, body), response);
  }

  private boolean mayContainBody(HttpServletRequest request) {
    return request.getContentLengthLong() != 0
        || request.getHeader("Transfer-Encoding") != null
        || METHODS_WITH_BODY.contains(request.getMethod().toUpperCase(Locale.ROOT));
  }

  private void writePayloadTooLarge(
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    errorResponseWriter.write(
        request,
        response,
        HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
        ApiErrorCode.REQUEST_TOO_LARGE,
        "El cuerpo de la solicitud supera el tamaño máximo permitido");
  }

  private static final class CachedBodyRequest extends HttpServletRequestWrapper {

    private final byte[] body;

    private CachedBodyRequest(HttpServletRequest request, byte[] body) {
      super(request);
      this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() {
      return new CachedBodyServletInputStream(body);
    }

    @Override
    public BufferedReader getReader() {
      String encoding = getCharacterEncoding();
      Charset charset = encoding == null
          ? StandardCharsets.ISO_8859_1
          : Charset.forName(encoding);
      return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    @Override
    public int getContentLength() {
      return body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body.length;
    }
  }

  private static final class CachedBodyServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream input;

    private CachedBodyServletInputStream(byte[] body) {
      this.input = new ByteArrayInputStream(body);
    }

    @Override
    public boolean isFinished() {
      return input.available() == 0;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      try {
        if (!isFinished()) {
          readListener.onDataAvailable();
        }
        if (isFinished()) {
          readListener.onAllDataRead();
        }
      } catch (IOException exception) {
        readListener.onError(exception);
      }
    }

    @Override
    public int read() {
      return input.read();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) {
      return input.read(bytes, offset, length);
    }
  }
}
