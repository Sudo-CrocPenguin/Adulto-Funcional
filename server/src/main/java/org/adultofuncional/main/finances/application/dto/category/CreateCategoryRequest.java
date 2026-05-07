package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para crear una nueva categoría.
 * <p>
 * <strong>¿Qué es?</strong><br>
 * Objeto que recibe los datos para crear una categoría (solo administradores).
 * <p>
 * <strong>¿Para qué sirve?</strong><br>
 * Valida el nombre y tipo de la categoría antes de pasarlos al caso de uso.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.category.CreateCategoryUseCase
 */
@Getter
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @NoHtml
    private String name;

    @NotNull(message = "El tipo de categoría es obligatorio")
    private CategoryType type;
}
