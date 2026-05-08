package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa los filtros disponibles
 * para la consulta y búsqueda de gastos fijos en el sistema financiero.
 *
 * <p>Esta clase encapsula los criterios de filtrado que el cliente puede
 * enviar al sistema para acotar los resultados al listar gastos fijos.
 * Se utiliza como parámetro de entrada en los casos de uso y servicios
 * de consulta de gastos fijos.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los parámetros
 * de filtro aplicables sobre el catálogo de gastos fijos del usuario,
 * combinando filtros estructurados (estado, categoría) con búsqueda
 * por texto libre.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite al cliente filtrar los gastos fijos por su estado operativo,
 * por la categoría a la que pertenecen, o mediante un término de búsqueda
 * libre sobre el nombre u otros campos textuales. Cada campo es opcional,
 * por lo que pueden combinarse libremente o usarse de forma independiente.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * lo que permite crear instancias de forma legible y flexible. Los valores
 * son accesibles a través de los getters generados por {@code @Getter}.
 * La capa de aplicación recibe esta instancia y aplica los filtros no nulos
 * sobre el repositorio de gastos fijos. La anotación {@code @NoHtml} sobre
 * el término de búsqueda previene la inyección de etiquetas HTML,
 * y {@code @Size} limita su longitud máxima para proteger el rendimiento
 * de las consultas.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Filtrar por estado y categoría
 * FixedExpenseFilterRequest filtro = FixedExpenseFilterRequest.builder()
 *         .status(Status.ACTIVE)
 *         .categoryId(UUID.fromString("..."))
 *         .build();
 *
 * // Búsqueda por término libre
 * FixedExpenseFilterRequest filtro = FixedExpenseFilterRequest.builder()
 *         .searchTerm("Netflix")
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class FixedExpenseFilterRequest {

    /**
     * Estado operativo por el cual se desea filtrar los gastos fijos.
     *
     * <p>Campo opcional que corresponde a un valor del enumerado {@link Status},
     * el cual define la situación actual del gasto fijo dentro del sistema
     * (por ejemplo: activo, pausado, cancelado, entre otros).
     * Si es {@code null}, no se aplica filtro por estado y se retornan
     * gastos fijos de cualquier estado.</p>
     */
    private Status status;

    /**
     * Identificador de la categoría financiera por la cual se desea filtrar.
     *
     * <p>Campo opcional que permite acotar los resultados a los gastos fijos
     * asociados a una categoría específica del sistema. Corresponde al UUID
     * único de la categoría registrada.
     * Si es {@code null}, no se aplica filtro por categoría y se retornan
     * gastos fijos de todas las categorías.</p>
     */
    private UUID categoryId;

    /**
     * Término de búsqueda libre para localizar gastos fijos por texto.
     *
     * <p>Campo opcional que permite realizar búsquedas sobre campos textuales
     * de los gastos fijos, como el nombre (por ejemplo: "Netflix", "Arriendo").
     * La capa de aplicación determina sobre qué campos se aplica este término.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Size(max = 50)}: el término de búsqueda no puede superar
     *       los 50 caracteres, protegiendo el rendimiento de las consultas.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
     *       previniendo ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @Size(max = 50, message = "Término de búsqueda demasiado largo")
    @NoHtml
    private String searchTerm;
}