package org.adultofuncional.main.account.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para {@link AccountResponse}.
 *
 * <p><strong>¿Qué se prueba?</strong><br>
 * El correcto funcionamiento de los métodos generados por Lombok y la construcción
 * del DTO mediante diferentes patrones.
 *
 * <p><strong>Escenarios cubiertos:</strong>
 * <ul>
 *   <li>Constructor por defecto + setters</li>
 *   <li>Constructor con todos los argumentos (AllArgsConstructor)</li>
 *   <li>Builder pattern de Lombok</li>
 *   <li>Métodos equals() y hashCode()</li>
 *   <li>Método toString()</li>
 * </ul>
 *
 * <p><strong>¿Cómo se prueba?</strong><br>
 * Se crean instancias del DTO mediante diferentes constructores y se verifica
 * que los valores se asignen correctamente y que las comparaciones funcionen.
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
        // Act
        AccountResponse response = new AccountResponse();
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
}