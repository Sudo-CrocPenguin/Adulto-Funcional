package org.adultofuncional.main.finances.application.dto.movement;

import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO (Data Transfer Object) que representa los filtros disponibles
 * para la consulta, búsqueda y paginación de movimientos financieros
 * en el sistema.
 *
 * <p>Esta clase encapsula todos los criterios de filtrado, ordenamiento
 * y paginación que el cliente puede enviar al sistema para acotar y
 * organizar los resultados al listar movimientos financieros.</p>
 *
 * <p><b>¿Qué es?</b><br>
 * Un objeto de transferencia de datos inmutable que agrupa los parámetros
 * de filtro, búsqueda, ordenamiento y paginación aplicables sobre el
 * registro de movimientos financieros del usuario, combinando filtros
 * estructurados (rango de fechas, tipo, categoría) con búsqueda por
 * texto libre y control de presentación de resultados.</p>
 *
 * <p><b>¿Para qué sirve?</b><br>
 * Permite al cliente filtrar los movimientos financieros por rango de
 * fechas, tipo de movimiento, categoría asociada o término de búsqueda
 * libre. Adicionalmente, controla el ordenamiento de los resultados
 * mediante campo y dirección de orden, y gestiona la paginación mediante
 * número de página y tamaño de página. Todos los campos son opcionales
 * y pueden combinarse libremente.</p>
 *
 * <p><b>¿Cómo funciona?</b><br>
 * Se construye mediante el patrón Builder generado por Lombok ({@code @Builder}),
 * lo que permite crear instancias de forma legible y flexible. Los valores
 * son accesibles a través de los getters generados por {@code @Getter}.
 * La capa de aplicación recibe esta instancia y aplica los filtros no nulos
 * sobre el repositorio de movimientos, construyendo la consulta de forma
 * dinámica según los parámetros presentes. La anotación {@code @NoHtml}
 * sobre el término de búsqueda previene la inyección de etiquetas HTML,
 * y {@code @Size} limita su longitud máxima para proteger el rendimiento
 * de las consultas.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Filtrar movimientos de un rango de fechas con paginación
 * MovementFilterRequest filtro = MovementFilterRequest.builder()
 *         .startDate(LocalDate.of(2025, 1, 1))
 *         .endDate(LocalDate.of(2025, 3, 31))
 *         .movementType(MovementType.EXPENSE)
 *         .sortBy("movementDate")
 *         .sortDirection("DESC")
 *         .page(0)
 *         .size(20)
 *         .build();
 *
 * // Búsqueda por término libre
 * MovementFilterRequest filtro = MovementFilterRequest.builder()
 *         .searchTerm("supermercado")
 *         .page(0)
 *         .size(10)
 *         .build();
 * }</pre>
 *
 * @author Miguel Angel Blandon Montes
 */
@Getter
@Builder
public class MovementFilterRequest {

    /**
     * Fecha de inicio del rango de búsqueda de movimientos.
     *
     * <p>Campo opcional que define el límite inferior del rango de fechas
     * sobre el cual se filtran los movimientos. Se representa como
     * {@link LocalDate} sin información de hora ni zona horaria.
     * Si es {@code null}, no se aplica límite inferior en la fecha
     * y se incluyen movimientos desde cualquier fecha anterior.</p>
     */
    private LocalDate startDate;

    /**
     * Fecha de fin del rango de búsqueda de movimientos.
     *
     * <p>Campo opcional que define el límite superior del rango de fechas
     * sobre el cual se filtran los movimientos. Se representa como
     * {@link LocalDate} sin información de hora ni zona horaria.
     * Si es {@code null}, no se aplica límite superior en la fecha
     * y se incluyen movimientos hasta cualquier fecha posterior.</p>
     */
    private LocalDate endDate;

    /**
     * Tipo de movimiento financiero por el cual se desea filtrar.
     *
     * <p>Campo opcional que corresponde a un valor del enumerado
     * {@link MovementType}, permitiendo acotar los resultados a
     * únicamente ingresos o únicamente egresos según corresponda.
     * Si es {@code null}, no se aplica filtro por tipo y se retornan
     * movimientos de cualquier tipo.</p>
     */
    private MovementType movementType;

    /**
     * Identificador de la categoría financiera por la cual se desea filtrar.
     *
     * <p>Campo opcional que permite acotar los resultados a los movimientos
     * asociados a una categoría específica del sistema. Corresponde al UUID
     * único de la categoría registrada.
     * Si es {@code null}, no se aplica filtro por categoría y se retornan
     * movimientos de todas las categorías.</p>
     */
    private UUID categoryId;

    /**
     * Término de búsqueda libre para localizar movimientos por texto.
     *
     * <p>Campo opcional que permite realizar búsquedas sobre campos textuales
     * de los movimientos, como la descripción (por ejemplo: "supermercado",
     * "servicios"). La capa de aplicación determina sobre qué campos
     * se aplica este término.
     * Si es {@code null}, no se aplica filtro por texto.</p>
     *
     * <p><b>Restricciones aplicadas cuando el valor es proporcionado:</b></p>
     * <ul>
     *   <li>{@code @Size(max = 100)}: el término de búsqueda no puede superar
     *       los 100 caracteres, protegiendo el rendimiento de las consultas.</li>
     *   <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
     *       previniendo ataques de inyección de código en este campo.</li>
     * </ul>
     */
    @Size(max = 100, message = "Término de búsqueda demasiado largo")
    @NoHtml
    private String searchTerm;

    /**
     * Nombre del campo por el cual se desea ordenar los resultados.
     *
     * <p>Campo opcional que indica el atributo del movimiento sobre el cual
     * se aplicará el ordenamiento de los resultados (por ejemplo:
     * {@code "movementDate"}, {@code "amount"}, {@code "movementType"}).
     * Si es {@code null}, la capa de aplicación aplica el ordenamiento
     * por defecto definido en el repositorio.</p>
     */
    private String sortBy;

    /**
     * Dirección del ordenamiento aplicado sobre los resultados.
     *
     * <p>Campo opcional que complementa a {@code sortBy} indicando si el
     * ordenamiento debe ser ascendente o descendente. Los valores esperados
     * son {@code "ASC"} para ascendente y {@code "DESC"} para descendente.
     * Si es {@code null}, la capa de aplicación aplica la dirección
     * de ordenamiento por defecto.</p>
     */
    private String sortDirection;

    /**
     * Número de página solicitada para la paginación de resultados.
     *
     * <p>Campo opcional que indica qué página de resultados se desea obtener,
     * siendo {@code 0} la primera página. Se utiliza en conjunto con
     * {@code size} para construir la consulta paginada.
     * Si es {@code null}, la capa de aplicación aplica el número de página
     * por defecto.</p>
     */
    private Integer page;

    /**
     * Cantidad de registros por página para la paginación de resultados.
     *
     * <p>Campo opcional que indica cuántos movimientos deben retornarse
     * por página. Se utiliza en conjunto con {@code page} para construir
     * la consulta paginada.
     * Si es {@code null}, la capa de aplicación aplica el tamaño de página
     * por defecto.</p>
     */
    private Integer size;
}