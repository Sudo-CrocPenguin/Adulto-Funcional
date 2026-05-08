package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa la solicitud de actualización
 * de una categoría financiera existente en el sistema.
 *
 * <p>Esta clase encapsula los datos que el cliente puede enviar para modificar
 * parcialmente una categoría ya registrada. A diferencia del DTO de creación,
 * todos sus campos son opcionales, permitiendo actualizaciones parciales
 * donde solo se envían los campos que se desean modificar.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los campos
 * modificables de una categoría financiera, aplicando validaciones de
 * formato y seguridad únicamente sobre los campos que lo requieren.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Transporta desde el cliente hacia la capa de aplicación los datos
 * que se desean actualizar sobre una categoría existente: su nombre,
 * su tipo, o ambos. Al no tener restricciones {@code @NotNull} ni
 * {@code @NotBlank}, el sistema interpreta los campos no enviados
 * como sin cambios, habilitando actualizaciones parciales (PATCH).</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * y sus campos son accesibles a través de los getters generados por {@code @Getter}.
 * Cuando el framework (Spring) recibe la solicitud, aplica las validaciones
 * activas: {@code @Size} verifica que el nombre no exceda el límite permitido
 * y {@code @NoHtml} previene la inyección de etiquetas HTML en el nombre.
 * La capa de aplicación es responsable de ignorar los campos nulos
 * y aplicar solo los cambios proporcionados sobre la entidad existente.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Actualizar solo el nombre
 * UpdateCategoryRequest solicitud = UpdateCategoryRequest.builder()
 *         .name("Transporte público")
 *         .build();
 *
 * // Actualizar nombre y tipo
 * UpdateCategoryRequest solicitud = UpdateCategoryRequest.builder()
 *         .name("Inversiones")
 *         .type(CategoryType.INCOME)
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class UpdateCategoryRequest {

    /**
     * Nuevo nombre que se desea asignar a la categoría financiera.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza el nombre actual
     * de la categoría. Si es {@code null}, el nombre de la categoría
     * permanece sin cambios.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Size(max = 50)}: el nombre no puede superar los 50 caracteres.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML, previniendo
     *       ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @NoHtml
    private String name;

    /**
     * Nuevo tipo que se desea asignar a la categoría financiera.
     *
     * <p>Campo opcional. Si se proporciona, reemplaza la clasificación actual
     * de la categoría según el enumerado {@link CategoryType}
     * (por ejemplo: ingreso, gasto, ahorro, entre otros).
     * Si es {@code null}, el tipo de la categoría permanece sin cambios.</p>
     */
    private CategoryType type;
}