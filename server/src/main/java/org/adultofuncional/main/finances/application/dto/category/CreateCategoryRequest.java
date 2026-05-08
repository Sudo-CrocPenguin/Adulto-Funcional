package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la solicitud de creación
 * de una nueva categoría financiera en el sistema.
 *
 * <p>Esta clase encapsula y valida los datos que el cliente debe enviar
 * al sistema para registrar una nueva categoría. Actúa como contrato
 * de entrada en el caso de uso de creación de categorías.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa y valida
 * los campos requeridos para crear una categoría financiera, aplicando
 * restricciones de validación mediante anotaciones de Bean Validation
 * (Jakarta) y seguridad de contenido.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * necesarios para crear una categoría: su nombre y su tipo. Garantiza
 * que los datos lleguen correctamente formateados, no vacíos y libres
 * de contenido HTML malicioso antes de que el sistema los procese.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por {@code @Getter}.
 * Las anotaciones de validación ({@code @NotBlank}, {@code @NotNull}, {@code @Size})
 * son procesadas automáticamente por el framework (Spring) al recibir la solicitud,
 * rechazando peticiones inválidas antes de llegar al caso de uso.
 * La anotación {@code @NoHtml} aplica una validación de seguridad personalizada
 * que previene la inyección de etiquetas HTML en el nombre de la categoría.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * CreateCategoryRequest solicitud = CreateCategoryRequest.builder()
 *         .name("Alimentación")
 *         .type(CategoryType.EXPENSE)
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class CreateCategoryRequest {

    /**
     * Nombre de la nueva categoría financiera.
     *
     * <p>Campo obligatorio que identifica de forma legible la categoría
     * dentro del sistema (por ejemplo: "Alimentación", "Transporte", "Salario").</p>
     *
     * <p><b>Restricciones aplicadas:</b></p>
     * <ul>
     *   <li>{@code @NotBlank}: el nombre no puede ser nulo, vacío ni contener
     *       únicamente espacios en blanco.</li>
     *   <li>{@code @Size(max = 50)}: el nombre no puede superar los 50 caracteres.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
     *       ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @NoHtml
    private String name;

    /**
     * Tipo de la categoría financiera a crear.
     *
     * <p>Campo obligatorio que clasifica la categoría dentro del sistema
     * financiero según el enumerado {@link CategoryType}
     * (por ejemplo: ingreso, gasto, ahorro, entre otros).</p>
     *
     * <p><b>Restricciones aplicadas:</b></p>
     * <ul>
     *   <li>{@code @NotNull}: el tipo no puede ser nulo; debe proporcionarse
     *       un valor válido del enumerado {@link CategoryType}.</li>
     * </ul>
     */
    @NotNull(message = "El tipo es obligatorio")
    private CategoryType type;
}