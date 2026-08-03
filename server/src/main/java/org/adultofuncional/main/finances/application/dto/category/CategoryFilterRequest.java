package org.adultofuncional.main.finances.application.dto.category;

import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.NoHtml;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO que encapsula los filtros opcionales para la consulta de categorías.
 *
 * <p>
 * Utilizado por los endpoints de listado de categorías que requieren paginación
 * y filtrado. La capa de aplicación (caso de uso) lo recibe para construir
 * las condiciones de búsqueda delegadas al repositorio.
 *
 * <p>
 * <strong>Filtros disponibles:</strong>
 * <ul>
 * <li>{@link #type} — permite restringir las categorías por su
 * {@link CategoryType} (ej. {@code Finanzas}, {@code Agenda}). Si es
 * {@code null} se retornan todas las categorías sin distinción de tipo.</li>
 * <li>{@link #searchTerm} — búsqueda insensible a mayúsculas por nombre.</li>
 * </ul>

 * <p>La página predeterminada es 0, el tamaño 20 y el máximo 100. Los campos
 * ordenables se validan mediante una lista blanca en aplicación.</p>
 *
 * <p>
 * <strong>Nota sobre borrado lógico:</strong> El campo {@code includeDeleted}
 * fue removido en versiones recientes porque el esquema actual de categorías
 * no soporta eliminación lógica (solo borrado físico). La documentación
 * anterior
 * lo mencionaba por compatibilidad, pero en la práctica el parámetro ya no se
 * envía ni tiene efecto.
 *
 * <p>
 * Este DTO se construye con Lombok {@link Builder} para facilitar su
 * instanciación en las pruebas y en los controladores.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see CategoryType
 * @see org.adultofuncional.main.finances.application.usecase.category.ListCategoriesUseCase
 */
@Getter
@Builder
public class CategoryFilterRequest {

  /**
   * Tipo de categoría por el cual filtrar.
   *
   * <p>
   * Corresponde al enumerado {@link CategoryType}. Los valores típicos son
   * {@code Finanzas} (categorías de movimientos financieros) y {@code Agenda}
   * (categorías de eventos). Cuando este campo es {@code null}, el filtro
   * no se aplica y se devuelven categorías de todos los tipos.
   */
  private CategoryType type;

  /** Búsqueda opcional e insensible a mayúsculas sobre el nombre. */
  @Size(max = 50, message = "El término de búsqueda no puede exceder 50 caracteres")
  @NoHtml
  private String searchTerm;

  /** Campo lógico permitido para ordenar. */
  private String sortBy;

  /** Dirección {@code ASC} o {@code DESC}. */
  private String sortDirection;

  /** Página base cero; usa 0 por defecto. */
  private Integer page;

  /** Registros por página; usa 20 por defecto y admite hasta 100. */
  private Integer size;
}
