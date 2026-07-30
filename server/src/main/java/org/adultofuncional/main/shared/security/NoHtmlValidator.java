package org.adultofuncional.main.shared.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Validador Jakarta Bean Validation para la anotación {@link NoHtml}.
 *
 * <p>
 * Verifica que una cadena de texto no contenga HTML. Utiliza Jsoup con una
 * política de limpieza {@link Safelist#none()}, que elimina cualquier tag,
 * atributo o entidad HTML. Si tras la limpieza el texto difiere del original,
 * significa que contenía HTML y la validación falla.
 *
 * <p>
 * <strong>Comportamiento con valores nulos:</strong>
 * Si el valor es {@code null}, el validador retorna {@code true} y delega
 * la comprobación de presencia en anotaciones como {@code @NotNull} o
 * {@code @NotBlank}. Esto evita mensajes de error duplicados en la respuesta
 * de validación.
 *
 * <p>
 * <strong>Uso:</strong> invocado automáticamente por el motor de validación
 * de Jakarta cuando un campo anotado con {@code @NoHtml} es validado (por
 * ejemplo, en un DTO de entrada de un controlador REST).
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see NoHtml
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

  /**
   * Evalúa si la cadena de entrada está libre de HTML.
   *
   * <p>
   * Utiliza {@link Jsoup#clean(String, Safelist)} con {@link Safelist#none()}
   * para eliminar cualquier elemento HTML. Si la cadena limpia es igual a la
   * original, no hay HTML y la validación pasa. Si difiere, hay HTML y la
   * validación falla.
   *
   * @param value   valor a validar; puede ser {@code null}
   * @param context contexto de validación Jakarta (no usado directamente)
   * @return {@code true} si el valor es {@code null} o no contiene HTML,
   *         {@code false} si contiene HTML
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // @NotNull o @NotBlank deben ocuparse de nulos
    }
    // strip() antes de limpiar para no penalizar espacios al inicio/fin
    String stripped = value.strip();
    String cleaned = Jsoup.clean(stripped, Safelist.none());
    return cleaned.equals(stripped);
  }
}
