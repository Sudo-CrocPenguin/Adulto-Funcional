package org.adultofuncional.main.account.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para el DTO {@link AccountResponse}.
 *
 * <p>Verifica el correcto funcionamiento de:
 * <ul>
 *   <li>Constructores (vacío y con todos los argumentos).</li>
 *   <li>Builder de Lombok.</li>
 *   <li>Getters y Setters.</li>
 *   <li>Métodos equals(), hashCode() y toString().</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @version 1.0
 * @since 0.0.1
 */
@DisplayName("Pruebas unitarias para AccountResponse")
class AccountResponseTest {

    private final UUID testId = UUID.randomUUID();
    private final LocalDateTime testCreatedAt = LocalDateTime.of(2025, 1, 1, 10, 30);

    @Test
    @DisplayName("Debería crear una instancia con el constructor por defecto y usar setters")
    void shouldCreateInstanceWithDefaultConstructorAndSetters() {
        // Arrange
        AccountResponse response = new AccountResponse();

        // Act
        response.setId(testId);
        response.setNames("Miguel Angel");
        response.setLastnames("Blandon Montes");
        response.setEmail("miguel@example.com");
        response.setPhone("+573001234567");
        response.setCreatedAt(testCreatedAt);

        // Assert
        assertThat(response.getId()).isEqualTo(testId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
    }

    @Test
    @DisplayName("Debería crear una instancia con el constructor con todos los argumentos")
    void shouldCreateInstanceWithAllArgsConstructor() {
        // Act
        AccountResponse response = new AccountResponse(
                testId,
                "Miguel Angel",
                "Blandon Montes",
                "miguel@example.com",
                "+573001234567",
                testCreatedAt
        );

        // Assert
        assertThat(response.getId()).isEqualTo(testId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
    }

    @Test
    @DisplayName("Debería crear una instancia usando el builder de Lombok")
    void shouldCreateInstanceUsingBuilder() {
        // Act
        AccountResponse response = AccountResponse.builder()
                .id(testId)
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .createdAt(testCreatedAt)
                .build();

        // Assert
        assertThat(response.getId()).isEqualTo(testId);
        assertThat(response.getNames()).isEqualTo("Miguel Angel");
        assertThat(response.getLastnames()).isEqualTo("Blandon Montes");
        assertThat(response.getEmail()).isEqualTo("miguel@example.com");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getCreatedAt()).isEqualTo(testCreatedAt);
    }

    @Test
    @DisplayName("Dos objetos con los mismos datos deberían ser iguales según equals/hashCode")
    void shouldBeEqualWhenSameData() {
        // Arrange
        AccountResponse response1 = AccountResponse.builder()
                .id(testId)
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .createdAt(testCreatedAt)
                .build();

        AccountResponse response2 = AccountResponse.builder()
                .id(testId)
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .createdAt(testCreatedAt)
                .build();

        // Assert
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Dos objetos con diferentes datos no deberían ser iguales")
    void shouldNotBeEqualWhenDifferentData() {
        // Arrange
        AccountResponse response1 = AccountResponse.builder()
                .id(testId)
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .createdAt(testCreatedAt)
                .build();

        AccountResponse response2 = AccountResponse.builder()
                .id(UUID.randomUUID())
                .names("Juan")
                .lastnames("Perez")
                .email("juan@example.com")
                .phone("123456789")
                .createdAt(LocalDateTime.now())
                .build();

        // Assert
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("toString() no debería lanzar excepción y debe contener los campos principales")
    void toStringShouldNotThrowExceptionAndContainFields() {
        // Arrange
        AccountResponse response = AccountResponse.builder()
                .id(testId)
                .names("Miguel Angel")
                .lastnames("Blandon Montes")
                .email("miguel@example.com")
                .phone("+573001234567")
                .createdAt(testCreatedAt)
                .build();

        // Act
        String toString = response.toString();

        // Assert
        assertThat(toString).isNotNull();
        assertThat(toString).contains("Miguel Angel");
        assertThat(toString).contains("Blandon Montes");
        assertThat(toString).contains("miguel@example.com");
    }
}