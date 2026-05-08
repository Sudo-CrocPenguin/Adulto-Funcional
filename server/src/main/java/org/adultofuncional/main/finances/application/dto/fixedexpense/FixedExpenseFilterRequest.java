package org.adultofuncional.main.finances.application.dto.fixedexpense;

import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.Status;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los filtros opcionales para la consulta de gastos fijos.
 *
 * <p>
 * Permite filtrar por {@link Status}, categoría y un término de búsqueda
 * libre sobre el nombre del gasto. Todos los filtros son opcionales y
 * pueden combinarse libremente; los campos nulos no se aplican en la
 * consulta al repositorio.
 *
 * <p>
 * <strong>Filtros disponibles:</strong>
 * <ul>
 * <li>{@link #status} — filtra por el estado operativo (ej. {@code ACTIVE},
 * {@code PAUSED}).</li>
 * <li>{@link #categoryId} — filtra por la categoría asociada.</li>
 * <li>{@link #searchTerm} — búsqueda textual sobre el nombre del gasto
 * (máximo 50 caracteres, sin HTML).</li>
 * </ul>
 *
 * <p>
 * <strong>Protección contra XSS:</strong>
 * El campo {@code searchTerm} está anotado con {@link NoHtml}, rechazando
 * cualquier contenido HTML y previniendo Stored XSS a través de los filtros
 * de búsqueda.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.finances.application.usecase.fixedexpense.ListFixedExpensesUseCase
 * @see NoHtml
 */
@Getter
@Builder
public class FixedExpenseFilterRequest {

  /**
   * Estado operativo por el cual se desea filtrar los gastos fijos.
   *
   * <p>
   * Campo opcional que corresponde a un valor del enumerado {@link Status},
   * el cual define la situación actual del gasto fijo dentro del sistema
   * (por ejemplo: activo, pausado, cancelado, entre otros).
   * Si es {@code null}, no se aplica filtro por estado y se retornan
   * gastos fijos de cualquier estado.
   */
  private Status status;

  /**
   * Identificador de la categoría financiera por la cual se desea filtrar.
   *
   * <p>
   * Campo opcional que permite acotar los resultados a los gastos fijos
   * asociados a una categoría específica del sistema. Corresponde al UUID
   * único de la categoría registrada.
   * Si es {@code null}, no se aplica filtro por categoría y se retornan
   * gastos fijos de todas las categorías.
   */
  private UUID categoryId;

  /**
   * Término de búsqueda libre para localizar gastos fijos por texto.
   *
   * <p>
   * Campo opcional que permite realizar búsquedas sobre campos textuales
   * de los gastos fijos, como el nombre (por ejemplo: "Netflix", "Arriendo").
   * La capa de aplicación determina sobre qué campos se aplica este término.
   *
   * <p>
   * <b>Restricciones aplicadas cuando el valor es proporcionado:</b>
   * <ul>
   * <li>{@code @Size(max = 50)}: el término de búsqueda no puede superar
   * los 50 caracteres, protegiendo el rendimiento de las consultas.</li>
   * <li>{@code @NoHtml}: no se permite contenido con etiquetas HTML,
   * previniendo ataques de inyección de código en este campo.</li>
   * </ul>
   */
  @Size(max = 50, message = "Término de búsqueda demasiado largo")
  @NoHtml
  private String searchTerm;
}
