package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los datos que el cliente envía para crear
 * una nueva categoría financiera.
 *
 * <p>
 * <strong>Validaciones aplicadas a cada campo:</strong>
 * <ul>
 * <li>{@code name} — obligatorio, máximo 50 caracteres.</li>
 * <li>{@code type} — obligatorio, debe ser un valor válido de
 * {@link CategoryType}.</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code name} está anotado con {@link NoHtml}.
 * Esto asegura que cualquier petición que contenga HTML (ej. {@code <script>},
 * {@code <img onerror=...>}) será rechazada con un error 400, evitando
 * el almacenamiento de scripts maliciosos en la base de datos (Stored XSS).
 * La validación se basa en Jsoup con una {@code Safelist.none()},
 * es decir, no se permite ningún tag ni atributo HTML.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.CreateCategoryUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class CreateCategoryRequest {

  /**
   * Nombre de la nueva categoría.
   * Obligatorio, máximo 50 caracteres, sin HTML.
   *
   * <p>
   * Ejemplos válidos: "Alimentación", "Transporte", "Salario".
   * No se permiten strings vacíos ni compuestos solo por espacios.
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
  @NoHtml
  private String name;

  /**
   * Tipo de categoría a crear.
   * Obligatorio, debe ser uno de los valores definidos en
   * {@link CategoryType} (ej. {@code INCOME}, {@code EXPENSE}).
   */
  @NotNull(message = "El tipo es obligatorio")
  private CategoryType type;
}
