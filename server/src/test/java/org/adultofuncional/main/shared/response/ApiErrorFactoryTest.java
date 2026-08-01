package org.adultofuncional.main.shared.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.adultofuncional.main.shared.observability.TraceIdProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ApiErrorFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ApiErrorFactory errorFactory = new ApiErrorFactory(new TraceIdProvider());

  @Test
  void preservesHistoricalSuccessContract() throws Exception {
    ApiResponse<String> response = new ApiResponse<>(200, "Operación exitosa", "resultado");

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsBytes(response));

    assertThat(json.size()).isEqualTo(3);
    assertThat(json.path("status").asInt()).isEqualTo(200);
    assertThat(json.path("message").asText()).isEqualTo("Operación exitosa");
    assertThat(json.path("data").asText()).isEqualTo("resultado");
    assertThat(json.has("code")).isFalse();
    assertThat(json.has("fieldErrors")).isFalse();
    assertThat(json.has("traceId")).isFalse();
  }

  @Test
  void buildsCompleteErrorAndOrdersFieldErrorsDeterministically() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    ApiResponse<Void> response = errorFactory.create(
        request,
        400,
        ApiErrorCode.VALIDATION_FAILED,
        "La solicitud contiene datos inválidos",
        List.of(
            new FieldValidationError("phone", "Size", "Longitud inválida"),
            new FieldValidationError("email", "NotBlank", "El email es obligatorio")));

    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getCode()).isEqualTo("VALIDATION_FAILED");
    assertThat(response.getData()).isNull();
    assertThat(response.getTraceId()).hasSize(32).matches("[0-9a-f]{32}");
    assertThat(response.getFieldErrors())
        .extracting(FieldValidationError::field)
        .containsExactly("email", "phone");
  }

  @Test
  void reusesTraceIdForEveryErrorInTheSameRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    ApiResponse<Void> first = errorFactory.create(
        request, 404, ApiErrorCode.RESOURCE_NOT_FOUND, "Recurso no encontrado");
    ApiResponse<Void> second = errorFactory.create(
        request, 500, ApiErrorCode.INTERNAL_ERROR, "Error interno");

    assertThat(second.getTraceId()).isEqualTo(first.getTraceId());
  }
}
