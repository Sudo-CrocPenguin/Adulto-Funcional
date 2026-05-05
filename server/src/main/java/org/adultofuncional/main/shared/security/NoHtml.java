package org.adultofuncional.main.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotación de validación Jakarta Bean Validation que restringe los campos
 * de texto para que no contengan HTML.
 *
 * <p>
 * La validación es estricta: cualquier cadena que contenga tags, atributos
 * o entidades HTML (por ejemplo {@code <script>}, {@code <img onerror=...>},
 * {@code &lt;}) se considera inválida y el request será rechazado con un
 * error HTTP 400 (Bad Request).
 *
 * <p>
 * Se basa en Jsoup con una {@code Safelist.none()}, es decir, no se permite
 * ningún elemento ni atributo HTML. Si el valor es {@code null}, la validación
 * se delega en otras anotaciones como {@code @NotNull} o {@code @NotBlank}.
 *
 * <p>
 * <strong>Uso típico:</strong>
 * 
 * <pre>
 * {
 *   &#64;code
 *   &#64;NotBlank
 *   @NoHtml
 *   private String names;
 * }
 * </pre>
 *
 * <p>
 * Esta anotación forma parte de la estrategia de defensa en profundidad
 * contra ataques de Stored XSS. Se aplica a todos los campos de texto
 * de los DTOs de entrada que puedan ser renderizados en una interfaz web.
 *
 * @author Juan Sebastian Rios
 * @since 0.0.1
 * @see NoHtmlValidator
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {

  /**
   * Mensaje de error mostrado cuando la validación falla.
   * Por defecto informa que el campo contiene HTML no permitido.
   */
  String message() default "El campo contiene HTML no permitido";

  /**
   * Grupos de validación a los que pertenece esta restricción.
   * Permite aplicar validaciones de forma condicional según el grupo.
   */
  Class<?>[] groups() default {};

  /**
   * Carga útil adicional que puede ser usada por clientes del API
   * de validación para extender la información del error.
   */
  Class<? extends Payload>[] payload() default {};
}
