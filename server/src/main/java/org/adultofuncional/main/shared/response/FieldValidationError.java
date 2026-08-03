package org.adultofuncional.main.shared.response;

/**
 * Detalle público de una restricción de validación incumplida.
 *
 * @param field   campo o parámetro que falló
 * @param code    nombre estable de la restricción, por ejemplo {@code NotBlank}
 * @param message explicación segura para una persona
 */
public record FieldValidationError(String field, String code, String message) {
}
