package org.adultofuncional.main.shared.exception;

import java.util.HashMap;
import java.util.Map;

import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones de la aplicación.
 *
 * <p>Intercepta todas las excepciones lanzadas en cualquier parte del sistema y
 * las convierte en respuestas HTTP estandarizadas usando {@link ApiResponse}.
 * De esta forma, todos los errores de la aplicación siguen el mismo formato
 * de respuesta, evitando que cada controlador tenga que manejar sus propios
 * errores individualmente.</p>
 *
 * <p>Funciona gracias a la anotación {@code @RestControllerAdvice}, que le indica
 * a Spring que esta clase debe interceptar las excepciones antes de que lleguen
 * al cliente.</p>
 */

@RestControllerAdvice
public class GlobalExceptionHandler  {

    /**
     * Maneja las excepciones de negocio generales de la aplicación.
     *
     * <p>Captura cualquier {@link BusinessException} y construye una respuesta
     * HTTP usando el código de estado y el mensaje que trae la excepción.</p>
     *
     * @param ex excepción de negocio lanzada
     * @return respuesta HTTP con el código y mensaje de la excepción
     */

    @ExceptionHandler(BusinessException.class)

    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {

        ApiResponse<Void> response = new ApiResponse<>(ex.getStatus(), ex.getMessage(), null );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Maneja las excepciones de recurso no encontrado (HTTP 404).
     *
     * <p>Captura {@link NotFoundException} y delega a {@link #handleBusiness}
     * ya que comparte el mismo formato de respuesta.</p>
     *
     * @param ex excepción lanzada cuando un recurso no existe
     * @return respuesta HTTP 404 con el mensaje de la excepción
     */

    @ExceptionHandler(NotFoundException.class)

    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {

        return handleBusiness(ex);
    }


     /**
     * Maneja los errores de validación de los campos del request (HTTP 400).
     *
     * <p>Captura {@link MethodArgumentNotValidException}, que Spring lanza
     * automáticamente cuando un campo anotado con {@code @Valid} no cumple
     * las restricciones definidas. Recorre todos los errores de validación
     * y los agrupa en un mapa donde la clave es el nombre del campo y el
     * valor es el mensaje de error correspondiente.</p>
     *
     * @param ex excepción lanzada por Spring cuando la validación de campos falla
     * @return respuesta HTTP 400 con un mapa de los campos que fallaron y sus errores
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation (MethodArgumentNotValidException ex){

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ApiResponse<Map<String, String>> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Error de validación", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja cualquier excepción no controlada por los demás handlers (HTTP 500).
     *
     * <p>Actúa como red de seguridad del sistema — captura cualquier excepción
     * inesperada que no haya sido contemplada y retorna un error genérico al
     * cliente, evitando que información sensible del servidor quede expuesta.</p>
     *
     * @param ex excepción inesperada lanzada en cualquier parte del sistema
     * @return respuesta HTTP 500 con un mensaje de error interno
     */

    @ExceptionHandler(Exception.class)

    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {

        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno" + ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    }

    /**
     * Maneja las excepciones de acceso no autorizado (HTTP 401).
     *
     * <p>Captura {@link UnauthorizedException} y delega a {@link #handleBusiness}.
     * Se lanza típicamente cuando las credenciales de inicio de sesión son
     * incorrectas.</p>
     *
     * @param ex excepción lanzada cuando el usuario no está autenticado
     * @return respuesta HTTP 401 con el mensaje de la excepción
     */

    @ExceptionHandler(UnauthorizedException.class)

    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {

        return handleBusiness(ex);
    }

    /**
     * Maneja las excepciones de conflicto de datos (HTTP 409).
     *
     * <p>Captura {@link ConflictException} y delega a {@link #handleBusiness}.
     * Se lanza típicamente cuando se intenta registrar un correo electrónico
     * que ya existe en el sistema.</p>
     *
     * @param ex excepción lanzada cuando hay un conflicto con datos existentes
     * @return respuesta HTTP 409 con el mensaje de la excepción
     */

    @ExceptionHandler(ConflictException.class)
    
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {

        return handleBusiness(ex);
    }
    
    /**
     * Maneja las excepciones de acceso denegado (HTTP 403).
     *
     * <p>Captura {@link ForbiddenException} y delega a {@link #handleBusiness}.
     * Se lanza cuando un usuario autenticado intenta acceder al gestor de
     * contraseñas sin haber verificado su contraseña maestra.</p>
     *
     * @param ex excepción lanzada cuando se deniega el acceso a un recurso protegido
     * @return respuesta HTTP 403 con el mensaje de la excepción
     */

    @ExceptionHandler(ForbiddenException.class)

    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {

        return handleBusiness(ex);
    }
}
