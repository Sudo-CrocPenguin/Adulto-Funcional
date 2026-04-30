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
 * Pruebas unitarias para el DTO {@link UpdateAccountRequest}.
 *
 * <p>Verifica:
 * <ul>
 *   <li>Constructores, builder, getters y setters.</li>
 *   <li>Las restricciones de validación (Bean Validation) como {@code @NotBlank}, {@code @Email}, {@code @Size}.</li>
 * </ul>
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
    @DisplayName("Debería crear una instancia con el constructor por defecto y setters")
    void shouldCreateInstanceWithDefaultConstructorAndSetters() {
        // Arrange
        UpdateAccountRequest request = new UpdateAccountRequest();

        // Act
        request.setNames("Miguel Angel");
        request.setLastnames("Blandon Montes");
        request.setEmail("miguel@example.com");
        request.setPhone("+573001234567");

        // Assert
        assertThat(request.getNames()).isEqualTo("Miguel Angel");
        assertThat(request.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(request.getEmail()).isEqualTo("miguel@example.com");
        assertThat(request.getPhone()).isEqualTo("+573001234567");
    }

    @Test
    @DisplayName("Debería crear una instancia con el constructor con todos los argumentos")
    void shouldCreateInstanceWithAllArgsConstructor() {
        // Act
        UpdateAccountRequest request = new UpdateAccountRequest(
                "Miguel Angel",
                "Blandon Montes",
                "+573001234567",
                "miguel@example.com"
        );

        // Assert
        assertThat(request.getNames()).isEqualTo("Miguel Angel");
        assertThat(request.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(request.getPhone()).isEqualTo("+573001234567");
        assertThat(request.getEmail()).isEqualTo("miguel@example.com");
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
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void equalsAndHashCodeShouldWorkCorrectly() {
        // Arrange
        UpdateAccountRequest request1 = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        UpdateAccountRequest request2 = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        UpdateAccountRequest request3 = UpdateAccountRequest.builder()
                .names("Juan")
                .lastnames("Perez")
                .phone("123456789")
                .email("juan@example.com")
                .build();

        // Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

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
                .names("")  // blank
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
    @DisplayName("Debe fallar si el nombre excede los 50 caracteres")
    void shouldFailWhenNamesExceedsMaxLength() {
        // Arrange
        String longName = "a".repeat(51);
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names(longName)
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("names");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El nombre no puede exceder 50 caracteres");
    }

    @Test
    @DisplayName("Debe fallar si los apellidos están vacíos o son nulos")
    void shouldFailWhenLastnamesIsBlank() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("")  // blank
                .phone("+573001234567")
                .email("miguel@example.com")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastnames");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Los apellidos son obligatorios");
    }

    @Test
    @DisplayName("Debe fallar si el teléfono está vacío o es nulo")
    void shouldFailWhenPhoneIsBlank() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("")
                .email("miguel@example.com")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El teléfono es obligatorio");
    }

    @Test
    @DisplayName("Debe fallar si el email está vacío")
    void shouldFailWhenEmailIsBlank() {
        // Arrange
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email("")
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El email es obligatorio");
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

    @Test
    @DisplayName("Debe fallar si el email excede los 255 caracteres")
    void shouldFailWhenEmailExceedsMaxLength() {
        // Arrange
        String longEmail = "a".repeat(250) + "@example.com"; // > 255?
        // Construir un email de exactamente 260 caracteres
        String localPart = "a".repeat(250);
        String domain = "@example.com";
        String email = localPart + domain; // 250 + 13 = 263
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .phone("+573001234567")
                .email(email)
                .build();

        // Act
        Set<ConstraintViolation<UpdateAccountRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).isEqualTo("El email no puede exceder 255 caracteres");
    }
}