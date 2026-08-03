package org.adultofuncional.main.shared.exception;

import static org.adultofuncional.main.shared.response.ApiErrorCode.ACCESS_DENIED;
import static org.adultofuncional.main.shared.response.ApiErrorCode.BUSINESS_RULE_VIOLATION;
import static org.adultofuncional.main.shared.response.ApiErrorCode.DATA_INTEGRITY_CONFLICT;
import static org.adultofuncional.main.shared.response.ApiErrorCode.ENDPOINT_NOT_FOUND;
import static org.adultofuncional.main.shared.response.ApiErrorCode.INTERNAL_ERROR;
import static org.adultofuncional.main.shared.response.ApiErrorCode.MEDIA_TYPE_UNSUPPORTED;
import static org.adultofuncional.main.shared.response.ApiErrorCode.METHOD_NOT_ALLOWED;
import static org.adultofuncional.main.shared.response.ApiErrorCode.PARAMETER_INVALID;
import static org.adultofuncional.main.shared.response.ApiErrorCode.REQUEST_BODY_INVALID;
import static org.adultofuncional.main.shared.response.ApiErrorCode.REQUIRED_PARAMETER_MISSING;
import static org.adultofuncional.main.shared.response.ApiErrorCode.REPRESENTATION_NOT_ACCEPTABLE;
import static org.adultofuncional.main.shared.response.ApiErrorCode.VALIDATION_FAILED;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.adultofuncional.main.shared.response.ApiErrorCode;
import org.adultofuncional.main.shared.response.ApiErrorFactory;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.adultofuncional.main.shared.response.FieldValidationError;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce excepciones de MVC, seguridad, aplicación y persistencia al contrato
 * uniforme definido por el ADR 0004.
 *
 * <p>Los mensajes públicos no incluyen detalles internos. Cada respuesta usa
 * un código estable, una lista determinista de errores de campo y el trace ID
 * generado para la petición. Las excepciones inesperadas conservan el stack
 * trace exclusivamente en logs.</p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final MediaType APPLICATION_JSON_UTF8 =
      new MediaType("application", "json", StandardCharsets.UTF_8);

  private final ApiErrorFactory errorFactory;

  /** Devuelve 429 y comunica cuándo puede reintentarse la operación. */
  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleRateLimit(
      RateLimitExceededException exception,
      HttpServletRequest request) {
    ApiResponse<Void> body = errorFactory.create(
        request,
        exception.getStatus(),
        exception.getCode(),
        exception.getMessage());
    return responseBuilder(exception.getStatus())
        .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.getRetryAfterSeconds()))
        .body(body);
  }

  /** Maneja errores de negocio conservando estado, código y mensaje seguros. */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(
      BusinessException exception,
      HttpServletRequest request) {
    log.debug("Error de negocio status={} code={}", exception.getStatus(), exception.getCode());
    return buildError(
        request,
        exception.getStatus(),
        exception.getCode(),
        exception.getMessage());
  }

  /** Maneja Bean Validation sobre cuerpos y objetos enlazados por MVC. */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
      org.springframework.web.bind.MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    return validationError(request, bindingErrors(exception));
  }

  /** Maneja validación de objetos enlazados fuera de un cuerpo JSON. */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBind(
      BindException exception,
      HttpServletRequest request) {
    return validationError(request, bindingErrors(exception));
  }

  /** Maneja validación directa de parámetros de métodos de controlador. */
  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodValidation(
      HandlerMethodValidationException exception,
      HttpServletRequest request) {
    List<FieldValidationError> errors = new ArrayList<>();
    exception.getParameterValidationResults().forEach(result -> {
      String parameterName = result.getMethodParameter().getParameterName();
      String field = parameterName == null
          ? "arg" + result.getMethodParameter().getParameterIndex()
          : parameterName;
      result.getResolvableErrors().forEach(error ->
          errors.add(toFieldError(field, error)));
    });
    exception.getCrossParameterValidationResults().forEach(error ->
        errors.add(toFieldError("$", error)));
    return validationError(request, errors);
  }

  /** Maneja restricciones declaradas directamente sobre parámetros. */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException exception,
      HttpServletRequest request) {
    List<FieldValidationError> errors = exception.getConstraintViolations().stream()
        .map(this::toFieldError)
        .toList();
    return validationError(request, errors);
  }

  /** Maneja cuerpos JSON truncados, inválidos o con tipos incompatibles. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotReadable(
      HttpMessageNotReadableException exception,
      HttpServletRequest request) {
    log.debug("Cuerpo de solicitud inválido", exception);
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        REQUEST_BODY_INVALID,
        "El cuerpo de la solicitud no es válido");
  }

  /** Maneja parámetros de ruta o query con un tipo incompatible. */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException exception,
      HttpServletRequest request) {
    log.debug("Parámetro con tipo inválido: {}", exception.getName());
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        PARAMETER_INVALID,
        "Uno o más parámetros no son válidos");
  }

  /** Maneja query parameters obligatorios ausentes. */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
      MissingServletRequestParameterException exception,
      HttpServletRequest request) {
    log.debug("Parámetro obligatorio ausente: {}", exception.getParameterName());
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        REQUIRED_PARAMETER_MISSING,
        "Falta un parámetro obligatorio");
  }

  /** Maneja headers u otros valores obligatorios de la petición ausentes. */
  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<ApiResponse<Void>> handleRequestBinding(
      ServletRequestBindingException exception,
      HttpServletRequest request) {
    log.debug("Valor obligatorio de la petición ausente", exception);
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        REQUIRED_PARAMETER_MISSING,
        "Falta un valor obligatorio en la solicitud");
  }

  /** Maneja reglas de dominio expresadas como argumentos inválidos. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
      IllegalArgumentException exception,
      HttpServletRequest request) {
    log.debug("Regla de negocio incumplida", exception);
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        BUSINESS_RULE_VIOLATION,
        "La solicitud incumple una regla de negocio");
  }

  /** Maneja rutas inexistentes sin convertirlas en errores internos. */
  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  public ResponseEntity<ApiResponse<Void>> handleEndpointNotFound(
      Exception exception,
      HttpServletRequest request) {
    log.debug("Endpoint no encontrado: {}", request.getRequestURI());
    return buildError(
        request,
        HttpStatus.NOT_FOUND,
        ENDPOINT_NOT_FOUND,
        "Endpoint no encontrado");
  }

  /** Maneja métodos HTTP no permitidos por una ruta existente. */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException exception,
      HttpServletRequest request) {
    log.debug("Método HTTP no permitido: {}", exception.getMethod());
    ApiResponse<Void> body = errorFactory.create(
        request,
        HttpStatus.METHOD_NOT_ALLOWED.value(),
        METHOD_NOT_ALLOWED,
        "Método HTTP no permitido");
    ResponseEntity.BodyBuilder response = responseBuilder(HttpStatus.METHOD_NOT_ALLOWED.value());
    if (exception.getSupportedHttpMethods() != null) {
      response.allow(exception.getSupportedHttpMethods().toArray(HttpMethod[]::new));
    }
    return response.body(body);
  }

  /** Maneja Content-Type no soportado por el endpoint. */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException exception,
      HttpServletRequest request) {
    log.debug("Content-Type no soportado: {}", exception.getContentType());
    return buildError(
        request,
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        MEDIA_TYPE_UNSUPPORTED,
        "Tipo de contenido no soportado");
  }

  /**
   * Maneja negociación de respuesta incompatible. El cuerpo puede ser omitido
   * por el contenedor si el cliente rechaza también {@code application/json}.
   */
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException exception,
      HttpServletRequest request) {
    log.debug("Formato de respuesta no aceptable", exception);
    return buildError(
        request,
        HttpStatus.NOT_ACCEPTABLE,
        REPRESENTATION_NOT_ACCEPTABLE,
        "No existe un formato de respuesta aceptable");
  }

  /** Maneja restricciones de integridad sin exponer SQL ni nombres internos. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
      DataIntegrityViolationException exception,
      HttpServletRequest request) {
    log.debug("Conflicto de integridad de datos", exception);
    return buildError(
        request,
        HttpStatus.CONFLICT,
        DATA_INTEGRITY_CONFLICT,
        "La operación entra en conflicto con los datos existentes");
  }

  /** Maneja denegaciones producidas dentro de Spring MVC, como PreAuthorize. */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
      AccessDeniedException exception,
      HttpServletRequest request) {
    log.debug("Acceso denegado", exception);
    return buildError(
        request,
        HttpStatus.FORBIDDEN,
        ACCESS_DENIED,
        "No tienes permiso para realizar esta operación");
  }

  /** Red de seguridad para errores inesperados no atribuibles al cliente. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(
      Exception exception,
      HttpServletRequest request) {
    ApiResponse<Void> body = errorFactory.create(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        INTERNAL_ERROR,
        "Ocurrió un error interno");
    log.error("Error interno no controlado traceId={}", body.getTraceId(), exception);
    return response(HttpStatus.INTERNAL_SERVER_ERROR.value(), body);
  }

  private ResponseEntity<ApiResponse<Void>> validationError(
      HttpServletRequest request,
      List<FieldValidationError> errors) {
    return buildError(
        request,
        HttpStatus.BAD_REQUEST,
        VALIDATION_FAILED,
        "La solicitud contiene datos inválidos",
        errors);
  }

  private List<FieldValidationError> bindingErrors(BindException exception) {
    List<FieldValidationError> errors = new ArrayList<>();
    exception.getBindingResult().getFieldErrors().stream()
        .map(this::toFieldError)
        .forEach(errors::add);
    exception.getBindingResult().getGlobalErrors().stream()
        .map(error -> toFieldError("$", error))
        .forEach(errors::add);
    return errors;
  }

  private FieldValidationError toFieldError(FieldError error) {
    return new FieldValidationError(
        error.getField(),
        error.getCode() == null ? "Invalid" : error.getCode(),
        safeMessage(error));
  }

  private FieldValidationError toFieldError(
      String field,
      MessageSourceResolvable error) {
    String[] codes = error.getCodes();
    String code = codes == null || codes.length == 0 ? "Invalid" : codes[0];
    int separator = code.indexOf('.');
    if (separator > 0) {
      code = code.substring(0, separator);
    }
    return new FieldValidationError(field, code, safeMessage(error));
  }

  private FieldValidationError toFieldError(ConstraintViolation<?> violation) {
    String path = violation.getPropertyPath().toString();
    int separator = path.lastIndexOf('.');
    String field = separator >= 0 ? path.substring(separator + 1) : path;
    String code = violation.getConstraintDescriptor()
        .getAnnotation()
        .annotationType()
        .getSimpleName();
    return new FieldValidationError(field, code, violation.getMessage());
  }

  private String safeMessage(MessageSourceResolvable error) {
    String message = error.getDefaultMessage();
    return message == null || message.isBlank() ? "Valor inválido" : message;
  }

  private ResponseEntity<ApiResponse<Void>> buildError(
      HttpServletRequest request,
      HttpStatus status,
      ApiErrorCode code,
      String message) {
    return buildError(request, status.value(), code, message, List.of());
  }

  private ResponseEntity<ApiResponse<Void>> buildError(
      HttpServletRequest request,
      int status,
      ApiErrorCode code,
      String message) {
    return buildError(request, status, code, message, List.of());
  }

  private ResponseEntity<ApiResponse<Void>> buildError(
      HttpServletRequest request,
      HttpStatus status,
      ApiErrorCode code,
      String message,
      List<FieldValidationError> errors) {
    return buildError(request, status.value(), code, message, errors);
  }

  private ResponseEntity<ApiResponse<Void>> buildError(
      HttpServletRequest request,
      int status,
      ApiErrorCode code,
      String message,
      List<FieldValidationError> errors) {
    return response(status, errorFactory.create(request, status, code, message, errors));
  }

  private ResponseEntity<ApiResponse<Void>> response(
      int status,
      ApiResponse<Void> body) {
    return responseBuilder(status).body(body);
  }

  private ResponseEntity.BodyBuilder responseBuilder(int status) {
    ResponseEntity.BodyBuilder response = ResponseEntity.status(status)
        .contentType(APPLICATION_JSON_UTF8);
    if (status == HttpStatus.UNAUTHORIZED.value()) {
      response.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    }
    return response;
  }
}
