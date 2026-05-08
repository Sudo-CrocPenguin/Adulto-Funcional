package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa los filtros disponibles
 * para la consulta y búsqueda de categorías financieras.
 *
 * <p>Esta clase encapsula los criterios de filtrado que el cliente puede
 * enviar al sistema para acotar los resultados al listar categorías.
 * Se utiliza como parámetro de entrada en los casos de uso y servicios
 * de consulta de categorías.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los parámetros
 * de filtro aplicables sobre el catálogo de categorías.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite filtrar las categorías por su tipo (ingreso, gasto, etc.) y
 * controlar si se deben incluir en los resultados aquellas categorías
 * que han sido eliminadas lógicamente del sistema.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * lo que permite crear instancias de forma legible y flexible sin necesidad
 * de constructores con múltiples parámetros. Los valores de sus campos son
 * accesibles a través de los getters generados por {@code @Getter} de Lombok.
 * Una vez construida, la instancia se pasa como argumento a la capa de
 * aplicación para que el caso de uso aplique los filtros correspondientes
 * sobre el repositorio de categorías.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * CategoryFilterRequest filtro = CategoryFilterRequest.builder()
 *         .type(CategoryType.EXPENSE)
 *         .includeDeleted(false)
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class CategoryFilterRequest {

    /**
     * Tipo de categoría por el cual se desea filtrar.
     *
     * <p>Corresponde a un valor del enumerado {@link CategoryType}, que define
     * las clasificaciones posibles de una categoría dentro del sistema financiero
     * (por ejemplo: ingreso, gasto, ahorro, entre otros).
     * Si este campo es {@code null}, no se aplica filtro por tipo y se retornan
     * categorías de cualquier tipo.</p>
     */
    private CategoryType type;

    /**
     * Indicador que determina si se deben incluir en los resultados
     * las categorías eliminadas lógicamente.
     *
     * <p>Cuando su valor es {@code true}, la consulta retornará tanto las
     * categorías activas como las que han sido marcadas como eliminadas
     * en el sistema. Cuando es {@code false} (valor por defecto en Java
     * para {@code boolean} primitivo), solo se retornan categorías activas.</p>
     */
    private boolean includeDeleted;
}