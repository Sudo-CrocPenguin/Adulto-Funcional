package org.adultofuncional.main.account.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para {@link UpdateAccountRequest}.
 *
 * <p><strong>¿Qué se prueba?</strong><br>
 * El correcto funcionamiento del DTO de solicitud, incluyendo:
 * <ul>
 *   <li>Constructores y builder de Lombok</li>
 *   <li>Getters y Setters</li>
 *   <li>Validaciones de Bean Validation ({@code @NotBlank}, {@code @Email}, {@code @Size})</li>
 *   <li>Caso feliz: request válido sin violaciones</li>
 *   <li>Casos error: cada campo inválido genera una violación</li>
 * </ul>
 *
 * <p><strong>¿Cómo se prueba?</strong><br>
 * Se utiliza el {@code Validator} de Jakarta Bean Validation para verificar
 * que las restricciones se aplican correctamente.
 *
 * //TODO: Agregar pruebas para validación de formato de teléfono cuando se implemente @Pattern
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 */
@DisplayName("Pruebas unitarias para UpdateAccountRequest")
class UpdateAccountRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Debería crear una instancia usando el builder de Lombok")
    void shouldCreateInstanceUsingBuilder() {
        // Act
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        // Assert
        assertThat(request.getNames()).isEqualTo("Miguel Angel");
        assertThat(request.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(request.getPhone()).isEqualTo("+573001234567");
        assertThat(request.getEmail()).isEqualTo("miguel@example.com");
    }

    @Test
    @DisplayName("Un request válido no debe tener violaciones de validación")
    void validRequestShouldHaveNoViolations() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Debe fallar si el nombre está vacío o es nulo")
    void shouldFailWhenNamesIsBlank() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("names");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre es obligatorio");
    }

    @Test
    @DisplayName("Debe fallar si el email no tiene formato válido")
    void shouldFailWhenEmailIsInvalid() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("correo-invalido")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Debe ser un email válido");
    }
}