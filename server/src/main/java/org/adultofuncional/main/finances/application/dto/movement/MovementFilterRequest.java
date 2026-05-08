package org.adultofuncional.main.finances.application.dto.movement;

import java.time.LocalDate;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.MovementType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los filtros, ordenamiento y paginación para la consulta
 * de movimientos financieros.
 *
 * <p>
 * Todos los campos son opcionales y pueden combinarse libremente. Los campos
 * nulos son ignorados por la capa de aplicación, que aplica los valores por
 * defecto definidos en el repositorio para el orden y la paginación.
 *
 * <p>
 * <strong>Filtros disponibles:</strong>
 * <ul>
 * <li>{@link #startDate} y {@link #endDate} — rango de fechas del
 * movimiento.</li>
 * <li>{@link #movementType} — tipo de movimiento ({@code INCOME} o
 * {@code EXPENSE}).</li>
 * <li>{@link #categoryId} — categoría asociada.</li>
 * <li>{@link #searchTerm} — búsqueda textual sobre la descripción
 * (máximo 100 caracteres, sin HTML).</li>
 * </ul>
 *
 * <p>
 * <strong>Ordenamiento:</strong> {@link #sortBy} (campo) y
 * {@link #sortDirection} ({@code ASC} o {@code DESC}).
 *
 * <p>
 * <strong>Paginación:</strong> {@link #page} (índice base 0) y
 * {@link #size} (registros por página).
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code searchTerm} está anotado con {@link NoHtml}, rechazando
 * cualquier contenido HTML y previniendo Stored XSS a través de los filtros
 * de búsqueda.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.ListMovementsUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class MovementFilterRequest {

  /**
   * Fecha de inicio del rango de búsqueda de movimientos.
   *
   * <p>
   * Campo opcional que define el límite inferior del rango de fechas
   * sobre el cual se filtran los movimientos. Se representa como
   * {@link LocalDate} sin información de hora ni zona horaria.
   * Si es {@code null}, no se aplica límite inferior en la fecha
   * y se incluyen movimientos desde cualquier fecha anterior.
   */
  private LocalDate startDate;

  /**
   * Fecha de fin del rango de búsqueda de movimientos.
   *
   * <p>
   * Campo opcional que define el límite superior del rango de fechas
   * sobre el cual se filtran los movimientos. Se representa como
   * {@link LocalDate} sin información de hora ni zona horaria.
   * Si es {@code null}, no se aplica límite superior en la fecha
   * y se incluyen movimientos hasta cualquier fecha posterior.
   */
  private LocalDate endDate;

  /**
   * Tipo de movimiento financiero por el cual se desea filtrar.
   *
   * <p>
   * Campo opcional que corresponde a un valor del enumerado
   * {@link MovementType}, permitiendo acotar los resultados a
   * únicamente ingresos o únicamente egresos según corresponda.
   * Si es {@code null}, no se aplica filtro por tipo y se retornan
   * movimientos de cualquier tipo.
   */
  private MovementType movementType;

  /**
   * Identificador de la categoría financiera por la cual se desea filtrar.
   *
   * <p>
   * Campo opcional que permite acotar los resultados a los movimientos
   * asociados a una categoría específica del sistema. Corresponde al UUID
   * único de la categoría registrada.
   * Si es {@code null}, no se aplica filtro por categoría y se retornan
   * movimientos de todas las categorías.
   */
  private UUID categoryId;

  /**
   * Término de búsqueda libre para localizar movimientos por texto.
   *
   * <p>
   * Campo opcional que permite realizar búsquedas sobre campos textuales
   * de los movimientos, como la descripción (por ejemplo: "supermercado",
   * "servicios"). La capa de aplicación determina sobre qué campos
   * se aplica este término.
   * Si es {@code null}, no se aplica filtro por texto.
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @Size(max = 100)}: el término de búsqueda no puede superar
   * los 100 caracteres, protegiendo el rendimiento de las consultas.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
   * previniendo ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @Size(max = 100, message = "Término de búsqueda demasiado largo")
  @NoHtml
  private String searchTerm;

  /**
   * Nombre del campo por el cual se desea ordenar los resultados.
   *
   * <p>
   * Campo opcional que indica el atributo del movimiento sobre el cual
   * se aplicará el ordenamiento de los resultados (por ejemplo:
   * {@code "movementDate"}, {@code "amount"}, {@code "movementType"}).
   * Si es {@code null}, la capa de aplicación aplica el ordenamiento
   * por defecto definido en el repositorio.
   */
  private String sortBy;

  /**
   * Dirección del ordenamiento aplicado sobre los resultados.
   *
   * <p>
   * Campo opcional que complementa a {@code sortBy} indicando si el
   * ordenamiento debe ser ascendente o descendente. Los valores esperados
   * son {@code "ASC"} para ascendente y {@code "DESC"} para descendente.
   * Si es {@code null}, la capa de aplicación aplica la dirección
   * de ordenamiento por defecto.
   */
  private String sortDirection;

  /**
   * Número de página solicitada para la paginación de resultados.
   *
   * <p>
   * Campo opcional que indica qué página de resultados se desea obtener,
   * siendo {@code 0} la primera página. Se utiliza en conjunto con
   * {@code size} para construir la consulta paginada.
   * Si es {@code null}, la capa de aplicación aplica el número de página
   * por defecto.
   */
  private Integer page;

  /**
   * Cantidad de registros por página para la paginación de resultados.
   *
   * <p>
   * Campo opcional que indica cuántos movimientos deben retornarse
   * por página. Se utiliza en conjunto con {@code page} para construir
   * la consulta paginada.
   * Si es {@code null}, la capa de aplicación aplica el tamaño de página
   * por defecto.
   */
  private Integer size;
}
