package org.adultofuncional.main.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.adultofuncional.main.shared.response.ApiErrorFactory;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
      new ApiErrorFactory(new TraceIdProvider()));

  @Test
  void exposesStableBusinessErrorWithoutRemovingHistoricalFields() {
    ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(
        new ConflictException("El correo ya está registrado"),
        new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getHeaders().getContentType().toString())
        .isEqualTo("application/json;charset=UTF-8");
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(409);
    assertThat(response.getBody().getCode()).isEqualTo("RESOURCE_CONFLICT");
    assertThat(response.getBody().getMessage()).isEqualTo("El correo ya está registrado");
    assertThat(response.getBody().getFieldErrors()).isEmpty();
    assertThat(response.getBody().getTraceId()).hasSize(32);
    assertThat(response.getBody().getData()).isNull();
  }

  @Test
  void returnsValidationErrorsOrderedByField() throws Exception {
    ValidationInput input = new ValidationInput();
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(input, "input");
    bindingResult.addError(fieldError("phone", "Size", "Teléfono inválido"));
    bindingResult.addError(fieldError("email", "NotBlank", "Email obligatorio"));

    Method method = ValidationFixture.class.getDeclaredMethod("accept", ValidationInput.class);
    MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
        new MethodParameter(method, 0), bindingResult);

    ResponseEntity<ApiResponse<Void>> response = handler.handleMethodArgumentNotValid(
        exception,
        new MockHttpServletRequest());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_FAILED");
    assertThat(response.getBody().getFieldErrors())
        .extracting(error -> error.field())
        .containsExactly("email", "phone");
  }

  @Test
  void mapsMissingEndpointToNotFoundContract() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inexistente");

    ResponseEntity<ApiResponse<Void>> response = handler.handleEndpointNotFound(
        new NoResourceFoundException(HttpMethod.GET, "api/inexistente"),
        request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo("ENDPOINT_NOT_FOUND");
    assertThat(response.getBody().getMessage()).isEqualTo("Endpoint no encontrado");
  }

  private FieldError fieldError(String field, String code, String message) {
    return new FieldError(
        "input",
        field,
        null,
        false,
        new String[] {code},
        null,
        message);
  }

  private static final class ValidationFixture {
    @SuppressWarnings("unused")
    void accept(ValidationInput input) {
    }
  }

  private static final class ValidationInput {
    @SuppressWarnings("unused")
    private String email;

    @SuppressWarnings("unused")
    private String phone;
  }
}
