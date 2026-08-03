package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;
import org.adultofuncional.main.shared.validation.InputPatterns;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para modificar
 * una categoría financiera existente.
 *
 * <p>
 * A diferencia del DTO de creación, todos los campos son opcionales:
 * el cliente puede enviar únicamente aquellos que desea modificar,
 * habilitando actualizaciones parciales (comportamiento PATCH).
 * Los campos no incluidos (nulos) son ignorados por la capa de aplicación
 * y el valor actual del dominio se mantiene sin cambios.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code name} — opcional, máximo 50 caracteres si se proporciona.</li>
 * <li>{@code type} — atributo histórico rechazado porque el tipo es inmutable.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code name} está anotado con {@link NoHtml}.
 * Esto asegura que cualquier valor que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazado con un error 400, evitando
 * la persistencia de scripts maliciosos (Stored XSS). La validación se basa
 * en Jsoup con una {@code Safelist.none()}, es decir, no se permite ningún
 * tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.category.UpdateCategoryUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class UpdateCategoryRequest {

  /**
   * Nuevo nombre que se desea asignar a la categoría financiera.
   *
   * <p>
   * Campo opcional. Si se proporciona, reemplaza el nombre actual
   * de la categoría. Si es {@code null}, el nombre de la categoría
   * permanece sin cambios.
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @Size(max = 50)}: el nombre no puede superar los 50
   * caracteres.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
   * ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  @Pattern(regexp = InputPatterns.NON_BLANK, message = "El nombre no puede estar vacío")
  @NoHtml
  private String name;

  /**
   * Nuevo tipo que se desea asignar a la categoría financiera.
   *
   * <p>
   * Se conserva temporalmente para producir un error de validación explícito
   * cuando un cliente antiguo intenta cambiarlo.
   */
  @Null(message = "El tipo de una categoría no puede modificarse")
  private CategoryType type;
}
