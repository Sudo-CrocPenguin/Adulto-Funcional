package org.adultofuncional.main.shared.exception;

import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler(BusinessException.class)

    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {

        ApiResponse<Void> response = new ApiResponse<>(ex.getStatus(), ex.getMessage(), null );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(NotFoundException.class)

    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {

        return handleBusiness(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation (MethodArgumentNotValidException ex){

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ApiResponse<Map<String, String>> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Error de validación", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)

    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {

        ApiResponse<Void> response = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error interno" + ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    }

    @ExceptionHandler(UnauthorizedException.class)

    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {

        return handleBusiness(ex);
    }

    @ExceptionHandler(ConflictException.class)
    
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {

        return handleBusiness(ex);
    }
    
}
